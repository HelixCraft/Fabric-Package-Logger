package dev.redstone.packetlogger.logger.unpacker;

import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;

/**
 * Unpacker für ScreenHandlerSlotUpdateS2CPacket.
 * Zeigt Slot-ID und komplettes Item mit allen Components.
 */
public class SlotUpdateS2CUnpacker implements PacketUnpacker<ScreenHandlerSlotUpdateS2CPacket> {
    
    @Override
    public String unpack(ScreenHandlerSlotUpdateS2CPacket packet) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        
        sb.append("syncId:").append(packet.getSyncId());
        sb.append(",revision:").append(packet.getRevision());
        sb.append(",slot:").append(packet.getSlot());
        
        ItemStack stack = packet.getStack();
        sb.append(",item:").append(ItemStackFormatter.format(stack));
        
        sb.append("}");
        return sb.toString();
    }
}
