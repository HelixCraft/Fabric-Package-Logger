package dev.redstone.packetlogger.logger.unpacker;

//? if >=26.1 {
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundTagQueryPacket;
//?} else {
/*import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.NbtQueryResponseS2CPacket;
*///?}

/**
 * Unpacker für NbtQueryResponseS2CPacket.
 * Zeigt das komplette empfangene NBT.
 */
public class NbtQueryResponseS2CUnpacker implements PacketUnpacker<
//? if >=26.1 {
    ClientboundTagQueryPacket
//?} else {
    /*NbtQueryResponseS2CPacket*/
//?}
> {
    
    @Override
    public String unpack(
//? if >=26.1 {
        ClientboundTagQueryPacket packet
//?} else {
        /*NbtQueryResponseS2CPacket packet*/
//?}
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        
        sb.append("transactionId:").append(packet.getTransactionId());
        
        //? if >=26.1 {
        CompoundTag nbt = packet.getTag();
        //?} else {
        /*NbtCompound nbt = packet.getNbt();*/
        //?}
        if (nbt != null && !nbt.isEmpty()) {
            sb.append(",nbt:").append(/*? if >=26.1 { */ nbt.toString() /*?} else { */ nbt.asString() /*?} */);
        } else {
            sb.append(",nbt:null");
        }
        
        sb.append("}");
        return sb.toString();
    }
}
