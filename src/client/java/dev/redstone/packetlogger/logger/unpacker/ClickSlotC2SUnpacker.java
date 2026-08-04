package dev.redstone.packetlogger.logger.unpacker;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.network.HashedStack;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.world.inventory.ContainerInput;

import java.util.ArrayList;
import java.util.List;

/**
 * Unpacker für ServerboundContainerClickPacket.
 * Zeigt Slot-ID, Action-Type, Button und alle modifizierten Slots.
 *
 * Hinweis: In 26.2 sendet der Client nur noch gehashte Item-Stacks (HashedStack),
 * daher können hier keine vollständigen Item-Daten mehr aufgelöst werden.
 */
public class ClickSlotC2SUnpacker implements PacketUnpacker<ServerboundContainerClickPacket> {

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
