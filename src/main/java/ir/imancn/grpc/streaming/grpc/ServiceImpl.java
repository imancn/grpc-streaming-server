package ir.imancn.grpc.streaming.grpc;

import io.grpc.stub.StreamObserver;
import ir.imancn.grpc.Packet;
import ir.imancn.grpc.ServiceGrpc;
import ir.imancn.grpc.streaming.model.PacketEntity;
import ir.imancn.grpc.streaming.model.PacketEntity.RpcTypeEntity;
import ir.imancn.grpc.streaming.repository.PacketRepository;
import lombok.SneakyThrows;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@GrpcService
public class ServiceImpl extends ServiceGrpc.ServiceImplBase {

    private final PacketRepository packetRepository;

    public ServiceImpl(PacketRepository packetRepository) {
        this.packetRepository = packetRepository;
    }

    @Override
    public void save(Packet request, StreamObserver<Packet> responseObserver) {
        PacketEntity packetEntity = packetRepository.save(new PacketEntity(request));
        responseObserver.onNext(packetEntity.toProtobuf());
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<Packet> saveAll(StreamObserver<Packet> responseObserver) {
        return new StreamObserver<Packet>() {
            final List<Packet> packetList = new ArrayList<>();

            @Override
            public void onNext(Packet packet) {
                packetList.add(packet);
            }

            @Override
            public void onCompleted() {
                List<PacketEntity> packetEntityList = packetRepository.saveAll(
                        packetList.stream().map(PacketEntity::new).collect(Collectors.toList())
                );
                responseObserver.onNext(
                        Packet.newBuilder()
                                .setValue(String.valueOf(packetEntityList.size()))
                                .build()
                );
                responseObserver.onCompleted();
            }

            @Override
            public void onError(Throwable throwable) {
                throw new RuntimeException("Save all failed");
            }
        };
    }

    @SneakyThrows
    @Override
    public void getByRpcType(Packet request, StreamObserver<Packet> responseObserver) {
        RpcTypeEntity rpcType = RpcTypeEntity.valueOf(request.getRpcType().toString());
        List<PacketEntity> packetEntityList = packetRepository.findAllByRpcType(rpcType);
        List<Packet> packetList = packetEntityList.stream().map(PacketEntity::toProtobuf).collect(Collectors.toList());
        for (Packet packet : packetList) {
            responseObserver.onNext(packet);
            Thread.sleep(1000);
        }
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<Packet> getAll(StreamObserver<Packet> responseObserver) {
        return new StreamObserver<Packet>() {
            final List<Packet> requestList = new ArrayList<>();

            @Override
            public void onNext(Packet request) {
                if (requestList.size() >= 3)
                    handleRequestList();
                requestList.add(request);
            }

            @Override
            public void onCompleted() {
                handleRequestList();
                responseObserver.onCompleted();
            }

            @Override
            public void onError(Throwable throwable) {
                throw new RuntimeException("Get all failed");
            }

            @SneakyThrows
            public void handleRequestList() {
                List<Long> idList = requestList.stream().map(Packet::getId).collect(Collectors.toList());
                List<PacketEntity> packetEntityList = packetRepository.findAllById(idList);
                List<Packet> packetList = packetEntityList.stream().map(PacketEntity::toProtobuf).collect(Collectors.toList());
                for (Packet packet : packetList) {
                    responseObserver.onNext(packet);
                    Thread.sleep(500);
                }
                requestList.clear();
            }
        };
    }
}


