package dev.redstone.packetlogger.logger.unpacker;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.NbtQueryResponseS2CPacket;

/**
 * Unpacker für NbtQueryResponseS2CPacket.
 * Zeigt das komplette empfangene NBT.
 */
public class NbtQueryResponseS2CUnpacker implements PacketUnpacker<NbtQueryResponseS2CPacket> {
    
    @Override
    public String unpack(NbtQueryResponseS2CPacket packet) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        
        sb.append("transactionId:").append(packet.getTransactionId());
        
        NbtCompound nbt = packet.getNbt();
        if (nbt != null && !nbt.isEmpty()) {
            sb.append(",nbt:").append(nbt.asString());
        } else {
            sb.append(",nbt:null");
        }
        
        sb.append("}");
        return sb.toString();
    }
}
