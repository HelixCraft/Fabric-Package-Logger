package dev.redstone.packetlogger.logger.unpacker;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Unpacker für ClientboundUpdateAttributesPacket.
 * Zeigt alle Attribute mit Base-Value und Modifiers.
 */
public class EntityAttributesS2CUnpacker implements PacketUnpacker<ClientboundUpdateAttributesPacket> {

    @Override
    public String unpack(ClientboundUpdateAttributesPacket packet) {
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
        List<ClientboundUpdateAttributesPacket.AttributeSnapshot> entries = packet.getValues();
        if (!entries.isEmpty()) {
            sb.append(",attributes:[");
            List<String> attrs = new ArrayList<>();
            for (ClientboundUpdateAttributesPacket.AttributeSnapshot entry : entries) {
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

    private String formatAttributeEntry(ClientboundUpdateAttributesPacket.AttributeSnapshot entry) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        // Attribute ID
        try {
            sb.append("attribute:\"").append(entry.attribute().getRegisteredName()).append("\"");
        } catch (Exception e) {
            sb.append("attribute:\"unknown\"");
        }

        // Base Value
        sb.append(",baseValue:").append(entry.base());

        // Modifiers
        Collection<AttributeModifier> modifiers = entry.modifiers();
        if (!modifiers.isEmpty()) {
            sb.append(",modifiers:[");
            List<String> mods = new ArrayList<>();
            for (AttributeModifier mod : modifiers) {
                StringBuilder modSb = new StringBuilder();
                modSb.append("{");
                modSb.append("id:\"").append(mod.id()).append("\"");
                modSb.append(",value:").append(mod.amount());
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
