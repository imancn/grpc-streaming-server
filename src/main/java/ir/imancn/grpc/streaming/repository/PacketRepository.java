package ir.imancn.grpc.streaming.repository;

import ir.imancn.grpc.streaming.model.PacketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PacketRepository extends JpaRepository<PacketEntity, Long> {

    List<PacketEntity> findAllByRpcType(PacketEntity.RpcTypeEntity rpcType);
}
