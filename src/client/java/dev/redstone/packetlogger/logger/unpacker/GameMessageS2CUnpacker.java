package dev.redstone.packetlogger.logger.unpacker;

import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;

/**
 * Unpacker für GameMessageS2CPacket mit sauber benannten Record-Feldern.
 */
public class GameMessageS2CUnpacker implements PacketUnpacker<GameMessageS2CPacket> {
    @Override
    public String unpack(GameMessageS2CPacket packet) {
        return "{content:" + TextFormatter.format(packet.content()) + ",overlay:" + packet.overlay() + "}";
    }
}
