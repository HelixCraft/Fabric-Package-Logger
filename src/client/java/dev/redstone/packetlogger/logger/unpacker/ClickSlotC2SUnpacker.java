package dev.redstone.packetlogger.logger.unpacker;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;

import java.util.ArrayList;
import java.util.List;

/**
 * Unpacker für ClickSlotC2SPacket.
 * Zeigt Slot-ID, Action-Type, Button und alle modifizierten Slots.
 */
public class ClickSlotC2SUnpacker implements PacketUnpacker<ClickSlotC2SPacket> {
    
    @Override
    public String unpack(ClickSlotC2SPacket packet) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        
        sb.append("syncId:").append(packet.getSyncId());
        sb.append(",revision:").append(packet.getRevision());
        sb.append(",slot:").append(packet.getSlot());
        sb.append(",button:").append(packet.getButton());
        sb.append(",actionType:\"").append(packet.getActionType().name()).append("\"");
        
        // Action-Type Beschreibung
        sb.append(",actionDescription:\"").append(describeAction(packet.getActionType(), packet.getButton(), packet.getSlot())).append("\"");
        
        // Modifizierte Slots
        Int2ObjectMap<ItemStack> modifiedStacks = packet.getModifiedStacks();
        if (!modifiedStacks.isEmpty()) {
            sb.append(",modifiedSlots:[");
            List<String> mods = new ArrayList<>();
            for (Int2ObjectMap.Entry<ItemStack> entry : modifiedStacks.int2ObjectEntrySet()) {
                mods.add("{slot:" + entry.getIntKey() + ",item:" + ItemStackFormatter.format(entry.getValue()) + "}");
            }
            sb.append(String.join(",", mods));
            sb.append("]");
        }
        
        // Cursor Stack
        ItemStack cursorStack = packet.getStack();
        sb.append(",cursorStack:").append(ItemStackFormatter.format(cursorStack));
        
        sb.append("}");
        return sb.toString();
    }
    
    private String describeAction(SlotActionType type, int button, int slot) {
        switch (type) {
            case PICKUP:
                return button == 0 ? "Left-click pickup" : "Right-click pickup (half)";
            case QUICK_MOVE:
                return "Shift-click (quick move)";
            case SWAP:
                return "Hotbar swap (key " + (button + 1) + ")";
            case CLONE:
                return "Middle-click clone";
            case THROW:
                return button == 0 ? "Drop one item (Q)" : "Drop entire stack (Ctrl+Q)";
            case QUICK_CRAFT:
                return "Drag/Quick craft";
            case PICKUP_ALL:
                return "Double-click pickup all";
            default:
                return type.name();
        }
    }
}
