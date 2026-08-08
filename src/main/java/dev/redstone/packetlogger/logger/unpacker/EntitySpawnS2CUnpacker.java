package dev.redstone.packetlogger.logger.unpacker;

//? if >=26.1 {
/*import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.phys.Vec3;
*///?} else {
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.registry.Registries;
//?}

/**
 * Unpacker für EntitySpawnS2CPacket.
 * Zeigt UUID, exakte Koordinaten, EntityType und Velocity.
 */
//? if >=26.1 {
/*public class EntitySpawnS2CUnpacker implements PacketUnpacker<ClientboundAddEntityPacket> {

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
*///?} elif >=1.21.9 {
/*public class EntitySpawnS2CUnpacker implements PacketUnpacker<EntitySpawnS2CPacket> {

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
        sb.append(",velocity:{x:").append(packet.getVelocity().getX())
         .append(",y:").append(packet.getVelocity().getY())
         .append(",z:").append(packet.getVelocity().getZ()).append("}");

        // Entity Data (z.B. für Projectiles die Owner-ID)
        int entityData = packet.getEntityData();
        if (entityData != 0) {
            sb.append(",entityData:").append(entityData);
        }

        sb.append("}");
        return sb.toString();
    }
}
*///?} else {
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
//?}