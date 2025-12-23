package dev.redstone.rendertweaks.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Zentrale Konfigurationsklasse für die Mod.
 * Speichert alle Einstellungen persistent als JSON.
 */
public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("render-tweaks-config.json");
    
    private static ModConfig INSTANCE;
    
    // Beispiel-Konfigurationswerte
    public boolean exampleBoolean = true;
    public boolean enableFeature = false;
    public int exampleInt = 50;
    public float exampleFloat = 0.75f;
    public String exampleString = "Hello World";
    
    // Color Picker Werte (ARGB Format)
    public int primaryColor = 0xFFFF0000; // Rot mit voller Deckkraft
    public int secondaryColor = 0x8000FF00; // Grün mit 50% Deckkraft
    public int accentColor = 0xFF0080FF; // Blau
    
    /**
     * Lädt die Konfiguration aus der Datei oder erstellt eine neue.
     */
    public static ModConfig load() {
        if (INSTANCE == null) {
            if (Files.exists(CONFIG_PATH)) {
                try {
                    String json = Files.readString(CONFIG_PATH);
                    INSTANCE = GSON.fromJson(json, ModConfig.class);
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
