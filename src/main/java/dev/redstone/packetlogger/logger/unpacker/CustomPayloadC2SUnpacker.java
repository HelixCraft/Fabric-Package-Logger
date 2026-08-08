package dev.redstone.packetlogger.logger.unpacker;

//? if >=26.1 {
/*import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.resources.Identifier;
*///?} else {
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;

import java.lang.reflect.Field;
//?}

/**
 * Unpacker für CustomPayloadC2SPacket.
 * Versucht den Channel und Payload-Inhalt zu lesen.
 */
public class CustomPayloadC2SUnpacker implements PacketUnpacker<
//? if >=26.1 {
    /*ServerboundCustomPayloadPacket
*///?} else {
    CustomPayloadC2SPacket
//?}
> {
    
    @Override
    public String unpack(
//? if >=26.1 {
        /*ServerboundCustomPayloadPacket packet
*///?} else {
        CustomPayloadC2SPacket packet
//?}
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        
        //? if >=26.1 {
        /*CustomPacketPayload payload = packet.payload();
        *///?} else {
        CustomPayload payload = packet.payload();
        //?}
        
        // Channel ID
        Identifier channelId = /*? if >=26.1 { */ /*payload.type().id(); *//*?} else { */ payload.getId().id(); /*?} */
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
    
    private String extractPayloadData(
//? if >=26.1 {
        /*CustomPacketPayload payload
*///?} else {
        CustomPayload payload
//?}
    ) {
        try {
            // Generisch: Alle Felder via Reflection
            return ReflectionUnpacker.unpackWithReflection(payload);
        } catch (Exception e) {
            return "{error:\"" + e.getMessage() + "\"}";
        }
    }
}
