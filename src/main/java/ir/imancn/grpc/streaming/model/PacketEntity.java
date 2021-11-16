package ir.imancn.grpc.streaming.model;

import ir.imancn.grpc.Packet;
import ir.imancn.grpc.RpcType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Date;

@Entity
@Table(name = "packet")
@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PacketEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Date timestamp;
    private RpcTypeEntity rpcType;
    private String value;

    public PacketEntity(Packet packet) {
        this.id = packet.getId();
        this.rpcType = RpcTypeEntity.valueOf(packet.getRpcType().name());
        this.timestamp = new Date(packet.getTimestamp());
        this.value = packet.getValue();
    }

    public Packet toProtobuf() {
        return Packet.newBuilder()
                .setId(this.id)
                .setTimestamp(this.timestamp.getTime())
                .setRpcType(RpcType.valueOf(this.rpcType.name()))
                .setValue(this.value)
                .build();
    }

    public enum RpcTypeEntity {
        UNARY,
        S2C_STREAMING,
        C2S_STREAMING,
        BIDIRECTIONAL_STREAMING
    }
}



