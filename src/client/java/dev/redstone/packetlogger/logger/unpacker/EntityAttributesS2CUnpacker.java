package dev.redstone.packetlogger.logger.unpacker;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.network.packet.s2c.play.EntityAttributesS2CPacket;
import net.minecraft.registry.Registries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Unpacker für EntityAttributesS2CPacket.
 * Zeigt alle Attribute mit Base-Value und Modifiers.
 */
public class EntityAttributesS2CUnpacker implements PacketUnpacker<EntityAttributesS2CPacket> {
    
    @Override
    public String unpack(EntityAttributesS2CPacket packet) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        
        int entityId = packet.getEntityId();
        sb.append("entityId:").append(entityId);
        
        // Entity-Typ ermitteln
        String entityType = getEntityType(entityId);
        if (entityType != null) {
            sb.append(",entityType:\"").append(entityType).append("\"");
        }
        
        // Attribute
        List<EntityAttributesS2CPacket.Entry> entries = packet.getEntries();
        if (!entries.isEmpty()) {
            sb.append(",attributes:[");
            List<String> attrs = new ArrayList<>();
            for (EntityAttributesS2CPacket.Entry entry : entries) {
                attrs.add(formatAttributeEntry(entry));
            }
            sb.append(String.join(",", attrs));
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
    
    private String formatAttributeEntry(EntityAttributesS2CPacket.Entry entry) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        
        // Attribute ID
        try {
            sb.append("attribute:\"").append(entry.attribute().getIdAsString()).append("\"");
        } catch (Exception e) {
            sb.append("attribute:\"unknown\"");
        }
        
        // Base Value
        sb.append(",baseValue:").append(entry.base());
        
        // Modifiers
        Collection<EntityAttributeModifier> modifiers = entry.modifiers();
        if (!modifiers.isEmpty()) {
            sb.append(",modifiers:[");
            List<String> mods = new ArrayList<>();
            for (EntityAttributeModifier mod : modifiers) {
                StringBuilder modSb = new StringBuilder();
                modSb.append("{");
                modSb.append("id:\"").append(mod.id()).append("\"");
                modSb.append(",value:").append(mod.value());
                modSb.append(",operation:\"").append(mod.operation().name()).append("\"");
                modSb.append("}");
                mods.add(modSb.toString());
            }
            sb.append(String.join(",", mods));
            sb.append("]");
        }
        
        sb.append("}");
        return sb.toString();
    }
}
