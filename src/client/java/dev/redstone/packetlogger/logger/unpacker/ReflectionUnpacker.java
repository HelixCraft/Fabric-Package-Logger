package dev.redstone.packetlogger.logger.unpacker;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Reflection-basierter Unpacker für Pakete ohne spezialisierten Unpacker.
 * Liest alle Felder rekursiv aus und formatiert sie.
 */
public class ReflectionUnpacker {

    private static final int MAX_DEPTH = 5;
    private static final int MAX_COLLECTION_SIZE = 100;

    public static String unpackWithReflection(Object obj) {
        return unpackWithReflection(obj, 0, new HashSet<>());
    }

    private static String unpackWithReflection(Object obj, int depth, Set<Integer> visited) {
        if (obj == null)
            return "null";
        if (depth > MAX_DEPTH)
            return "...";

        // Zyklus-Erkennung
        int hash = System.identityHashCode(obj);
        if (visited.contains(hash))
            return "<circular>";
        visited.add(hash);

        try {
            // Spezielle Typen zuerst
            if (obj instanceof ItemStack) {
                return ItemStackFormatter.format((ItemStack) obj);
            }
            if (obj instanceof NbtCompound) {
                // 1.21.5+: asString() returns Optional<String>
                return ((NbtCompound) obj).asString().orElse(obj.toString());
            }
            if (obj instanceof NbtElement) {
                // 1.21.5+: asString() returns Optional<String>
                return ((NbtElement) obj).asString().orElse(obj.toString());
            }
            if (obj instanceof Text) {
                return "\"" + escapeString(((Text) obj).getString()) + "\"";
            }
            if (obj instanceof BlockPos) {
                BlockPos pos = (BlockPos) obj;
                return "{x:" + pos.getX() + ",y:" + pos.getY() + ",z:" + pos.getZ() + "}";
            }
            if (obj instanceof Vec3d) {
                Vec3d vec = (Vec3d) obj;
                return "{x:" + vec.x + ",y:" + vec.y + ",z:" + vec.z + "}";
            }
            if (obj instanceof ChunkPos) {
                ChunkPos pos = (ChunkPos) obj;
                return "{x:" + pos.x + ",z:" + pos.z + "}";
            }
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
        if (len == 0)
            return "[]";
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
                if (i > 0)
                    sb.append(",");
                sb.append(String.format("%02X", bytes[i]));
            }
            sb.append("]");
            return sb.toString();
        }

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < len; i++) {
            if (i > 0)
                sb.append(",");
            sb.append(unpackWithReflection(java.lang.reflect.Array.get(array, i), depth + 1, visited));
        }
        sb.append("]");
        return sb.toString();
    }

    private static String formatCollection(Collection<?> col, int depth, Set<Integer> visited) {
        if (col.isEmpty())
            return "[]";
        if (col.size() > MAX_COLLECTION_SIZE) {
            return "[" + col.size() + " items, truncated]";
        }

        StringBuilder sb = new StringBuilder("[");
        int i = 0;
        for (Object item : col) {
            if (i > 0)
                sb.append(",");
            sb.append(unpackWithReflection(item, depth + 1, visited));
            i++;
        }
        sb.append("]");
        return sb.toString();
    }

    private static String formatMap(Map<?, ?> map, int depth, Set<Integer> visited) {
        if (map.isEmpty())
            return "{}";
        if (map.size() > MAX_COLLECTION_SIZE) {
            return "{" + map.size() + " entries, truncated}";
        }

        StringBuilder sb = new StringBuilder("{");
        int i = 0;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (i > 0)
                sb.append(",");
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
                if (Modifier.isStatic(field.getModifiers()))
                    continue;

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

    private static String escapeString(String s) {
        if (s == null)
            return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
