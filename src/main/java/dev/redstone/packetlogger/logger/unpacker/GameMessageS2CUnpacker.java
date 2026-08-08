package dev.redstone.packetlogger.logger.unpacker;

//? if >=26.1 {
/*import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
*///?} else {
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
//?}

/**
 * Unpacker für GameMessageS2CPacket mit sauber benannten Record-Feldern.
 */
public class GameMessageS2CUnpacker implements PacketUnpacker<
//? if >=26.1 {
    /*ClientboundSystemChatPacket
*///?} else {
    GameMessageS2CPacket
//?}
> {
    @Override
    public String unpack(
//? if >=26.1 {
        /*ClientboundSystemChatPacket packet
*///?} else {
        GameMessageS2CPacket packet
//?}
    ) {
        return "{content:" + TextFormatter.format(packet.content()) + ",overlay:" + packet.overlay() + "}";
    }
}