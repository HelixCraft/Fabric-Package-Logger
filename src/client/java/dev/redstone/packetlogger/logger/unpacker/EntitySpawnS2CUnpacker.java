package dev.redstone.packetlogger.logger.unpacker;

import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.registry.Registries;

/**
 * Unpacker für EntitySpawnS2CPacket.
 * Zeigt UUID, exakte Koordinaten, EntityType und Velocity.
 */
public class EntitySpawnS2CUnpacker implements PacketUnpacker<EntitySpawnS2CPacket> {
    
    @Override
    public String unpack(EntitySpawnS2CPacket packet) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        
        // Entity ID
        sb.append("entityId:").append(packet.getEntityId());
        
        // UUID
        sb.append(",uuid:\"").append(packet.getUuid().toString()).append("\"");
        
        // Entity Type
        try {
            String typeId = Registries.ENTITY_TYPE.getId(packet.getEntityType()).toString();
            sb.append(",type:\"").append(typeId).append("\"");
        } catch (Exception e) {
            sb.append(",type:\"unknown\"");
        }
        
        // Position (exakt)
        sb.append(",pos:{x:").append(packet.getX())
          .append(",y:").append(packet.getY())
          .append(",z:").append(packet.getZ()).append("}");
        
        // Rotation
        sb.append(",rotation:{pitch:").append(packet.getPitch())
          .append(",yaw:").append(packet.getYaw())
          .append(",headYaw:").append(packet.getHeadYaw()).append("}");
        
        // Velocity
        sb.append(",velocity:{x:").append(packet.getVelocityX())
          .append(",y:").append(packet.getVelocityY())
          .append(",z:").append(packet.getVelocityZ()).append("}");
        
        // Entity Data (z.B. für Projectiles die Owner-ID)
        int entityData = packet.getEntityData();
        if (entityData != 0) {
            sb.append(",entityData:").append(entityData);
        }
        
        sb.append("}");
        return sb.toString();
    }
}
