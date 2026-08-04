package dev.redstone.packetlogger.logger.unpacker;

import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;

/**
 * Unpacker für ClientboundContainerSetSlotPacket.
 * Zeigt Slot-ID und komplettes Item mit allen Components.
 */
public class SlotUpdateS2CUnpacker implements PacketUnpacker<ClientboundContainerSetSlotPacket> {

    @Override
    public String unpack(ClientboundContainerSetSlotPacket packet) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        sb.append("syncId:").append(packet.getContainerId());
        sb.append(",revision:").append(packet.getStateId());
        sb.append(",slot:").append(packet.getSlot());

        ItemStack stack = packet.getItem();
        sb.append(",item:").append(ItemStackFormatter.format(stack));
        
        sb.append("}");
        return sb.toString();
    }
}
