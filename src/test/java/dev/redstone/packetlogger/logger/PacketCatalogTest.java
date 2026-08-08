package dev.redstone.packetlogger.logger;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Version-agnostischer Test: leitet die Registry-Klassen über dieselben
 * Klassennamen wie PacketCatalog ab (Yarn: *Packets, 26.x: *PacketTypes),
 * damit er in beiden Mapping-Sets kompiliert.
 */
class PacketCatalogTest {

    private static final String[][] REGISTRY_NAMES = {
        {"net.minecraft.network.packet.PlayPackets", "net.minecraft.network.protocol.game.GamePacketTypes"},
        {"net.minecraft.network.packet.CommonPackets", "net.minecraft.network.protocol.common.CommonPacketTypes"},
        {"net.minecraft.network.packet.LoginPackets", "net.minecraft.network.protocol.login.LoginPacketTypes"},
        {"net.minecraft.network.packet.ConfigPackets", "net.minecraft.network.protocol.configuration.ConfigurationPacketTypes"},
        {"net.minecraft.network.packet.HandshakePackets", "net.minecraft.network.protocol.handshake.HandshakePacketTypes"},
        {"net.minecraft.network.packet.StatusPackets", "net.minecraft.network.protocol.status.StatusPacketTypes"},
        {"net.minecraft.network.packet.PingPackets", "net.minecraft.network.protocol.ping.PingPacketTypes"},
        {"net.minecraft.network.packet.CookiePackets", "net.minecraft.network.protocol.cookie.CookiePacketTypes"}
    };

    private static List<Class<?>> registryClasses() {
        List<Class<?>> result = new ArrayList<>();
        for (String[] pair : REGISTRY_NAMES) {
            Class<?> clazz = null;
            for (String name : pair) {
                try {
                    clazz = Class.forName(name);
                    break;
                } catch (ClassNotFoundException ignored) {
                }
            }
            if (clazz != null) result.add(clazz);
        }
        return result;
    }

    private static boolean isClientbound(Class<?> registry, Field field, Class<?> packetClass) {
        try {
            Object type = field.get(null);
            String dir = String.valueOf(type);
            // toString liefert "clientbound/..." bzw. "CLIENTBOUND" je nach Mapping-Set
            return dir.toLowerCase().contains("clientbound");
        } catch (IllegalAccessException e) {
            return false;
        }
    }

    @Test
    void catalogCoversAllRegisteredPackets() throws Exception {
        List<Class<?>> registries = registryClasses();
        assertFalse(registries.isEmpty(), "Keine Registry-Klassen gefunden");

        Set<Class<?>> allPacketClasses = new LinkedHashSet<>();
        for (Class<?> registry : registries) {
            for (Field field : registry.getFields()) {
                if (field.getType().getName().contains("PacketType")) {
                    Type generic = field.getGenericType();
                    if (generic instanceof ParameterizedType pt) {
                        Type[] args = pt.getActualTypeArguments();
                        if (args.length == 1 && args[0] instanceof Class<?> clazz) {
                            allPacketClasses.add(clazz);
                        }
                    }
                }
            }
        }

        List<String> s2c = PacketCatalog.getS2CPacketNames();
        List<String> c2s = PacketCatalog.getC2SPacketNames();

        for (Class<?> packetClass : allPacketClasses) {
            String name = PacketLogger.getPacketName(packetClass);
            assertFalse(name.isEmpty(), "Name darf nicht leer sein: " + packetClass);
            boolean clientbound = false;
            for (Class<?> registry : registries) {
                for (Field field : registry.getFields()) {
                    if (!field.getType().getName().contains("PacketType")) continue;
                    Type generic = field.getGenericType();
                    if (generic instanceof ParameterizedType pt) {
                        Type[] args = pt.getActualTypeArguments();
                        if (args.length == 1 && args[0] == packetClass) {
                            clientbound = isClientbound(registry, field, packetClass);
                        }
                    }
                }
            }
            if (clientbound) {
                assertTrue(s2c.contains(name), "S2C-Paket fehlt: " + name + " (" + packetClass + ")");
            } else {
                assertTrue(c2s.contains(name), "C2S-Paket fehlt: " + name + " (" + packetClass + ")");
            }
        }

        assertTrue(new LinkedHashSet<>(s2c).size() == s2c.size(), "Duplikate in S2C-Liste");
        assertTrue(new LinkedHashSet<>(c2s).size() == c2s.size(), "Duplikate in C2S-Liste");
    }

    @Test
    void everyPacketClassHasValidName() throws Exception {
        for (Class<?> registry : registryClasses()) {
            for (Field field : registry.getFields()) {
                if (!field.getType().getName().contains("PacketType")) continue;
                Type generic = field.getGenericType();
                if (!(generic instanceof ParameterizedType pt)) continue;
                Type[] args = pt.getActualTypeArguments();
                if (args.length == 1 && args[0] instanceof Class<?> clazz) {
                    String name = PacketLogger.getPacketName(clazz);
                    assertFalse(name == null || name.isEmpty(), "Kein Name für " + clazz);
                }
            }
        }
    }
}
