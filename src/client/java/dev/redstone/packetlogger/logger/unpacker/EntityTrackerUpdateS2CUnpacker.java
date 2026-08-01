package dev.redstone.packetlogger.logger.unpacker;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.ArrayList;
import java.util.List;

/**
 * Unpacker für ClientboundSetEntityDataPacket.
 * Zeigt Entity-ID und alle DataTracker-Einträge mit aufgelösten Werten.
 */
public class EntityTrackerUpdateS2CUnpacker implements PacketUnpacker<ClientboundSetEntityDataPacket> {

    @Override
    public String unpack(ClientboundSetEntityDataPacket packet) {
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
        List<SynchedEntityData.DataValue<?>> entries = packet.packedItems();
        if (entries != null && !entries.isEmpty()) {
            sb.append(",trackedValues:[");
            List<String> values = new ArrayList<>();
            for (SynchedEntityData.DataValue<?> entry : entries) {
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
            Minecraft client = Minecraft.getInstance();
            if (client.level != null) {
                Entity entity = client.level.getEntity(entityId);
                if (entity != null) {
                    return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }

    private String formatTrackerEntry(SynchedEntityData.DataValue<?> entry) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("id:").append(entry.id());

        Object value = entry.value();
        sb.append(",value:").append(ReflectionUnpacker.unpackWithReflection(value));

        // Versuche den Serializer-Typ zu ermitteln
        try {
            String serializerName = entry.serializer().getClass().getSimpleName();
            sb.append(",type:\"").append(serializerName).append("\"");
        } catch (Exception e) {
            // Ignore
        }

        sb.append("}");
        return sb.toString();
    }
}
