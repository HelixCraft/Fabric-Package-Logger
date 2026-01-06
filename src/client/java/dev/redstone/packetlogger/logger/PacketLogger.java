package dev.redstone.packetlogger.logger;

import dev.redstone.packetlogger.config.ModConfig;
import dev.redstone.packetlogger.logger.unpacker.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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
        registerPacket(InventoryS2CPacket.class, "InventoryS2CPacket", new InventoryS2CUnpacker());
        registerPacket(ScreenHandlerSlotUpdateS2CPacket.class, "ScreenHandlerSlotUpdateS2CPacket", new SlotUpdateS2CUnpacker());
        registerPacket(CreativeInventoryActionC2SPacket.class, "CreativeInventoryActionC2SPacket", new CreativeInventoryC2SUnpacker());
        registerPacket(ClickSlotC2SPacket.class, "ClickSlotC2SPacket", new ClickSlotC2SUnpacker());

        // Block Pakete
        registerPacket(BlockEntityUpdateS2CPacket.class, "BlockEntityUpdateS2CPacket", new BlockEntityUpdateS2CUnpacker());
        registerPacket(BlockUpdateS2CPacket.class, "BlockUpdateS2CPacket", new BlockUpdateS2CUnpacker());
        registerPacket(ChunkDeltaUpdateS2CPacket.class, "ChunkDeltaUpdateS2CPacket", new ChunkDeltaUpdateS2CUnpacker());
        
        // Entity Pakete
        registerPacket(EntityTrackerUpdateS2CPacket.class, "EntityTrackerUpdateS2CPacket", new EntityTrackerUpdateS2CUnpacker());
        registerPacket(EntityAttributesS2CPacket.class, "EntityAttributesS2CPacket", new EntityAttributesS2CUnpacker());
        registerPacket(EntitySpawnS2CPacket.class, "EntitySpawnS2CPacket", new EntitySpawnS2CUnpacker());
        
        // Chunk Pakete
        registerPacket(ChunkDataS2CPacket.class, "ChunkDataS2CPacket", new ChunkDataS2CUnpacker());
        
        // NBT/Custom Pakete
        registerPacket(NbtQueryResponseS2CPacket.class, "NbtQueryResponseS2CPacket", new NbtQueryResponseS2CUnpacker());
        registerPacket(CustomPayloadS2CPacket.class, "CustomPayloadS2CPacket", new CustomPayloadS2CUnpacker());
        registerPacket(CustomPayloadC2SPacket.class, "CustomPayloadC2SPacket", new CustomPayloadC2SUnpacker());
        
        // Weitere Pakete (nur Namen-Mapping)
        registerPacketName(GameJoinS2CPacket.class, "GameJoinS2CPacket");
        registerPacketName(PlayerPositionLookS2CPacket.class, "PlayerPositionLookS2CPacket");
        registerPacketName(OpenScreenS2CPacket.class, "OpenScreenS2CPacket");
        registerPacketName(CloseScreenS2CPacket.class, "CloseScreenS2CPacket");
        registerPacketName(EntityEquipmentUpdateS2CPacket.class, "EntityEquipmentUpdateS2CPacket");
        registerPacketName(EntityPositionS2CPacket.class, "EntityPositionS2CPacket");
        registerPacketName(EntityVelocityUpdateS2CPacket.class, "EntityVelocityUpdateS2CPacket");
        registerPacketName(HealthUpdateS2CPacket.class, "HealthUpdateS2CPacket");
        registerPacketName(ExperienceBarUpdateS2CPacket.class, "ExperienceBarUpdateS2CPacket");
        registerPacketName(ChatMessageS2CPacket.class, "ChatMessageS2CPacket");
        registerPacketName(GameMessageS2CPacket.class, "GameMessageS2CPacket");
        registerPacketName(ParticleS2CPacket.class, "ParticleS2CPacket");
        registerPacketName(PlaySoundS2CPacket.class, "PlaySoundS2CPacket");
        registerPacketName(WorldTimeUpdateS2CPacket.class, "WorldTimeUpdateS2CPacket");
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
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.inGameHud == null || client.inGameHud.getChatHud() == null) return;
        
        MutableText timeText = Text.literal("[" + timestamp + "] ").formatted(Formatting.GRAY);
        MutableText dirText = Text.literal("[" + direction + "] ").formatted(incoming ? Formatting.GREEN : Formatting.RED);
        MutableText nameText = Text.literal(packetName + " ").formatted(Formatting.YELLOW);
        String shortData = packetData.length() > 300 ? packetData.substring(0, 300) + "..." : packetData;
        MutableText dataText = Text.literal(shortData).formatted(Formatting.WHITE);
        
        MutableText fullMessage = Text.empty().append(timeText).append(dirText).append(nameText).append(dataText);
        client.inGameHud.getChatHud().addMessage(fullMessage);
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
            MinecraftClient client = MinecraftClient.getInstance();
            if (client != null) {
                if (client.getCurrentServerEntry() != null) {
                    return sanitizeFileName(client.getCurrentServerEntry().address);
                }
                if (client.getServer() != null && client.getServer().getSaveProperties() != null) {
                    return sanitizeFileName(client.getServer().getSaveProperties().getLevelName());
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
