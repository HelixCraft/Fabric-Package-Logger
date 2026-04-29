package dev.redstone.packetlogger.logger.unpacker;

import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.UnknownCustomPayload;
import net.minecraft.network.packet.BrandCustomPayload;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;

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
        
        Identifier channelId = payload.getId().id();
        sb.append("channel:\"").append(channelId).append("\"");
        sb.append(",payloadType:\"").append(payload.getClass().getSimpleName()).append("\"");

        String payloadData = extractPayloadData(payload);
        if (payloadData != null) {
            sb.append(",data:").append(payloadData);
        }

        sb.append("}");
        return sb.toString();
    }

    private String extractPayloadData(CustomPayload payload) {
        try {
            if (payload instanceof BrandCustomPayload brandPayload) {
                return TextFormatter.formatPlainString(brandPayload.brand());
            }

            if (payload instanceof UnknownCustomPayload unknownPayload) {
                return "{unknownChannel:" + TextFormatter.formatPlainString(unknownPayload.id().toString()) + "}";
            }

            return ReflectionUnpacker.unpackWithReflection(payload);
        } catch (Exception e) {
            return "{error:\"" + TextFormatter.escapeString(e.getMessage()) + "\"}";
        }
    }
}
