package dev.redstone.packetlogger.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Zentrale Konfigurationsklasse für die Mod.
 * Speichert alle Einstellungen persistent als JSON.
 */
public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("packet-logger-config.json");
    
    private static ModConfig INSTANCE;
    
    // Package Logger Settings
    public boolean logPackages = true;
    public LogMode logMode = LogMode.CHAT;
    public boolean deepLogging = true; // Detailliertes Logging mit NBT-Daten
    public List<String> selectedS2CPackages = new ArrayList<>();
    public List<String> selectedC2SPackages = new ArrayList<>();
    
    public enum LogMode {
        CHAT("Chat"),
        FILE("File");
        
        private final String displayName;
        
        LogMode(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public LogMode next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }
    
    /**
     * Lädt die Konfiguration aus der Datei oder erstellt eine neue.
     */
    public static ModConfig load() {
        if (INSTANCE == null) {
            if (Files.exists(CONFIG_PATH)) {
                try {
                    String json = Files.readString(CONFIG_PATH);
                    INSTANCE = GSON.fromJson(json, ModConfig.class);
                    if (INSTANCE.selectedS2CPackages == null) {
                        INSTANCE.selectedS2CPackages = new ArrayList<>();
                    }
                    if (INSTANCE.selectedC2SPackages == null) {
                        INSTANCE.selectedC2SPackages = new ArrayList<>();
                    }
                } catch (IOException e) {
                    System.err.println("Fehler beim Laden der Config: " + e.getMessage());
                    INSTANCE = new ModConfig();
                }
            } else {
                INSTANCE = new ModConfig();
                INSTANCE.save();
            }
        }
        return INSTANCE;
    }
    
    /**
     * Speichert die aktuelle Konfiguration in die Datei.
     */
    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            String json = GSON.toJson(this);
            Files.writeString(CONFIG_PATH, json);
        } catch (IOException e) {
            System.err.println("Fehler beim Speichern der Config: " + e.getMessage());
        }
    }
    
    public static ModConfig getInstance() {
        if (INSTANCE == null) {
            return load();
        }
        return INSTANCE;
    }
}
