package dev.redstone.packetlogger.logger.unpacker;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

/**
 * Formatiert Minecraft-Textobjekte inklusive Farben und Stilinformationen.
 */
public final class TextFormatter {
    private TextFormatter() {
    }

    public static String format(Text text) {
        if (text == null) {
            return "null";
        }

        try {
            JsonElement json = TextCodecs.CODEC.encodeStart(JsonOps.INSTANCE, text).result().orElse(null);
            if (json != null) {
                return json.toString();
            }
        } catch (Exception ignored) {
        }

        return formatPlainString(text.getString());
    }

    public static String formatPlainString(String value) {
        return "\"" + escapeString(value) + "\"";
    }

    public static String escapeString(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
