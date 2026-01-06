package dev.redstone.packetlogger.logger.unpacker;

import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;

import java.lang.reflect.Field;

/**
 * Unpacker für CustomPayloadC2SPacket.
 * Versucht den Channel und Payload-Inhalt zu lesen.
 */
public class CustomPayloadC2SUnpacker implements PacketUnpacker<CustomPayloadC2SPacket> {
    
    @Override
    public String unpack(CustomPayloadC2SPacket packet) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        
        CustomPayload payload = packet.payload();
        
        // Channel ID
        Identifier channelId = payload.getId().id();
        sb.append("channel:\"").append(channelId.toString()).append("\"");
        
        // Payload Type
        sb.append(",payloadType:\"").append(payload.getClass().getSimpleName()).append("\"");
        
        // Versuche Payload-Daten zu extrahieren
        String payloadData = extractPayloadData(payload);
        if (payloadData != null) {
            sb.append(",data:").append(payloadData);
        }
        
        sb.append("}");
        return sb.toString();
    }
    
    private String extractPayloadData(CustomPayload payload) {
        try {
            // Generisch: Alle Felder via Reflection
            return ReflectionUnpacker.unpackWithReflection(payload);
        } catch (Exception e) {
            return "{error:\"" + e.getMessage() + "\"}";
        }
    }
}
