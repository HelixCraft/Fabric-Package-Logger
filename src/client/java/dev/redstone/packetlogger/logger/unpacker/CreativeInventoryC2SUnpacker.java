package dev.redstone.packetlogger.logger.unpacker;

import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;

/**
 * Unpacker für ServerboundSetCreativeModeSlotPacket.
 * Zeigt welches Item in welchen Slot gesetzt wird.
 */
public class CreativeInventoryC2SUnpacker implements PacketUnpacker<ServerboundSetCreativeModeSlotPacket> {

    @Override
    public String unpack(ServerboundSetCreativeModeSlotPacket packet) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        // Slot (record accessor)
        int slot = packet.slotNum();
        sb.append("slot:").append(slot);

        // ItemStack (record accessor)
        ItemStack stack = packet.itemStack();
        if (stack == null) stack = ItemStack.EMPTY;
        sb.append(",item:").append(ItemStackFormatter.format(stack));

        sb.append("}");
        return sb.toString();
    }
}
