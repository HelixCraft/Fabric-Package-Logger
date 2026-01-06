package dev.redstone.packetlogger.logger.unpacker;

import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Unpacker für CreativeInventoryActionC2SPacket.
 * Zeigt welches Item in welchen Slot gesetzt wird.
 */
public class CreativeInventoryC2SUnpacker implements PacketUnpacker<CreativeInventoryActionC2SPacket> {
    
    @Override
    public String unpack(CreativeInventoryActionC2SPacket packet) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        
        // Slot via Reflection (API kann sich ändern)
        int slot = getFieldValue(packet, "slot", Integer.class, -1);
        sb.append("slot:").append(slot);
        
        // ItemStack via Reflection
        ItemStack stack = getFieldValue(packet, "stack", ItemStack.class, ItemStack.EMPTY);
        if (stack == null) stack = ItemStack.EMPTY;
        sb.append(",item:").append(ItemStackFormatter.format(stack));
        
        sb.append("}");
        return sb.toString();
    }
    
    @SuppressWarnings("unchecked")
    private <T> T getFieldValue(Object obj, String fieldName, Class<T> type, T defaultValue) {
        try {
            // Versuche Getter
            try {
                String getterName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
                Method getter = obj.getClass().getMethod(getterName);
                return (T) getter.invoke(obj);
            } catch (NoSuchMethodException e) {
                // Ignore
            }
            
            // Versuche direktes Feld
            for (Field field : obj.getClass().getDeclaredFields()) {
                if (field.getName().equalsIgnoreCase(fieldName) || field.getName().contains(fieldName)) {
                    field.setAccessible(true);
                    Object value = field.get(obj);
                    if (type.isInstance(value)) {
                        return (T) value;
                    }
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return defaultValue;
    }
}
