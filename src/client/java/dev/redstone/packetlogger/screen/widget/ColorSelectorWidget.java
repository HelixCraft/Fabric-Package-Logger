package dev.redstone.packetlogger.screen.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

/**
 * Kompaktes Color-Selector-Widget: Hex-Input + Color-Box
 * Klick auf Box öffnet den Color-Editor-Dialog
 */
public class ColorSelectorWidget extends ClickableWidget {
    private final TextFieldWidget hexField;
    private final Consumer<Integer> onColorChanged;
    private final Runnable onBoxClicked;
    private int color;
    
    private static final int BOX_SIZE = 20;
    private static final int SPACING = 5;
    
    public ColorSelectorWidget(int x, int y, int width, int initialColor, 
                               Consumer<Integer> onColorChanged, Runnable onBoxClicked) {
        super(x, y, width, BOX_SIZE, Text.empty());
        this.color = initialColor;
        this.onColorChanged = onColorChanged;
        this.onBoxClicked = onBoxClicked;
        
        MinecraftClient client = MinecraftClient.getInstance();
        
        // Hex-Input-Feld (links)
        int hexFieldWidth = width - BOX_SIZE - SPACING;
        this.hexField = new TextFieldWidget(
            client.textRenderer, x, y, hexFieldWidth, BOX_SIZE,
            Text.literal("Hex Color")
        );
        this.hexField.setMaxLength(9); // #AARRGGBB
        this.hexField.setText(String.format("#%08X", color));
        this.hexField.setChangedListener(this::onHexChanged);
    }
    
    public void setColor(int color) {
        this.color = color;
        this.hexField.setText(String.format("#%08X", color));
    }
    
    public int getColor() {
        return color;
    }
    
    private void onHexChanged(String hex) {
        try {
            if (hex.startsWith("#")) {
                hex = hex.substring(1);
            }
            if (hex.length() == 6) {
                hex = "FF" + hex; // Füge Alpha hinzu
            }
            if (hex.length() == 8) {
                int newColor = (int)Long.parseLong(hex, 16);
                this.color = newColor;
                if (onColorChanged != null) {
                    onColorChanged.accept(newColor);
                }
            }
        } catch (NumberFormatException e) {
            // Ignoriere ungültige Eingaben
        }
    }
    
    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        // Render Hex-Field
        hexField.render(context, mouseX, mouseY, delta);
        
        // Render Color-Box (rechts)
        int boxX = getX() + width - BOX_SIZE;
        int boxY = getY();
        
        // Schachbrett-Hintergrund
        for (int i = 0; i < BOX_SIZE; i += 4) {
            for (int j = 0; j < BOX_SIZE; j += 4) {
                int bgColor = ((i / 4 + j / 4) % 2 == 0) ? 0xFFCCCCCC : 0xFFFFFFFF;
                context.fill(boxX + i, boxY + j, boxX + i + 4, boxY + j + 4, bgColor);
            }
        }
        
        // Farbe
        context.fill(boxX, boxY, boxX + BOX_SIZE, boxY + BOX_SIZE, color);
        
        // Border
        context.drawBorder(boxX, boxY, BOX_SIZE, BOX_SIZE, 0xFF000000);
        
        // Hover-Effekt
        if (isMouseOverBox(mouseX, mouseY)) {
            context.drawBorder(boxX - 1, boxY - 1, BOX_SIZE + 2, BOX_SIZE + 2, 0xFFFFFFFF);
        }
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Prüfe ob Hex-Field geklickt wurde
        if (hexField.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        
        // Prüfe ob Color-Box geklickt wurde
        if (isMouseOverBox(mouseX, mouseY)) {
            if (onBoxClicked != null) {
                onBoxClicked.run();
            }
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return hexField.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean charTyped(char chr, int modifiers) {
        return hexField.charTyped(chr, modifiers);
    }
    
    public void setFocused(boolean focused) {
        hexField.setFocused(focused);
    }
    
    private boolean isMouseOverBox(double mouseX, double mouseY) {
        int boxX = getX() + width - BOX_SIZE;
        int boxY = getY();
        return mouseX >= boxX && mouseX < boxX + BOX_SIZE && 
               mouseY >= boxY && mouseY < boxY + BOX_SIZE;
    }
    
    @Override
    protected void appendClickableNarrations(net.minecraft.client.gui.screen.narration.NarrationMessageBuilder builder) {
    }
}
