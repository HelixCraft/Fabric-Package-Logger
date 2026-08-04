package dev.redstone.packetlogger.logger.unpacker;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.ArrayList;
import java.util.List;

/**
 * Unpacker für ClientboundContainerSetContentPacket.
 * Formatiert den Inhalt im Container-NBT-Format:
 * {components:{"minecraft:container":[{item:{...},slot:X}]},id:"minecraft:generic_9x3"}
 */
public class InventoryS2CUnpacker implements PacketUnpacker<ClientboundContainerSetContentPacket> {

    @Override
    public String unpack(ClientboundContainerSetContentPacket packet) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        // SyncId und Revision (StateId)
        int syncId = packet.containerId();
        int revision = packet.stateId();
        sb.append("syncId:").append(syncId);
        sb.append(",revision:").append(revision);

        // Screen-Typ ermitteln
        String screenType = getScreenType(syncId);
        if (screenType != null) {
            sb.append(",id:\"").append(screenType).append("\"");
        }

        // Container-Inhalt im NBT-Format
        List<ItemStack> contents = packet.items();
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
        ItemStack cursorStack = packet.carriedItem();
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
            Minecraft client = Minecraft.getInstance();
            if (client.player == null) return null;

            AbstractContainerMenu handler = client.player.containerMenu;
            if (handler != null && handler.containerId == syncId) {
                MenuType<?> type = handler.getType();
                if (type != null) {
                    return BuiltInRegistries.MENU.getKey(type).toString();
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }
}
