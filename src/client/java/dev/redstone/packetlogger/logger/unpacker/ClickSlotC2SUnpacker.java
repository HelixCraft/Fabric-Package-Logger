package dev.redstone.packetlogger.logger.unpacker;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;

import java.util.ArrayList;
import java.util.List;

/**
 * Unpacker für ClickSlotC2SPacket.
 * Zeigt Slot-ID, Action-Type, Button und alle modifizierten Slots.
 * 
 * 1.21.5+ Hinweis: modifiedStacks und cursor sind jetzt ItemStackHash statt
 * ItemStack.
 */
public class ClickSlotC2SUnpacker implements PacketUnpacker<ClickSlotC2SPacket> {

    @Override
    public String unpack(ClickSlotC2SPacket packet) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        // 1.21.5+: Packets are Records, use field accessors instead of getters
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
                // ItemStackHash kann nicht direkt als ItemStack verwendet werden,
                // daher nutzen wir Reflection zum Auslesen der Daten
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
