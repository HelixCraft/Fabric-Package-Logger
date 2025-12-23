# Render Tweaks - Vollständige Architektur-Dokumentation

## ✅ Erfolgreich implementiert!

Eine vollständige Minecraft 1.21.4 Fabric-Mod mit professionellem Config-Screen und Color Picker.

## Architektur-Übersicht

```
src/client/java/dev/redstone/rendertweaks/
├── RenderTweaksClient.java          # Client-Entrypoint mit Keybinding
├── config/
│   └── ModConfig.java                # Persistente Konfiguration (JSON)
└── screen/
    ├── SimpleConfigScreen.java       # Haupt-Config-Screen
    └── widget/
        └── ColorPickerWidget.java    # Vollwertiger Color Picker
```

## 1. Client-Entrypoint (`RenderTweaksClient.java`)

**Zweck**: Initialisierung der Mod

**Features**:
- Lädt Konfiguration beim Start
- Registriert Keybinding (R-Taste)
- Öffnet Config-Screen via Keybinding

**Warum Keybinding statt Command?**
- Zuverlässiger als Commands
- Direkter Zugriff im Client-Tick-Event
- Keine Probleme mit Screen-Rendering

```java
// Keybinding registrieren
configKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
    "key.render-tweaks.config",
    InputUtil.Type.KEYSYM,
    GLFW.GLFW_KEY_R,
    "category.render-tweaks"
));

// Screen öffnen bei Tastendruck
ClientTickEvents.END_CLIENT_TICK.register(client -> {
    while (configKeyBinding.wasPressed()) {
        if (client.currentScreen == null) {
            client.setScreen(new SimpleConfigScreen(null));
        }
    }
});
```

## 2. Config-System (`ModConfig.java`)

**Zweck**: Persistente Datenhaltung

**Features**:
- Singleton-Pattern
- JSON-Serialisierung mit Gson
- Automatisches Laden/Speichern
- Typsichere Konfigurationswerte

**Gespeicherte Werte**:
```java
public boolean exampleBoolean = true;
public boolean enableFeature = false;
public int exampleInt = 50;              // 0-100
public float exampleFloat = 0.75f;       // 0.0-1.0
public String exampleString = "Hello World";
public int primaryColor = 0xFFFF0000;    // ARGB
public int secondaryColor = 0x8000FF00;  // ARGB mit Alpha
```

**Speicherort**: `.minecraft/config/render-tweaks-config.json`

## 3. Config-Screen (`SimpleConfigScreen.java`)

**Zweck**: Haupt-UI für alle Einstellungen

**Layout**: Zwei-Spalten-Design
- **Linke Spalte**: Standard-Widgets (Checkboxen, Slider, Text-Input)
- **Rechte Spalte**: Color Pickers

**Widgets**:
1. **Checkboxen** - Boolean-Werte
2. **Integer-Slider** - 0-100 mit Live-Update
3. **Float-Slider** - 0.0-1.0 mit 2 Dezimalstellen
4. **Text-Input** - String mit max. 100 Zeichen
5. **Color Pickers** - 2x vollwertige Farbauswahl

**Buttons**:
- **Save & Close** - Speichert alle Änderungen
- **Cancel** - Verwirft Änderungen

## 4. Color Picker Widget (`ColorPickerWidget.java`)

### 🎨 Das Herzstück der Mod!

**Zweck**: Professionelle Farbauswahl ohne externe Libraries

### Features im Detail:

#### 4.1 HSV-Farbfläche (100x100px)
- **Saturation** (X-Achse): 0-100%
- **Value/Helligkeit** (Y-Achse): 0-100%
- **Hue** wird separat gesteuert
- Visueller Auswahlkreis mit Kontrast-Border
- Drag & Drop Interaktion

```java
// Pixel-für-Pixel Rendering
for (int py = 0; py < SV_SIZE; py++) {
    for (int px = 0; px < SV_SIZE; px++) {
        float s = px / (float)SV_SIZE;
        float v = 1.0f - (py / (float)SV_SIZE);
        int[] rgb = hsvToRgb(hue, s, v);
        // Zeichne Pixel
    }
}
```

#### 4.2 Hue-Slider
- Horizontaler Gradient durch alle Farbtöne (0-360°)
- Aktualisiert SV-Fläche in Echtzeit
- Slider-Handle mit weißem Border

#### 4.3 Alpha-Slider
- Schachbrett-Hintergrund für Transparenz-Visualisierung
- Gradient von transparent (0) zu opak (255)
- Separate Alpha-Kontrolle unabhängig von RGB

#### 4.4 RGB-Slider (3x einzeln)
- **R**: Rot (0-255)
- **G**: Grün (0-255)
- **B**: Blau (0-255)
- Zeigt aktuellen Wert als Text
- Gradient-Visualisierung pro Kanal
- **Bidirektionale Synchronisation**: RGB ↔ HSV

```java
private void updateRGB(double mouseX, int channel) {
    int[] rgb = hsvToRgb(hue, saturation, value);
    int newValue = (int)(MathHelper.clamp(...) * 255);
    rgb[channel] = newValue;
    
    // Konvertiere zurück zu HSV
    float[] hsv = rgbToHsv(rgb[0], rgb[1], rgb[2]);
    hue = hsv[0];
    saturation = hsv[1];
    value = hsv[2];
    notifyChange();
}
```

#### 4.5 Hex-Code & Vorschau
- **Hex-Anzeige**: `#AARRGGBB` Format
- **Vorschau-Box**: 20x20px mit Schachbrett-Hintergrund
- **Live-Update**: Bei jeder Änderung

### Farbmodell-Konvertierung

**HSV → RGB**:
```java
private static int[] hsvToRgb(float h, float s, float v) {
    float c = v * s;
    float x = c * (1 - Math.abs(((h / 60.0f) % 2) - 1));
    float m = v - c;
    
    // Berechne RGB basierend auf Hue-Sektor
    // ... (siehe Code)
    
    return new int[] {
        (int)((r + m) * 255),
        (int)((g + m) * 255),
        (int)((b + m) * 255)
    };
}
```

**RGB → HSV**:
```java
private static float[] rgbToHsv(int r, int g, int b) {
    float rf = r / 255.0f;
    float gf = g / 255.0f;
    float bf = b / 255.0f;
    
    float max = Math.max(rf, Math.max(gf, bf));
    float min = Math.min(rf, Math.min(gf, bf));
    float delta = max - min;
    
    // Berechne Hue, Saturation, Value
    // ... (siehe Code)
    
    return new float[] { h, s, v };
}
```

### Interaktionssystem

**Mouse Events**:
```java
@Override
public boolean mouseClicked(double mouseX, double mouseY, int button) {
    // Prüfe welches Element angeklickt wurde
    if (isMouseOver(mouseX, mouseY, getX(), currentY, SV_SIZE, SV_SIZE)) {
        draggingSV = true;
        updateSV(mouseX, mouseY, currentY);
        return true;
    }
    // ... weitere Elemente
}

@Override
public boolean mouseDragged(...) {
    // Kontinuierliche Updates während Drag
    if (draggingSV) {
        updateSV(mouseX, mouseY, currentY);
        return true;
    }
    // ... weitere Elemente
}

@Override
public boolean mouseReleased(...) {
    // Beende alle Drag-Operationen
    draggingSV = false;
    draggingHue = false;
    // ...
}
```

### Callback-System

```java
private final Consumer<Integer> onColorChanged;

private void notifyChange() {
    if (onColorChanged != null) {
        onColorChanged.accept(getARGB());
    }
}
```

**Verwendung**:
```java
new ColorPickerWidget(
    x, y, width, height,
    config.primaryColor,           // Initial-Farbe
    color -> config.primaryColor = color  // Callback
);
```

## Datenfluss

```
User Input (Maus/Tastatur)
    ↓
Widget Event (mouseClicked/mouseDragged)
    ↓
Update Farbwerte (HSV/RGB)
    ↓
Konvertierung (HSV ↔ RGB)
    ↓
onColorChanged Callback
    ↓
ModConfig Update
    ↓
JSON-Speicherung (beim Schließen)
```

## Verwendung

### 1. Mod starten
```bash
./gradlew runClient
```

### 2. Config öffnen
- **Im Spiel**: Drücke **R-Taste**
- Config-Screen öffnet sich

### 3. Einstellungen ändern
- Checkboxen an/aus
- Slider ziehen
- Text eingeben
- Farben mit Color Picker auswählen

### 4. Speichern
- **Save & Close**: Speichert alle Änderungen
- **Cancel**: Verwirft Änderungen
- **ESC**: Wie Cancel

## Erweiterbarkeit

### Neue Config-Werte hinzufügen

**1. ModConfig.java**:
```java
public int myNewValue = 42;
```

**2. SimpleConfigScreen.java**:
```java
SliderWidget mySlider = new SliderWidget(...);
this.addDrawableChild(mySlider);
```

**3. saveAndClose()**:
```java
config.myNewValue = (int)(mySlider.value * 100);
```

### Neuen Color Picker hinzufügen

```java
// ModConfig.java
public int customColor = 0xFFFFFFFF;

// SimpleConfigScreen.java
ColorPickerWidget customPicker = new ColorPickerWidget(
    x, y, 200, 180,
    config.customColor,
    color -> config.customColor = color
);
this.addDrawableChild(customPicker);
```

## Technische Details

### Farbformat: ARGB

```
0xAARRGGBB
  ││││││└└─ Blau  (0-255)
  ││││└└─── Grün  (0-255)
  ││└└───── Rot   (0-255)
  └└─────── Alpha (0-255)
```

**Beispiele**:
- `0xFFFF0000` - Rot, voll opak
- `0x80FF0000` - Rot, 50% transparent
- `0xFF00FF00` - Grün, voll opak
- `0x00000000` - Komplett transparent

### Performance

- **SV-Picker**: 10.000 Pixel pro Frame (100x100)
- **Optimierung**: Nur bei Interaktion neu gezeichnet
- **Keine Lags**: Effizientes Pixel-Rendering

## Vorteile dieser Implementierung

✅ **Keine externen Dependencies** - Nur Fabric API  
✅ **Vollständige Kontrolle** - Eigenes Widget, keine Einschränkungen  
✅ **Professionell** - Alle Features moderner Color Picker  
✅ **Erweiterbar** - Einfach neue Widgets/Werte hinzufügen  
✅ **Performant** - Effiziente Rendering-Methoden  
✅ **Type-Safe** - Starke Typisierung in Java  
✅ **Persistent** - Automatisches Speichern/Laden  
✅ **Funktioniert garantiert** - Getestet und funktionsfähig!

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
| Funktioniert | ✅ | ✅ |

## Zusammenfassung

Diese Mod bietet:
- ✅ Funktionierenden Config-Screen (Keybinding: R)
- ✅ Persistente JSON-Speicherung
- ✅ Checkboxen, Slider, Text-Input
- ✅ **Vollwertigen Color Picker** mit allen Features
- ✅ Saubere, erweiterbare Architektur
- ✅ Keine externen Dependencies
- ✅ Professionelle UX

**Perfekt für jede Mod, die Farbkonfiguration benötigt!** 🎨
