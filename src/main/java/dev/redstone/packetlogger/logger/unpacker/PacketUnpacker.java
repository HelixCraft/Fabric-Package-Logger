package dev.redstone.packetlogger.logger.unpacker;

//? if >=26.1 {
import net.minecraft.network.protocol.Packet;
//?} else {
/*import net.minecraft.network.packet.Packet;
*///?}

/**
 * Interface für spezialisierte Packet-Unpacker.
 * Jeder Unpacker weiß, wie er ein bestimmtes Paket vollständig auslesen kann.
 */
public interface PacketUnpacker<T extends Packet<?>> {
    /**
     * Entpackt das Paket und gibt einen detaillierten String zurück.
     */
    String unpack(T packet);
}
