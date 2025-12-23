# Render Tweaks - Minecraft Fabric Mod

Eine Minecraft 1.21.4 Fabric-Mod mit professionellem Config-Screen und vollwertigem Color Picker.

## Features

### Config-Screen
- **Command**: `/configscreen` - Öffnet den Config-Screen jederzeit im Spiel
- **Persistente Speicherung**: Alle Einstellungen werden automatisch als JSON gespeichert
- **Verschiedene Widget-Typen**:
  - Checkboxen für Boolean-Werte
  - Slider für Integer und Float-Werte
  - Text-Input-Felder für Strings
  - **Vollwertiger Color Picker** (siehe unten)

### Color Picker Widget

Ein professionelles Farbauswahl-Widget mit allen Features moderner Grafiksoftware:

✅ **HSV-Farbfläche** - Visuelle Auswahl von Sättigung und Helligkeit  
✅ **Hue-Slider** - Farbton-Auswahl (0-360°)  
✅ **Alpha-Slider** - Transparenz-Kontrolle mit Schachbrett-Hintergrund  
✅ **RGB-Slider** - Einzelne Kontrolle für Rot, Grün, Blau (0-255)  
✅ **Hex-Anzeige** - Live-Anzeige des Hex-Codes (#AARRGGBB)  
✅ **Live-Vorschau** - Echtzeit-Vorschau der gewählten Farbe  
✅ **Bidirektionale Synchronisation** - Alle Eingabemodi aktualisieren sich gegenseitig

**Wichtig**: Dieser Color Picker ist komplett selbst implementiert und verwendet **NICHT** Cloth Config!

## Installation

### Voraussetzungen
- Minecraft 1.21.4
- Fabric Loader 0.17.2+
- Fabric API 0.119.4+
- Java 21

### Build

```bash
./gradlew build
```

Die fertige Mod-Datei findest du in: `build/libs/render-tweaks-1.0.0.jar`

### Installation im Spiel

1. Installiere Fabric Loader für Minecraft 1.21.4
2. Kopiere die JAR-Datei in den `mods`-Ordner
3. Starte Minecraft

## Verwendung

### Config-Screen öffnen

Im Spiel:
```
/configscreen
```

### Konfiguration

Die Config wird automatisch gespeichert unter:
```
.minecraft/config/render-tweaks-config.json
```

Beispiel-Config:
```json
{
  "exampleBoolean": true,
  "enableFeature": false,
  "exampleInt": 50,
  "exampleFloat": 0.75,
  "exampleString": "Hello World",
  "primaryColor": -65536,
  "secondaryColor": -2147418368,
  "accentColor": -16744193
}
```

**Hinweis**: Farben werden als ARGB-Integer gespeichert (z.B. -65536 = 0xFFFF0000 = Rot).

## Entwicklung

### Projekt-Struktur

```
src/client/java/dev/redstone/rendertweaks/
├── RenderTweaksClient.java          # Client-Entrypoint
├── command/
│   └── ConfigCommand.java           # /configscreen Command
├── config/
│   └── ModConfig.java               # Config-Datenmodell
└── gui/
    ├── ConfigScreen.java            # Haupt-Config-Screen
    └── widget/
        └── ColorPickerWidget.java   # Color Picker Widget
```

### Neue Config-Werte hinzufügen

1. **Feld in ModConfig.java hinzufügen**:
```java
public int myNewValue = 42;
```

2. **Widget in ConfigScreen.java erstellen**:
```java
SliderWidget mySlider = new SliderWidget(...);
this.addDrawableChild(mySlider);
```

3. **Wert beim Speichern übernehmen**:
```java
config.myNewValue = (int)(mySlider.value * 100);
config.save();
```

### Neuen Color Picker hinzufügen

```java
// In ModConfig.java
public int myColor = 0xFFFF0000;

// In ConfigScreen.java
ColorPickerWidget myPicker = new ColorPickerWidget(
    x, y, 200, 150,
    config.myColor,
    color -> config.myColor = color
);
this.addDrawableChild(myPicker);
```

## Dokumentation

- **[ARCHITECTURE.md](ARCHITECTURE.md)** - Detaillierte Architektur-Dokumentation
- **[COLOR_PICKER_USAGE.md](COLOR_PICKER_USAGE.md)** - Color Picker Verwendung & Integration

## Technische Details

### Farbformat

Der Color Picker verwendet 32-bit ARGB-Integers:

```
0xAARRGGBB
  ││││││└└─ Blau  (0-255)
  ││││└└─── Grün  (0-255)
  ││└└───── Rot   (0-255)
  └└─────── Alpha (0-255)
```

### HSV-Farbmodell

Intern arbeitet der Color Picker mit HSV (Hue, Saturation, Value):
- **Hue**: Farbton (0-360°)
- **Saturation**: Sättigung (0-1)
- **Value**: Helligkeit (0-1)

Die Konvertierung zwischen RGB und HSV erfolgt automatisch.

## Lizenz

CC0-1.0 (Public Domain)

## Credits

- Fabric API Team
- Minecraft Modding Community

## Support

Bei Fragen oder Problemen:
1. Prüfe die Dokumentation in `ARCHITECTURE.md`
2. Schaue in `COLOR_PICKER_USAGE.md` für Color Picker Details
3. Erstelle ein Issue auf GitHub

## Roadmap

Mögliche zukünftige Features:
- [ ] Scrollbarer Config-Screen
- [ ] Kategorien/Tabs für Organisation
- [ ] Farb-Presets
- [ ] Import/Export von Configs
- [ ] Keybind-Widgets
- [ ] Dropdown-Menüs
- [ ] Tooltip-System

---

**Viel Spaß mit der Mod!** 🎨
