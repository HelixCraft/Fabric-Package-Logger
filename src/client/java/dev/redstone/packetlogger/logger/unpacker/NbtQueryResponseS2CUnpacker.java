package dev.redstone.packetlogger.logger.unpacker;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundTagQueryPacket;

/**
 * Unpacker für ClientboundTagQueryPacket.
 * Zeigt das komplette empfangene NBT.
 */
public class NbtQueryResponseS2CUnpacker implements PacketUnpacker<ClientboundTagQueryPacket> {

    @Override
    public String unpack(ClientboundTagQueryPacket packet) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        sb.append("transactionId:").append(packet.getTransactionId());

        CompoundTag nbt = packet.getTag();
        if (nbt != null && !nbt.isEmpty()) {
            sb.append(",nbt:").append(nbt.toString());
        } else {
            sb.append(",nbt:null");
        }
        
        sb.append("}");
        return sb.toString();
    }
}
