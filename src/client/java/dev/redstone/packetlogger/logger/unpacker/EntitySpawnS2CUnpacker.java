package dev.redstone.packetlogger.logger.unpacker;

import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.phys.Vec3;

/**
 * Unpacker für ClientboundAddEntityPacket.
 * Zeigt UUID, exakte Koordinaten, EntityType und Velocity.
 */
public class EntitySpawnS2CUnpacker implements PacketUnpacker<ClientboundAddEntityPacket> {

    @Override
    public String unpack(ClientboundAddEntityPacket packet) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        // Entity ID
        sb.append("entityId:").append(packet.getId());

        // UUID
        sb.append(",uuid:\"").append(packet.getUUID().toString()).append("\"");

        // Entity Type
        try {
            String typeId = BuiltInRegistries.ENTITY_TYPE.getKey(packet.getType()).toString();
            sb.append(",type:\"").append(typeId).append("\"");
        } catch (Exception e) {
            sb.append(",type:\"unknown\"");
        }

        // Position (exakt)
        sb.append(",pos:{x:").append(packet.getX())
          .append(",y:").append(packet.getY())
          .append(",z:").append(packet.getZ()).append("}");

        // Rotation
        sb.append(",rotation:{pitch:").append(packet.getXRot())
          .append(",yaw:").append(packet.getYRot())
          .append(",headYaw:").append(packet.getYHeadRot()).append("}");

        // Velocity
        Vec3 velocity = packet.getMovement();
        sb.append(",velocity:{x:").append(velocity.x)
          .append(",y:").append(velocity.y)
          .append(",z:").append(velocity.z).append("}");

        // Entity Data (z.B. für Projectiles die Owner-ID)
        int entityData = packet.getData();
        if (entityData != 0) {
            sb.append(",entityData:").append(entityData);
        }

        sb.append("}");
        return sb.toString();
    }
}
