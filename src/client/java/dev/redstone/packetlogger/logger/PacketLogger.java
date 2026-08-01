package dev.redstone.packetlogger.logger;

import dev.redstone.packetlogger.config.ModConfig;
import dev.redstone.packetlogger.logger.unpacker.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Deep Packet Logger - Loggt alle Netzwerk-Pakete mit vollständigen Daten.
 */
public class PacketLogger {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private static Path currentLogFile = null;
    private static String currentSessionId = null;
    private static boolean wasLoggingEnabled = false;

    private static final Map<Class<?>, PacketUnpacker<?>> UNPACKERS = new HashMap<>();
    private static final Map<Class<?>, String> PACKET_NAMES = new HashMap<>();

    static {
        registerUnpackers();
    }

    private static void registerUnpackers() {
        // Inventory/Item Pakete
        registerPacket(ClientboundContainerSetContentPacket.class, "InventoryS2CPacket", new InventoryS2CUnpacker());
        registerPacket(ClientboundContainerSetSlotPacket.class, "ScreenHandlerSlotUpdateS2CPacket", new SlotUpdateS2CUnpacker());
        registerPacket(ServerboundSetCreativeModeSlotPacket.class, "CreativeInventoryActionC2SPacket", new CreativeInventoryC2SUnpacker());
        registerPacket(ServerboundContainerClickPacket.class, "ClickSlotC2SPacket", new ClickSlotC2SUnpacker());

        // Block Pakete
        registerPacket(ClientboundBlockEntityDataPacket.class, "BlockEntityUpdateS2CPacket", new BlockEntityUpdateS2CUnpacker());
        registerPacket(ClientboundBlockUpdatePacket.class, "BlockUpdateS2CPacket", new BlockUpdateS2CUnpacker());
        registerPacket(ClientboundSectionBlocksUpdatePacket.class, "ChunkDeltaUpdateS2CPacket", new ChunkDeltaUpdateS2CUnpacker());

        // Entity Pakete
        registerPacket(ClientboundSetEntityDataPacket.class, "EntityTrackerUpdateS2CPacket", new EntityTrackerUpdateS2CUnpacker());
        registerPacket(ClientboundUpdateAttributesPacket.class, "EntityAttributesS2CPacket", new EntityAttributesS2CUnpacker());
        registerPacket(ClientboundAddEntityPacket.class, "EntitySpawnS2CPacket", new EntitySpawnS2CUnpacker());

        // Chunk Pakete
        registerPacket(ClientboundLevelChunkWithLightPacket.class, "ChunkDataS2CPacket", new ChunkDataS2CUnpacker());

        // NBT/Custom Pakete
        registerPacket(ClientboundTagQueryPacket.class, "NbtQueryResponseS2CPacket", new NbtQueryResponseS2CUnpacker());
        registerPacket(ClientboundCustomPayloadPacket.class, "CustomPayloadS2CPacket", new CustomPayloadS2CUnpacker());
        registerPacket(ServerboundCustomPayloadPacket.class, "CustomPayloadC2SPacket", new CustomPayloadC2SUnpacker());

        // Weitere Pakete (nur Namen-Mapping)
        registerPacketName(ClientboundLoginPacket.class, "GameJoinS2CPacket");
        registerPacketName(ClientboundPlayerPositionPacket.class, "PlayerPositionLookS2CPacket");
        registerPacketName(ClientboundOpenScreenPacket.class, "OpenScreenS2CPacket");
        registerPacketName(ClientboundContainerClosePacket.class, "CloseScreenS2CPacket");
        registerPacketName(ClientboundSetEquipmentPacket.class, "EntityEquipmentUpdateS2CPacket");
        registerPacketName(ClientboundEntityPositionSyncPacket.class, "EntityPositionS2CPacket");
        registerPacketName(ClientboundSetEntityMotionPacket.class, "EntityVelocityUpdateS2CPacket");
        registerPacketName(ClientboundSetHealthPacket.class, "HealthUpdateS2CPacket");
        registerPacketName(ClientboundSetExperiencePacket.class, "ExperienceBarUpdateS2CPacket");
        registerPacketName(ClientboundPlayerChatPacket.class, "ChatMessageS2CPacket");
        registerPacketName(ClientboundSystemChatPacket.class, "GameMessageS2CPacket");
        registerPacketName(ClientboundLevelParticlesPacket.class, "ParticleS2CPacket");
        registerPacketName(ClientboundSoundPacket.class, "PlaySoundS2CPacket");
        registerPacketName(ClientboundSetTimePacket.class, "WorldTimeUpdateS2CPacket");

        // Bindung/Bewegung von Entities - fehlten in PACKET_NAMES, daher wurde ihr Mojang-Name
        // (z.B. "ClientboundSetPassengersPacket") nie gegen den Yarn-Namen im Whitelist gematcht
        // und sie tauchten NIE im Log auf. Jetzt korrekt gemappt:
        registerPacketName(ClientboundBundlePacket.class, "BundleS2CPacket");
        registerPacketName(ClientboundSetPassengersPacket.class, "EntityPassengersSetS2CPacket");
        registerPacketName(ClientboundSetEntityLinkPacket.class, "EntityAttachS2CPacket");
        registerPacketName(ClientboundTeleportEntityPacket.class, "EntityPositionS2CPacket");
        registerPacketName(ClientboundMoveEntityPacket.Pos.class, "EntityS2CPacket");
        registerPacketName(ClientboundMoveEntityPacket.Rot.class, "EntityS2CPacket");
        registerPacketName(ClientboundMoveEntityPacket.PosRot.class, "EntityS2CPacket");
        registerPacketName(ClientboundRemoveEntitiesPacket.class, "EntitiesDestroyS2CPacket");
    }

    private static <T extends Packet<?>> void registerPacket(Class<T> clazz, String name, PacketUnpacker<T> unpacker) {
        PACKET_NAMES.put(clazz, name);
        if (unpacker != null) {
            UNPACKERS.put(clazz, unpacker);
        }
    }

    private static void registerPacketName(Class<?> clazz, String name) {
        PACKET_NAMES.put(clazz, name);
    }

    public static void onWorldJoin(String worldName) {
        if (ModConfig.getInstance().logMode == ModConfig.LogMode.FILE) {
            currentSessionId = null;
            currentLogFile = null;
            System.out.println("[PacketLogger] New session started: " + worldName);
        }
    }

    public static void onWorldLeave() {
        if (currentLogFile != null && ModConfig.getInstance().logMode == ModConfig.LogMode.FILE) {
            try {
                synchronized (PacketLogger.class) {
                    try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(currentLogFile.toFile(), true)))) {
                        writer.println();
                        writer.println("=== Session ended: " + LocalDateTime.now().format(FILE_DATE_FORMAT) + " ===");
                    }
                }
            } catch (IOException e) { }
        }
        currentSessionId = null;
        currentLogFile = null;
    }

    public static void logIncoming(Packet<?> packet) {
        // BundleS2CPacket ist ein Container: der Server packt Spawn/Metadata/SetPassengers/Teleport
        // haeufig hier hinein. Er kommt als EIN Objekt durch channelRead0 - ohne Entpacken bleiben
        // alle inneren Pakete (u.a. EntityPassengersSetS2CPacket) unsichtbar. Also rekursiv entpacken.
        if (packet instanceof ClientboundBundlePacket bundle) {
            for (Packet<?> sub : bundle.subPackets()) {
                logIncoming(sub);
            }
            return;
        }
        logPacket(packet, true);
    }

    public static void logOutgoing(Packet<?> packet) {
        logPacket(packet, false);
    }

    private static void logPacket(Packet<?> packet, boolean incoming) {
        ModConfig config = ModConfig.getInstance();

        if (config.logPackets && !wasLoggingEnabled && config.logMode == ModConfig.LogMode.FILE) {
            currentSessionId = null;
            currentLogFile = null;
        }
        wasLoggingEnabled = config.logPackets;

        if (!config.logPackets) return;

        String simpleName = getDeobfuscatedName(packet);

        if (incoming) {
            if (!shouldLogS2C(simpleName, config)) return;
        } else {
            if (!shouldLogC2S(simpleName, config)) return;
        }

        String timestamp = LocalTime.now().format(TIME_FORMAT);
        String direction = incoming ? "S2C" : "C2S";
        String packetData = unpackPacket(packet);

        if (config.logMode == ModConfig.LogMode.CHAT) {
            logToChat(timestamp, direction, simpleName, packetData, incoming);
        } else {
            logToFile(timestamp, direction, simpleName, packetData);
        }
    }

    private static String getDeobfuscatedName(Packet<?> packet) {
        Class<?> clazz = packet.getClass();
        String mappedName = PACKET_NAMES.get(clazz);
        if (mappedName != null) return mappedName;

        String simpleName = clazz.getSimpleName();
        if (simpleName.contains("Packet")) return simpleName;
        return simpleName;
    }

    private static boolean shouldLogS2C(String simpleName, ModConfig config) {
        if (config.selectedS2CPackets.isEmpty()) return false;
        for (String selected : config.selectedS2CPackets) {
            if (simpleName.equals(selected) || simpleName.endsWith(selected)) return true;
        }
        return false;
    }

    private static boolean shouldLogC2S(String simpleName, ModConfig config) {
        if (config.selectedC2SPackets.isEmpty()) return false;
        for (String selected : config.selectedC2SPackets) {
            if (simpleName.equals(selected) || simpleName.endsWith(selected)) return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static String unpackPacket(Packet<?> packet) {
        try {
            PacketUnpacker<Packet<?>> unpacker = (PacketUnpacker<Packet<?>>) UNPACKERS.get(packet.getClass());
            if (unpacker != null) return unpacker.unpack(packet);
            return ReflectionUnpacker.unpackWithReflection(packet);
        } catch (Exception e) {
            return "{error: \"" + e.getMessage() + "\"}";
        }
    }

    private static void logToChat(String timestamp, String direction, String packetName, String packetData, boolean incoming) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        MutableComponent timeText = Component.literal("[" + timestamp + "] ").withStyle(ChatFormatting.GRAY);
        MutableComponent dirText = Component.literal("[" + direction + "] ").withStyle(incoming ? ChatFormatting.GREEN : ChatFormatting.RED);
        MutableComponent nameText = Component.literal(packetName + " ").withStyle(ChatFormatting.YELLOW);
        String shortData = packetData.length() > 300 ? packetData.substring(0, 300) + "..." : packetData;
        MutableComponent dataText = Component.literal(shortData).withStyle(ChatFormatting.WHITE);

        MutableComponent fullMessage = Component.empty().append(timeText).append(dirText).append(nameText).append(dataText);
        client.player.sendSystemMessage(fullMessage);
    }

    private static void logToFile(String timestamp, String direction, String packetName, String packetData) {
        try {
            Path logFile = getLogFile();
            String logLine = String.format("[%s] [%s] %s %s%n", timestamp, direction, packetName, packetData);
            synchronized (PacketLogger.class) {
                try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(logFile.toFile(), true)))) {
                    writer.print(logLine);
                }
            }
        } catch (IOException e) {
            System.err.println("[PacketLogger] Error writing to log file: " + e.getMessage());
        }
    }

    private static Path getLogFile() throws IOException {
        if (currentSessionId == null || currentLogFile == null) {
            currentSessionId = LocalDateTime.now().format(FILE_DATE_FORMAT);
            Path configDir = FabricLoader.getInstance().getConfigDir();
            Path logDir = configDir.resolve("packet-logger");
            Files.createDirectories(logDir);

            String worldName = getWorldName();
            String fileName = "packets_" + currentSessionId + "_" + worldName + ".log";
            currentLogFile = logDir.resolve(fileName);

            try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(currentLogFile.toFile())))) {
                writer.println("=== Deep Packet Logger ===");
                writer.println("Session: " + currentSessionId);
                writer.println("World: " + worldName);
                writer.println("Format: [TIME] [DIRECTION] PacketName {deep_data}");
                writer.println("==========================================");
                writer.println();
            }
            System.out.println("[PacketLogger] Created new log file: " + fileName);
        }
        return currentLogFile;
    }

    private static String getWorldName() {
        try {
            Minecraft client = Minecraft.getInstance();
            if (client != null) {
                if (client.getCurrentServer() != null) {
                    return sanitizeFileName(client.getCurrentServer().ip);
                }
                if (client.getSingleplayerServer() != null && client.getSingleplayerServer().getWorldData() != null) {
                    return sanitizeFileName(client.getSingleplayerServer().getWorldData().getLevelName());
                }
            }
        } catch (Exception e) { }
        return "unknown";
    }

    private static String sanitizeFileName(String name) {
        if (name == null) return "unknown";
        return name.replaceAll("[^a-zA-Z0-9._-]", "_").replaceAll("_+", "_").substring(0, Math.min(name.length(), 50));
    }
}
