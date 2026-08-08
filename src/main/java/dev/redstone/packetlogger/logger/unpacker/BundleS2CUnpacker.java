package dev.redstone.packetlogger.logger.unpacker;

//? if >=26.1 {
/*import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.Packet;
*///?} else {
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
//?}

import java.util.ArrayList;
import java.util.List;

/**
 * Unpacker für BundleS2CPacket.
 * Loggt enthaltene Pakete einzeln, damit Bundle-Pakete nachvollziehbar bleiben.
 */
public class BundleS2CUnpacker implements PacketUnpacker<
//? if >=26.1 {
    /*ClientboundBundlePacket
*///?} else {
    BundleS2CPacket
//?}
> {
    @Override
    public String unpack(
//? if >=26.1 {
        /*ClientboundBundlePacket packet
*///?} else {
        BundleS2CPacket packet
//?}
    ) {
        List<String> entries = new ArrayList<>();

        //? if >=26.1 {
        /*for (Packet<? super ClientGamePacketListener> innerPacket : packet.subPackets()) {
        *///?} else {
        for (Packet<? super ClientPlayPacketListener> innerPacket : packet.getPackets()) {
        //?}
            String packetName = innerPacket.getClass().getSimpleName();
            String packetData = ReflectionUnpacker.unpackWithReflection(innerPacket);
            entries.add("{packet:\"" + packetName + "\",data:" + packetData + "}");
        }

        return "{packetCount:" + entries.size() + ",packets:[" + String.join(",", entries) + "]}";
    }
}