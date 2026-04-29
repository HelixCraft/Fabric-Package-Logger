package dev.redstone.packetlogger.logger.unpacker;

import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;

import java.util.ArrayList;
import java.util.List;

/**
 * Unpacker für BundleS2CPacket.
 * Loggt enthaltene Pakete einzeln, damit Bundle-Pakete nachvollziehbar bleiben.
 */
public class BundleS2CUnpacker implements PacketUnpacker<BundleS2CPacket> {
    @Override
    public String unpack(BundleS2CPacket packet) {
        List<String> entries = new ArrayList<>();

        for (Packet<? super ClientPlayPacketListener> innerPacket : packet.getPackets()) {
            String packetName = innerPacket.getClass().getSimpleName();
            String packetData = ReflectionUnpacker.unpackWithReflection(innerPacket);
            entries.add("{packet:\"" + packetName + "\",data:" + packetData + "}");
        }

        return "{packetCount:" + entries.size() + ",packets:[" + String.join(",", entries) + "]}";
    }
}
