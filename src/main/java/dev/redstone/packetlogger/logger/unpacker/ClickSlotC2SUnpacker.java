package dev.redstone.packetlogger.logger.unpacker;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
//? if >=26.1 {
/*import net.minecraft.network.HashedStack;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.world.inventory.ContainerInput;
*///?} elif >=1.21.5 {
/*import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
*///?} else {
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
//?}

import java.util.ArrayList;
import java.util.List;

/**
 * Unpacker für ClickSlotC2SPacket.
 * Zeigt Slot-ID, Action-Type, Button und alle modifizierten Slots.
 */
//? if >=26.1 {
/*public class ClickSlotC2SUnpacker implements PacketUnpacker<ServerboundContainerClickPacket> {

    @Override
    public String unpack(ServerboundContainerClickPacket packet) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        sb.append("syncId:").append(packet.containerId());
        sb.append(",revision:").append(packet.stateId());
        sb.append(",slot:").append(packet.slotNum());
        sb.append(",button:").append(packet.buttonNum());
        sb.append(",actionType:\"").append(packet.containerInput().name()).append("\"");

        // Action-Type Beschreibung
        sb.append(",actionDescription:\"").append(describeAction(packet.containerInput(), packet.buttonNum(), packet.slotNum())).append("\"");

        // Modifizierte Slots (nur gehasht verfügbar)
        Int2ObjectMap<HashedStack> modifiedStacks = packet.changedSlots();
        if (!modifiedStacks.isEmpty()) {
            sb.append(",modifiedSlots:[");
            List<String> mods = new ArrayList<>();
            for (Int2ObjectMap.Entry<HashedStack> entry : modifiedStacks.int2ObjectEntrySet()) {
                mods.add("{slot:" + entry.getIntKey() + ",hashedItem:\"" + entry.getValue() + "\"}");
            }
            sb.append(String.join(",", mods));
            sb.append("]");
        }

        // Cursor Stack (gehasht)
        HashedStack cursorStack = packet.carriedItem();
        sb.append(",cursorStack:\"").append(cursorStack).append("\"");

        sb.append("}");
        return sb.toString();
    }

    private String describeAction(ContainerInput type, int button, int slot) {
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
*///?} elif >=1.21.5 {
/*public class ClickSlotC2SUnpacker implements PacketUnpacker<ClickSlotC2SPacket> {

    @Override
    public String unpack(ClickSlotC2SPacket packet) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        sb.append("syncId:").append(packet.syncId());
        sb.append(",revision:").append(packet.revision());
        sb.append(",slot:").append(packet.slot());
        sb.append(",button:").append(packet.button());
        sb.append(",actionType:\"").append(packet.actionType().name()).append("\"");

        // Action-Type Beschreibung
        sb.append(",actionDescription:\"").append(describeAction(packet.actionType(), packet.button(), packet.slot()))
                .append("\"");

        // Modifizierte Slots - 1.21.5+: Returns Int2ObjectMap<ItemStackHash>
        var modifiedStacks = packet.modifiedStacks();
        if (!modifiedStacks.isEmpty()) {
            sb.append(",modifiedSlots:[");
            List<String> mods = new ArrayList<>();
            for (Int2ObjectMap.Entry<?> entry : modifiedStacks.int2ObjectEntrySet()) {
                String itemStr = ReflectionUnpacker.unpackWithReflection(entry.getValue());
                mods.add("{slot:" + entry.getIntKey() + ",item:" + itemStr + "}");
            }
            sb.append(String.join(",", mods));
            sb.append("]");
        }

        // Cursor Stack - 1.21.5+: cursor() returns ItemStackHash
        Object cursorHash = packet.cursor();
        sb.append(",cursorStack:").append(ReflectionUnpacker.unpackWithReflection(cursorHash));

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
*///?} else {
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
//?}
