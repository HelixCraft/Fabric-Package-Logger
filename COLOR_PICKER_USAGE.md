# Color Picker Widget - Verwendung & Integration

## Schnellstart

### 1. Color Picker erstellen

```java
ColorPickerWidget picker = new ColorPickerWidget(
    x,              // X-Position
    y,              // Y-Position
    200,            // Breite
    150,            // Höhe
    0xFFFF0000,     // Initial-Farbe (ARGB)
    color -> {      // Callback bei Änderung
        // Mache etwas mit der neuen Farbe
        System.out.println("Neue Farbe: #" + Integer.toHexString(color));
    }
);
```

### 2. Zum Screen hinzufügen

```java
this.addDrawableChild(picker);
```

Das war's! Der Color Picker ist voll funktionsfähig.

## Farbformat: ARGB

Der Color Picker arbeitet mit 32-bit ARGB-Integers:

```
0xAARRGGBB
  ││││││└└─ Blau  (0-255)
  ││││└└─── Grün  (0-255)
  ││└└───── Rot   (0-255)
  └└─────── Alpha (0-255, 0=transparent, 255=opak)
```

### Beispiele:

```java
0xFFFF0000  // Rot, voll opak
0x80FF0000  // Rot, 50% transparent
0xFF00FF00  // Grün, voll opak
0xFF0000FF  // Blau, voll opak
0xFFFFFFFF  // Weiß, voll opak
0xFF000000  // Schwarz, voll opak
0x00000000  // Komplett transparent
```

## Farbe extrahieren

```java
int argb = picker.getARGB(); // Oder aus Callback

int alpha = (argb >> 24) & 0xFF;
int red   = (argb >> 16) & 0xFF;
int green = (argb >> 8) & 0xFF;
int blue  = argb & 0xFF;

// Als Float (0.0 - 1.0)
float alphaF = alpha / 255.0f;
float redF   = red / 255.0f;
float greenF = green / 255.0f;
float blueF  = blue / 255.0f;
```

## Farbe setzen

```java
// Aus ARGB
picker.setColorFromARGB(0xFF00FF00);

// Aus RGB-Komponenten
int r = 255, g = 128, b = 64, a = 255;
int argb = (a << 24) | (r << 16) | (g << 8) | b;
picker.setColorFromARGB(argb);
```

## Integration in Config

### Schritt 1: Config-Feld

```java
// In ModConfig.java
public int playerNameColor = 0xFFFFFFFF;
public int healthBarColor = 0xFFFF0000;
public int manaBarColor = 0xFF0000FF;
```

### Schritt 2: Widget erstellen

```java
// In ConfigScreen.init()
ColorPickerWidget playerNamePicker = new ColorPickerWidget(
    centerX - 100, currentY, 200, 150,
    config.playerNameColor,
    color -> config.playerNameColor = color
);
this.addDrawableChild(playerNamePicker);
```

### Schritt 3: Speichern

```java
// In ConfigScreen.saveAndClose()
config.save(); // Farben werden automatisch gespeichert
```

## Verwendung der Farben im Rendering

### Text-Rendering

```java
int color = ModConfig.getInstance().playerNameColor;

// Mit DrawContext (1.21+)
context.drawText(textRenderer, "Player Name", x, y, color, shadow);

// Ohne Alpha (nur RGB)
int rgb = color & 0x00FFFFFF;
context.drawText(textRenderer, "Text", x, y, rgb, false);
```

### Rechteck-Rendering

```java
int color = ModConfig.getInstance().healthBarColor;

// Mit Alpha
context.fill(x1, y1, x2, y2, color);

// Ohne Alpha
context.fill(x1, y1, x2, y2, color | 0xFF000000);
```

### OpenGL/RenderSystem

```java
int argb = ModConfig.getInstance().accentColor;

float a = ((argb >> 24) & 0xFF) / 255.0f;
float r = ((argb >> 16) & 0xFF) / 255.0f;
float g = ((argb >> 8) & 0xFF) / 255.0f;
float b = (argb & 0xFF) / 255.0f;

RenderSystem.setShaderColor(r, g, b, a);
// ... Rendering-Code
RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f); // Reset
```

## Erweiterte Anwendungsfälle

### Mehrere Color Picker mit Kategorien

```java
// Kategorie-Überschrift
this.addDrawableChild(ButtonWidget.builder(
    Text.literal("UI Colors"),
    button -> {}
).dimensions(x, y, width, 20).build());
y += 25;

// Color Picker 1
ColorPickerWidget uiPrimary = new ColorPickerWidget(
    x, y, width, 150,
    config.uiPrimaryColor,
    color -> config.uiPrimaryColor = color
);
this.addDrawableChild(uiPrimary);
y += 160;

// Color Picker 2
ColorPickerWidget uiSecondary = new ColorPickerWidget(
    x, y, width, 150,
    config.uiSecondaryColor,
    color -> config.uiSecondaryColor = color
);
this.addDrawableChild(uiSecondary);
```

### Reset-Button für Farben

```java
this.addDrawableChild(ButtonWidget.builder(
    Text.literal("Reset to Default"),
    button -> {
        config.primaryColor = 0xFFFF0000;
        // Recreate screen to update picker
        client.setScreen(new ConfigScreen(parent));
    }
).dimensions(x, y, width, 20).build());
```

### Farb-Presets

```java
private static final int[] COLOR_PRESETS = {
    0xFFFF0000, // Rot
    0xFF00FF00, // Grün
    0xFF0000FF, // Blau
    0xFFFFFF00, // Gelb
    0xFFFF00FF, // Magenta
    0xFF00FFFF, // Cyan
};

// Preset-Buttons
for (int i = 0; i < COLOR_PRESETS.length; i++) {
    final int preset = COLOR_PRESETS[i];
    this.addDrawableChild(ButtonWidget.builder(
        Text.empty(),
        button -> {
            config.primaryColor = preset;
            client.setScreen(new ConfigScreen(parent));
        }
    ).dimensions(x + i * 25, y, 20, 20).build());
}
```

## Tipps & Best Practices

### 1. Alpha-Kanal beachten

Wenn du keine Transparenz brauchst, setze Alpha immer auf 255:

```java
int color = pickerColor | 0xFF000000; // Erzwingt volle Deckkraft
```

### 2. Performance

Color Picker sind rendering-intensiv. Für viele Picker:
- Verwende Tabs/Kategorien
- Implementiere Scrolling
- Zeige nur sichtbare Picker

### 3. Accessibility

```java
// Füge Tooltips hinzu
if (isMouseOver(mouseX, mouseY)) {
    context.drawTooltip(textRenderer, 
        Text.literal("Click and drag to select color"), 
        mouseX, mouseY);
}
```

### 4. Validierung

```java
// Stelle sicher, dass Alpha nicht 0 ist (unsichtbar)
if ((color & 0xFF000000) == 0) {
    color |= 0xFF000000; // Setze auf voll opak
}
```

## Fehlerbehebung

### Problem: Farbe wird nicht gespeichert

**Lösung**: Stelle sicher, dass `config.save()` aufgerufen wird:

```java
private void saveAndClose() {
    // ... andere Werte
    config.save(); // WICHTIG!
    this.close();
}
```

### Problem: Farbe ist schwarz/unsichtbar

**Lösung**: Prüfe Alpha-Kanal:

```java
int color = config.primaryColor;
if ((color & 0xFF000000) == 0) {
    color = 0xFFFF0000; // Default zu Rot
}
```

### Problem: Picker reagiert nicht

**Lösung**: Stelle sicher, dass Widget hinzugefügt wurde:

```java
this.addDrawableChild(picker); // Nicht vergessen!
```

## Beispiel: Vollständige Integration

```java
public class MyConfigScreen extends Screen {
    private final ModConfig config = ModConfig.getInstance();
    private ColorPickerWidget colorPicker;
    
    public MyConfigScreen() {
        super(Text.literal("My Config"));
    }
    
    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = 40;
        
        // Label
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Choose Your Color"),
            button -> {}
        ).dimensions(centerX - 100, y, 200, 20).build());
        y += 25;
        
        // Color Picker
        this.colorPicker = new ColorPickerWidget(
            centerX - 100, y, 200, 150,
            config.myColor,
            color -> {
                config.myColor = color;
                // Optional: Live-Update
                updatePreview();
            }
        );
        this.addDrawableChild(colorPicker);
        y += 160;
        
        // Save Button
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Save"),
            button -> {
                config.save();
                this.close();
            }
        ).dimensions(centerX - 50, y, 100, 20).build());
    }
    
    private void updatePreview() {
        // Zeige Live-Vorschau der Farbe
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(
            this.textRenderer, this.title, 
            this.width / 2, 15, 0xFFFFFF
        );
        super.render(context, mouseX, mouseY, delta);
    }
}
```

## Zusammenfassung

Der ColorPickerWidget bietet:
- ✅ Vollständige HSV-Farbauswahl
- ✅ Alpha-Kanal-Unterstützung
- ✅ RGB-Slider
- ✅ Hex-Anzeige
- ✅ Live-Vorschau
- ✅ Einfache Integration
- ✅ Keine externen Dependencies

Perfekt für jede Mod, die Farbkonfiguration benötigt!
