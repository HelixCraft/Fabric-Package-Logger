package dev.redstone.packetlogger.logger.unpacker;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.resources.Identifier;

/**
 * Unpacker für ServerboundCustomPayloadPacket.
 * Versucht den Channel und Payload-Inhalt zu lesen.
 */
public class CustomPayloadC2SUnpacker implements PacketUnpacker<ServerboundCustomPayloadPacket> {

    @Override
    public String unpack(ServerboundCustomPayloadPacket packet) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        CustomPacketPayload payload = packet.payload();

        // Channel ID
        Identifier channelId = payload.type().id();
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
    
    private String extractPayloadData(CustomPacketPayload payload) {
        try {
            // Generisch: Alle Felder via Reflection
            return ReflectionUnpacker.unpackWithReflection(payload);
        } catch (Exception e) {
            return "{error:\"" + e.getMessage() + "\"}";
        }
    }
}
