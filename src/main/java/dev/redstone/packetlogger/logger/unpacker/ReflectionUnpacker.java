package dev.redstone.packetlogger.logger.unpacker;

//? if >=26.1 {
/*import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
*///?} else {
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
//?}

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Reflection-basierter Unpacker für Pakete ohne spezialisierten Unpacker.
 * Liest alle Felder rekursiv aus und formatiert sie.
 */
public class ReflectionUnpacker {
    
    private static final int MAX_DEPTH = 12;
    private static final int MAX_COLLECTION_SIZE = 100;
    
    public static String unpackWithReflection(Object obj) {
        return unpackWithReflection(obj, 0, new HashSet<>());
    }
    
    private static String unpackWithReflection(Object obj, int depth, Set<Integer> visited) {
        if (obj == null) return "null";
        if (depth > MAX_DEPTH) return "...";
        
        // Zyklus-Erkennung
        int hash = System.identityHashCode(obj);
        if (visited.contains(hash)) return "<circular>";
        visited.add(hash);
        
        try {
            // Spezielle Typen zuerst
            if (obj instanceof ItemStack) {
                return ItemStackFormatter.format((ItemStack) obj);
            }
            //? if >=26.1 {
            /*if (obj instanceof CompoundTag) {
                return ((CompoundTag) obj).toString();
            }
            if (obj instanceof Tag) {
                return ((Tag) obj).toString();
            }
            if (obj instanceof Component) {
                return formatComponent((Component) obj);
            }
            *///?} elif >=1.21.5 {
            /*if (obj instanceof NbtCompound) {
                return ((NbtCompound) obj).asString().orElse(obj.toString());
            }
            if (obj instanceof NbtElement) {
                return ((NbtElement) obj).asString().orElse(obj.toString());
            }
            if (obj instanceof Text) {
                return "\"" + escapeString(((Text) obj).getString()) + "\"";
            }
            *///?} else {
            if (obj instanceof NbtCompound) {
                return ((NbtCompound) obj).asString();
            }
            if (obj instanceof NbtElement) {
                return ((NbtElement) obj).asString();
            }
            if (obj instanceof Text) {
                return "\"" + escapeString(((Text) obj).getString()) + "\"";
            }
            //?}
            if (obj instanceof BlockPos) {
                BlockPos pos = (BlockPos) obj;
                return "{x:" + pos.getX() + ",y:" + pos.getY() + ",z:" + pos.getZ() + "}";
            }
            //? if >=26.1 {
            /*if (obj instanceof Vec3) {
                Vec3 vec = (Vec3) obj;
                return "{x:" + vec.x + ",y:" + vec.y + ",z:" + vec.z + "}";
            }
            if (obj instanceof ChunkPos) {
                ChunkPos pos = (ChunkPos) obj;
                return "{x:" + pos.x() + ",z:" + pos.z() + "}";
            }
            *///?} else {
            if (obj instanceof Vec3d) {
                Vec3d vec = (Vec3d) obj;
                return "{x:" + vec.x + ",y:" + vec.y + ",z:" + vec.z + "}";
            }
            if (obj instanceof ChunkPos) {
                ChunkPos pos = (ChunkPos) obj;
                return "{x:" + pos.x + ",z:" + pos.z + "}";
            }
            //?}
            if (obj instanceof UUID) {
                return "\"" + obj.toString() + "\"";
            }
            if (obj instanceof Enum) {
                return "\"" + ((Enum<?>) obj).name() + "\"";
            }
            if (obj instanceof String) {
                return "\"" + escapeString((String) obj) + "\"";
            }
            if (obj instanceof Number || obj instanceof Boolean) {
                return obj.toString();
            }
            
            // Arrays
            if (obj.getClass().isArray()) {
                return formatArray(obj, depth, visited);
            }
            
            // Collections
            if (obj instanceof Collection) {
                return formatCollection((Collection<?>) obj, depth, visited);
            }
            
            // Maps
            if (obj instanceof Map) {
                return formatMap((Map<?, ?>) obj, depth, visited);
            }
            
            // Optional
            if (obj instanceof Optional) {
                Optional<?> opt = (Optional<?>) obj;
                return opt.map(o -> unpackWithReflection(o, depth + 1, visited)).orElse("empty");
            }
            
            // Generische Objekte via Reflection
            return formatObject(obj, depth, visited);
            
        } catch (Exception e) {
            return "{error:\"" + escapeString(e.getMessage()) + "\"}";
        } finally {
            visited.remove(hash);
        }
    }
    
    private static String formatArray(Object array, int depth, Set<Integer> visited) {
        int len = java.lang.reflect.Array.getLength(array);
        if (len == 0) return "[]";
        if (len > MAX_COLLECTION_SIZE) {
            return "[" + len + " items, truncated]";
        }
        
        // Byte-Arrays als Hex
        if (array instanceof byte[]) {
            byte[] bytes = (byte[]) array;
            if (bytes.length > 64) {
                return "byte[" + bytes.length + "]";
            }
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < bytes.length; i++) {
                if (i > 0) sb.append(",");
                sb.append(String.format("%02X", bytes[i]));
            }
            sb.append("]");
            return sb.toString();
        }
        
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < len; i++) {
            if (i > 0) sb.append(",");
            sb.append(unpackWithReflection(java.lang.reflect.Array.get(array, i), depth + 1, visited));
        }
        sb.append("]");
        return sb.toString();
    }
    
    private static String formatCollection(Collection<?> col, int depth, Set<Integer> visited) {
        if (col.isEmpty()) return "[]";
        if (col.size() > MAX_COLLECTION_SIZE) {
            return "[" + col.size() + " items, truncated]";
        }
        
        StringBuilder sb = new StringBuilder("[");
        int i = 0;
        for (Object item : col) {
            if (i > 0) sb.append(",");
            sb.append(unpackWithReflection(item, depth + 1, visited));
            i++;
        }
        sb.append("]");
        return sb.toString();
    }
    
    private static String formatMap(Map<?, ?> map, int depth, Set<Integer> visited) {
        if (map.isEmpty()) return "{}";
        if (map.size() > MAX_COLLECTION_SIZE) {
            return "{" + map.size() + " entries, truncated}";
        }
        
        StringBuilder sb = new StringBuilder("{");
        int i = 0;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (i > 0) sb.append(",");
            sb.append(unpackWithReflection(entry.getKey(), depth + 1, visited));
            sb.append(":");
            sb.append(unpackWithReflection(entry.getValue(), depth + 1, visited));
            i++;
        }
        sb.append("}");
        return sb.toString();
    }
    
    private static String formatObject(Object obj, int depth, Set<Integer> visited) {
        StringBuilder sb = new StringBuilder("{");
        List<String> fields = new ArrayList<>();
        
        Class<?> clazz = obj.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) continue;
                
                try {
                    field.setAccessible(true);
                    Object value = field.get(obj);
                    String formatted = unpackWithReflection(value, depth + 1, visited);
                    fields.add(field.getName() + ":" + formatted);
                } catch (Exception e) {
                    // Skip inaccessible fields
                }
            }
            clazz = clazz.getSuperclass();
        }
        
        sb.append(String.join(",", fields));
        sb.append("}");
        return sb.toString();
    }
    
    //? if >=26.1 {
    /*/^*
     * Formatiert eine Component inkl. Style (Farbe, Formatierung) und Siblings.
     * getString() allein verwirft den Style — daher hier manuell ausgelesen.
     ^/
    private static String formatComponent(Component component) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("text:\"").append(escapeString(component.getContents() instanceof net.minecraft.network.chat.contents.PlainTextContents
                ? ((net.minecraft.network.chat.contents.PlainTextContents) component.getContents()).text()
                : component.getString())).append("\"");

        net.minecraft.network.chat.Style style = component.getStyle();
        net.minecraft.network.chat.TextColor color = style.getColor();
        if (color != null) {
            sb.append(",color:\"").append(color.serialize()).append("\"");
        }
        if (style.isBold())          sb.append(",bold:true");
        if (style.isItalic())        sb.append(",italic:true");
        if (style.isUnderlined())    sb.append(",underlined:true");
        if (style.isStrikethrough()) sb.append(",strikethrough:true");
        if (style.isObfuscated())    sb.append(",obfuscated:true");

        List<Component> siblings = component.getSiblings();
        if (siblings != null && !siblings.isEmpty()) {
            List<String> parts = new ArrayList<>();
            for (Component sibling : siblings) {
                parts.add(formatComponent(sibling));
            }
            sb.append(",extra:[").append(String.join(",", parts)).append("]");
        }

        sb.append("}");
        return sb.toString();
    }
    *///?}

    private static String escapeString(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
