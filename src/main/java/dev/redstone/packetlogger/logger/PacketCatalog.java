package dev.redstone.packetlogger.logger;

//? if >=26.1 {
/*import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.common.CommonPacketTypes;
import net.minecraft.network.protocol.configuration.ConfigurationPacketTypes;
import net.minecraft.network.protocol.cookie.CookiePacketTypes;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.network.protocol.handshake.HandshakePacketTypes;
import net.minecraft.network.protocol.login.LoginPacketTypes;
import net.minecraft.network.protocol.ping.PingPacketTypes;
import net.minecraft.network.protocol.status.StatusPacketTypes;
*///?} else {
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.CommonPackets;
import net.minecraft.network.packet.ConfigPackets;
import net.minecraft.network.packet.CookiePackets;
import net.minecraft.network.packet.HandshakePackets;
import net.minecraft.network.packet.LoginPackets;
import net.minecraft.network.packet.PingPackets;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.StatusPackets;
//?}

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Enumeriert zur Laufzeit alle registrierten Vanilla-Pakete aus den
 * Registry-Klassen und bildet daraus die auswählbaren S2C-/C2S-Namenslisten.
 * Damit ist jedes Paket (alle Phasen) in der Config auswählbar.
 */
public final class PacketCatalog {
    private static final List<String> S2C_NAMES = collect(true);
    private static final List<String> C2S_NAMES = collect(false);

    private PacketCatalog() {
    }

    public static List<String> getS2CPacketNames() {
        return S2C_NAMES;
    }

    public static List<String> getC2SPacketNames() {
        return C2S_NAMES;
    }

    private static List<String> collect(boolean s2c) {
        Set<String> names = new LinkedHashSet<>();
        for (Class<?> registry : registryClasses()) {
            for (Field field : registry.getFields()) {
                if (!PacketType.class.isAssignableFrom(field.getType())) {
                    continue;
                }
                try {
                    PacketType<?> type = (PacketType<?>) field.get(null);
                    boolean clientbound = isClientbound(type);
                    if (clientbound != s2c) {
                        continue;
                    }
                    Class<?> packetClass = packetClass(field);
                    if (packetClass != null) {
                        names.add(PacketLogger.getPacketName(packetClass));
                    }
                } catch (IllegalAccessException e) {
                    // statische Felder sind immer zugreifbar
                }
            }
        }
        return new ArrayList<>(names);
    }

    private static boolean isClientbound(PacketType<?> type) {
        //? if >=26.1 {
        /*return type.flow() == PacketFlow.CLIENTBOUND;
        *///?} else {
        return type.side() == NetworkSide.CLIENTBOUND;
        //?}
    }

    private static Class<?> packetClass(Field field) {
        Type generic = field.getGenericType();
        if (generic instanceof ParameterizedType parameterized) {
            Type[] args = parameterized.getActualTypeArguments();
            if (args.length == 1 && args[0] instanceof Class<?> clazz) {
                return clazz;
            }
        }
        return null;
    }

    private static Class<?>[] registryClasses() {
        //? if >=26.1 {
        /*return new Class<?>[]{
            GamePacketTypes.class, CommonPacketTypes.class, LoginPacketTypes.class,
            ConfigurationPacketTypes.class, HandshakePacketTypes.class, StatusPacketTypes.class,
            PingPacketTypes.class, CookiePacketTypes.class
        };
        *///?} else {
        return new Class<?>[]{
            PlayPackets.class, CommonPackets.class, LoginPackets.class, ConfigPackets.class,
            HandshakePackets.class, StatusPackets.class, PingPackets.class, CookiePackets.class
        };
        //?}
    }
}
