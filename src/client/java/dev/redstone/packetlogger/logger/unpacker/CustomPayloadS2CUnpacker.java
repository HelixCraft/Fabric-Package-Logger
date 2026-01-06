package dev.redstone.packetlogger.logger.unpacker;

import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

/**
 * Unpacker für CustomPayloadS2CPacket.
 * Versucht den Channel und Payload-Inhalt zu lesen.
 */
public class CustomPayloadS2CUnpacker implements PacketUnpacker<CustomPayloadS2CPacket> {
    
    @Override
    public String unpack(CustomPayloadS2CPacket packet) {
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
            // Bekannte Payload-Typen
            String className = payload.getClass().getSimpleName();
            
            // Brand Payload (Server-Name)
            if (className.contains("Brand")) {
                return extractBrandPayload(payload);
            }
            
            // Generisch: Alle Felder via Reflection
            return ReflectionUnpacker.unpackWithReflection(payload);
            
        } catch (Exception e) {
            return "{error:\"" + e.getMessage() + "\"}";
        }
    }
    
    private String extractBrandPayload(CustomPayload payload) {
        try {
            // Versuche das "brand" Feld zu finden
            for (Field field : payload.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(payload);
                if (value instanceof String) {
                    return "\"" + escapeString((String) value) + "\"";
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }
    
    private String escapeString(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
