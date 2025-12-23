# Render Tweaks - Architektur-Dokumentation

## Übersicht

Diese Minecraft Fabric-Mod (1.21.4) bietet einen vollwertigen Config-Screen mit professionellem Color Picker.

## Architektur

### 1. Config-System (`config/ModConfig.java`)

**Zweck**: Zentrale Datenhaltung und Persistenz

**Features**:
- Singleton-Pattern für globalen Zugriff
- JSON-Serialisierung mit Gson
- Automatisches Laden/Speichern in `config/render-tweaks-config.json`
- Typsichere Konfigurationswerte

**Unterstützte Datentypen**:
- `boolean` - Checkboxen
- `int` - Integer-Slider
- `float` - Float-Slider
- `String` - Text-Input
- `int` (ARGB) - Color Picker

### 2. Command-System (`command/ConfigCommand.java`)

**Zweck**: Client-seitiger Command `/configscreen`

**Implementation**:
- Nutzt Fabric's `ClientCommandRegistrationCallback`
- Thread-sicher durch `client.execute()`
- Öffnet Config-Screen von überall im Spiel

### 3. Config-Screen (`gui/ConfigScreen.java`)

**Zweck**: Haupt-UI für alle Einstellungen

**Komponenten**:
- Checkboxen für Boolean-Werte
- Slider für numerische Werte (int/float)
- TextFieldWidget für String-Eingabe
- ColorPickerWidget für Farbauswahl
- Save & Cancel Buttons

**Layout**:
- Zentriert, scrollbar (erweiterbar)
- Klare Beschriftungen
- Logische Gruppierung

### 4. Color Picker Widget (`gui/widget/ColorPickerWidget.java`)

**Zweck**: Vollwertiges Farbauswahl-Widget

#### Features im Detail:

##### 4.1 HSV-Farbfläche (Saturation/Value Picker)
- 100x100px Farbfläche
- Zeigt alle Sättigungs- und Helligkeitswerte für aktuellen Hue
- Visueller Auswahlkreis mit Kontrast-Border
- Drag & Drop Interaktion

##### 4.2 Hue-Slider
- Horizontaler Gradient durch alle Farbtöne (0-360°)
- Slider-Handle mit weißem/schwarzem Border
- Aktualisiert SV-Fläche in Echtzeit

##### 4.3 Alpha-Slider
- Schachbrett-Hintergrund für Transparenz-Visualisierung
- Gradient von transparent zu opak
- Separate Alpha-Kontrolle unabhängig von RGB

##### 4.4 RGB-Slider (3x)
- Einzelne Slider für Rot, Grün, Blau
- Zeigt aktuellen Wert (0-255)
- Gradient-Visualisierung pro Kanal
- Bidirektionale Synchronisation mit HSV

##### 4.5 Hex-Input & Vorschau
- Hex-Code-Anzeige im Format #AARRGGBB
- Vorschau-Box mit Schachbrett-Hintergrund
- Live-Update bei jeder Änderung

#### Technische Details:

**Farbmodell-Konvertierung**:
```java
HSV -> RGB: hsvToRgb(float h, float s, float v)
RGB -> HSV: rgbToHsv(int r, int g, int b)
```

**Interaktionszustände**:
- Separate Drag-Flags für jedes interaktive Element
- `mouseClicked()` - Startet Interaktion
- `mouseDragged()` - Kontinuierliche Updates
- `mouseReleased()` - Beendet Interaktion

**Callback-System**:
```java
Consumer<Integer> onColorChanged
```
- Wird bei jeder Farbänderung aufgerufen
- Übergibt ARGB-Wert (32-bit Integer)
- Ermöglicht Live-Updates in Config

## Datenfluss

```
User Input (Widget)
    ↓
ColorPickerWidget (HSV-Modell)
    ↓
onColorChanged Callback
    ↓
ModConfig (ARGB-Speicherung)
    ↓
JSON-Datei (Persistenz)
```

## Erweiterbarkeit

### Neue Config-Werte hinzufügen:

1. **ModConfig.java**: Feld hinzufügen
```java
public int newValue = 42;
```

2. **ConfigScreen.java**: Widget erstellen
```java
SliderWidget newSlider = new SliderWidget(...);
this.addDrawableChild(newSlider);
```

3. **ConfigScreen.saveAndClose()**: Wert speichern
```java
config.newValue = (int)(newSlider.value * 100);
```

### Neuen Color Picker hinzufügen:

```java
// In ModConfig.java
public int customColor = 0xFFFFFFFF;

// In ConfigScreen.java
ColorPickerWidget customPicker = new ColorPickerWidget(
    x, y, width, height,
    config.customColor,
    color -> config.customColor = color
);
this.addDrawableChild(customPicker);
```

## Build & Test

```bash
# Build
./gradlew build

# Run Client
./gradlew runClient
```

Im Spiel: `/configscreen` eingeben

## Vorteile dieser Implementierung

1. **Keine externen UI-Libraries**: Reine Minecraft/Fabric API
2. **Vollständige Kontrolle**: Eigenes Widget, keine Einschränkungen
3. **Professionell**: Alle Features moderner Color Picker
4. **Erweiterbar**: Einfach neue Widgets/Werte hinzufügen
5. **Performant**: Effiziente Rendering-Methoden
6. **Type-Safe**: Starke Typisierung in Java
7. **Persistent**: Automatisches Speichern/Laden

## Vergleich zu Cloth Config

| Feature | Cloth Config | Diese Implementation |
|---------|--------------|---------------------|
| Color Picker | Nur Hex-Input | Vollwertiges Widget |
| HSV-Fläche | ❌ | ✅ |
| Alpha-Slider | ❌ | ✅ |
| RGB-Slider | ❌ | ✅ |
| Live-Vorschau | ❌ | ✅ |
| Abhängigkeiten | Externe Lib | Nur Fabric API |
| Anpassbarkeit | Begrenzt | Vollständig |

## Zukünftige Erweiterungen

- Scrollbarer Config-Screen für viele Optionen
- Kategorien/Tabs für Organisation
- Preset-System für Farben
- Import/Export von Configs
- Keybind-Widgets
- Dropdown-Menüs
