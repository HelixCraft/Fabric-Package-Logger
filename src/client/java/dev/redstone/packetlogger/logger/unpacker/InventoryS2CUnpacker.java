package dev.redstone.packetlogger.logger.unpacker;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.registry.Registries;

import java.util.ArrayList;
import java.util.List;

/**
 * Unpacker für InventoryS2CPacket.
 * Formatiert den Inhalt im Container-NBT-Format:
 * {components:{"minecraft:container":[{item:{...},slot:X}]},id:"minecraft:generic_9x3"}
 */
public class InventoryS2CUnpacker implements PacketUnpacker<InventoryS2CPacket> {

    @Override
    public String unpack(InventoryS2CPacket packet) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        // 1.21.5+: Packets are Records, use field accessors instead of getters
        int syncId = packet.syncId();
        int revision = packet.revision();
        sb.append("syncId:").append(syncId);
        sb.append(",revision:").append(revision);

        // Screen-Typ ermitteln
        String screenType = getScreenType(syncId);
        if (screenType != null) {
            sb.append(",id:\"").append(screenType).append("\"");
        }

        // Container-Inhalt im NBT-Format
        List<ItemStack> contents = packet.contents();
        sb.append(",components:{\"minecraft:container\":[");

        List<String> items = new ArrayList<>();
        for (int slot = 0; slot < contents.size(); slot++) {
            ItemStack stack = contents.get(slot);
            if (stack != null && !stack.isEmpty()) {
                String itemStr = ItemStackFormatter.formatForSlot(stack, slot);
                if (itemStr != null) {
                    items.add(itemStr);
                }
            }
        }
        sb.append(String.join(",", items));
        sb.append("]}");

        // Cursor Item
        ItemStack cursorStack = packet.cursorStack();
        if (cursorStack != null && !cursorStack.isEmpty()) {
            sb.append(",cursorStack:").append(ItemStackFormatter.format(cursorStack));
        }

        sb.append("}");
        return sb.toString();
    }

    /**
     * Versucht den Screen-Typ aus der SyncId zu ermitteln.
     */
    private String getScreenType(int syncId) {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null)
                return null;

            ScreenHandler handler = client.player.currentScreenHandler;
            if (handler != null && handler.syncId == syncId) {
                ScreenHandlerType<?> type = handler.getType();
                if (type != null) {
                    return Registries.SCREEN_HANDLER.getId(type).toString();
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }
}
