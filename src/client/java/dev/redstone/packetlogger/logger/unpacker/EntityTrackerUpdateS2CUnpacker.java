package dev.redstone.packetlogger.logger.unpacker;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.registry.Registries;

import java.util.ArrayList;
import java.util.List;

/**
 * Unpacker für EntityTrackerUpdateS2CPacket.
 * Zeigt Entity-ID und alle DataTracker-Einträge mit aufgelösten Werten.
 */
public class EntityTrackerUpdateS2CUnpacker implements PacketUnpacker<EntityTrackerUpdateS2CPacket> {
    
    @Override
    public String unpack(EntityTrackerUpdateS2CPacket packet) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        
        int entityId = packet.id();
        sb.append("entityId:").append(entityId);
        
        // Entity-Typ ermitteln
        String entityType = getEntityType(entityId);
        if (entityType != null) {
            sb.append(",entityType:\"").append(entityType).append("\"");
        }
        
        // DataTracker Entries
        List<DataTracker.SerializedEntry<?>> entries = packet.trackedValues();
        if (entries != null && !entries.isEmpty()) {
            sb.append(",trackedValues:[");
            List<String> values = new ArrayList<>();
            for (DataTracker.SerializedEntry<?> entry : entries) {
                values.add(formatTrackerEntry(entry));
            }
            sb.append(String.join(",", values));
            sb.append("]");
        }
        
        sb.append("}");
        return sb.toString();
    }
    
    private String getEntityType(int entityId) {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world != null) {
                Entity entity = client.world.getEntityById(entityId);
                if (entity != null) {
                    return Registries.ENTITY_TYPE.getId(entity.getType()).toString();
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }
    
    private String formatTrackerEntry(DataTracker.SerializedEntry<?> entry) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("id:").append(entry.id());
        
        Object value = entry.value();
        sb.append(",value:").append(ReflectionUnpacker.unpackWithReflection(value));
        
        // Versuche den Serializer-Typ zu ermitteln
        try {
            String serializerName = entry.handler().getClass().getSimpleName();
            sb.append(",type:\"").append(serializerName).append("\"");
        } catch (Exception e) {
            // Ignore
        }
        
        sb.append("}");
        return sb.toString();
    }
}
