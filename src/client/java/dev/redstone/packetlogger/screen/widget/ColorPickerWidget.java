package dev.redstone.packetlogger.screen.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.function.Consumer;

/**
 * Vollwertiger Color Picker mit:
 * - HSV Farbfläche (Saturation/Value)
 * - Hue Slider
 * - Alpha Slider
 * - RGB Slider (R, G, B einzeln)
 * - Hex-Code Anzeige
 * - Live-Vorschau
 */
public class ColorPickerWidget extends ClickableWidget {
    private final Consumer<Integer> onColorChanged;
    
    // HSV Farbmodell
    private float hue = 0.0f;        // 0-360
    private float saturation = 1.0f; // 0-1
    private float value = 1.0f;      // 0-1
    private float alpha = 1.0f;      // 0-1
    
    // Layout
    private static final int SV_SIZE = 100;
    private static final int SLIDER_HEIGHT = 10;
    private static final int SPACING = 3;
    private static final int PREVIEW_SIZE = 20;
    
    // Interaktion
    private boolean draggingSV = false;
    private boolean draggingHue = false;
    private boolean draggingAlpha = false;
    private boolean draggingRed = false;
    private boolean draggingGreen = false;
    private boolean draggingBlue = false;
    
    public ColorPickerWidget(int x, int y, int width, int height, int initialColor, Consumer<Integer> onColorChanged) {
        super(x, y, width, height, Text.empty());
        this.onColorChanged = onColorChanged;
        setColorFromARGB(initialColor);
    }
    
    private void setColorFromARGB(int argb) {
        this.alpha = ((argb >> 24) & 0xFF) / 255.0f;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        
        float[] hsv = rgbToHsv(r, g, b);
        this.hue = hsv[0];
        this.saturation = hsv[1];
        this.value = hsv[2];
    }
    
    private int getARGB() {
        int[] rgb = hsvToRgb(hue, saturation, value);
        int a = (int)(alpha * 255);
        return (a << 24) | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
    }
    
    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        int currentY = getY();
        
        // 1. SV Picker
        renderSVPicker(context, getX(), currentY);
        currentY += SV_SIZE + SPACING;
        
        // 2. Hue Slider
        context.drawText(client.textRenderer, "Hue", getX(), currentY, 0xFFFFFF, false);
        currentY += 10;
        renderHueSlider(context, getX(), currentY);
        currentY += SLIDER_HEIGHT + SPACING;
        
        // 3. Alpha Slider
        context.drawText(client.textRenderer, "Alpha", getX(), currentY, 0xFFFFFF, false);
        currentY += 10;
        renderAlphaSlider(context, getX(), currentY);
        currentY += SLIDER_HEIGHT + SPACING;
        
        // 4. RGB Sliders
        renderRGBSliders(context, getX(), currentY, client);
        currentY += (SLIDER_HEIGHT + 10) * 3 + SPACING;
        
        // 5. Hex & Preview
        renderHexAndPreview(context, getX(), currentY, client);
    }
    
    private void renderSVPicker(DrawContext context, int x, int y) {
        // Zeichne SV-Fläche
        for (int py = 0; py < SV_SIZE; py++) {
            for (int px = 0; px < SV_SIZE; px++) {
                float s = px / (float)SV_SIZE;
                float v = 1.0f - (py / (float)SV_SIZE);
                int[] rgb = hsvToRgb(hue, s, v);
                int color = 0xFF000000 | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
                context.fill(x + px, y + py, x + px + 1, y + py + 1, color);
            }
        }
        
        // Auswahlkreis
        int circleX = x + (int)(saturation * SV_SIZE);
        int circleY = y + (int)((1.0f - value) * SV_SIZE);
        context.drawBorder(circleX - 4, circleY - 4, 8, 8, 0xFFFFFFFF);
        context.drawBorder(circleX - 3, circleY - 3, 6, 6, 0xFF000000);
    }
    
    private void renderHueSlider(DrawContext context, int x, int y) {
        // Hue Gradient
        for (int i = 0; i < width; i++) {
            float h = (i / (float)width) * 360.0f;
            int[] rgb = hsvToRgb(h, 1.0f, 1.0f);
            int color = 0xFF000000 | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
            context.fill(x + i, y, x + i + 1, y + SLIDER_HEIGHT, color);
        }
        
        // Handle
        int handleX = x + (int)((hue / 360.0f) * width);
        context.fill(handleX - 1, y - 1, handleX + 1, y + SLIDER_HEIGHT + 1, 0xFFFFFFFF);
    }
    
    private void renderAlphaSlider(DrawContext context, int x, int y) {
        // Schachbrett
        for (int i = 0; i < width; i += 4) {
            for (int j = 0; j < SLIDER_HEIGHT; j += 4) {
                int color = ((i / 4 + j / 4) % 2 == 0) ? 0xFFCCCCCC : 0xFFFFFFFF;
                context.fill(x + i, y + j, x + i + 4, y + j + 4, color);
            }
        }
        
        // Alpha Gradient
        int[] rgb = hsvToRgb(hue, saturation, value);
        for (int i = 0; i < width; i++) {
            float a = i / (float)width;
            int alphaInt = (int)(a * 255);
            int color = (alphaInt << 24) | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
            context.fill(x + i, y, x + i + 1, y + SLIDER_HEIGHT, color);
        }
        
        // Handle
        int handleX = x + (int)(alpha * width);
        context.fill(handleX - 1, y - 1, handleX + 1, y + SLIDER_HEIGHT + 1, 0xFFFFFFFF);
    }
    
    private void renderRGBSliders(DrawContext context, int x, int y, MinecraftClient client) {
        int[] rgb = hsvToRgb(hue, saturation, value);
        String[] labels = {"R", "G", "B"};
        
        for (int i = 0; i < 3; i++) {
            int currentY = y + i * (SLIDER_HEIGHT + 10);
            context.drawText(client.textRenderer, labels[i] + ": " + rgb[i], x, currentY, 0xFFFFFF, false);
            currentY += 10;
            
            // Gradient
            for (int px = 0; px < width; px++) {
                int val = (int)((px / (float)width) * 255);
                int[] tempRgb = rgb.clone();
                tempRgb[i] = val;
                int color = 0xFF000000 | (tempRgb[0] << 16) | (tempRgb[1] << 8) | tempRgb[2];
                context.fill(x + px, currentY, x + px + 1, currentY + SLIDER_HEIGHT, color);
            }
            
            // Handle
            int handleX = x + (int)((rgb[i] / 255.0f) * width);
            context.fill(handleX - 1, currentY - 1, handleX + 1, currentY + SLIDER_HEIGHT + 1, 0xFFFFFFFF);
        }
    }
    
    private void renderHexAndPreview(DrawContext context, int x, int y, MinecraftClient client) {
        // Preview Box
        int previewX = x + width - PREVIEW_SIZE;
        
        // Schachbrett
        for (int i = 0; i < PREVIEW_SIZE; i += 4) {
            for (int j = 0; j < PREVIEW_SIZE; j += 4) {
                int color = ((i / 4 + j / 4) % 2 == 0) ? 0xFFCCCCCC : 0xFFFFFFFF;
                context.fill(previewX + i, y + j, previewX + i + 4, y + j + 4, color);
            }
        }
        
        // Farbe
        context.fill(previewX, y, previewX + PREVIEW_SIZE, y + PREVIEW_SIZE, getARGB());
        context.drawBorder(previewX, y, PREVIEW_SIZE, PREVIEW_SIZE, 0xFF000000);
        
        // Hex
        String hex = String.format("#%08X", getARGB());
        context.drawText(client.textRenderer, hex, x, y + 6, 0xFFFFFF, false);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.active || !this.visible) return false;
        
        int currentY = getY();
        
        // SV Picker
        if (isMouseOver(mouseX, mouseY, getX(), currentY, SV_SIZE, SV_SIZE)) {
            draggingSV = true;
            updateSV(mouseX, mouseY, currentY);
            return true;
        }
        currentY += SV_SIZE + SPACING + 10;
        
        // Hue
        if (isMouseOver(mouseX, mouseY, getX(), currentY, width, SLIDER_HEIGHT)) {
            draggingHue = true;
            updateHue(mouseX);
            return true;
        }
        currentY += SLIDER_HEIGHT + SPACING + 10;
        
        // Alpha
        if (isMouseOver(mouseX, mouseY, getX(), currentY, width, SLIDER_HEIGHT)) {
            draggingAlpha = true;
            updateAlpha(mouseX);
            return true;
        }
        currentY += SLIDER_HEIGHT + SPACING;
        
        // RGB
        for (int i = 0; i < 3; i++) {
            int sliderY = currentY + i * (SLIDER_HEIGHT + 10) + 10;
            if (isMouseOver(mouseX, mouseY, getX(), sliderY, width, SLIDER_HEIGHT)) {
                if (i == 0) draggingRed = true;
                else if (i == 1) draggingGreen = true;
                else draggingBlue = true;
                updateRGB(mouseX, i);
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        int currentY = getY();
        
        if (draggingSV) {
            updateSV(mouseX, mouseY, currentY);
            return true;
        }
        currentY += SV_SIZE + SPACING + 10;
        
        if (draggingHue) {
            updateHue(mouseX);
            return true;
        }
        currentY += SLIDER_HEIGHT + SPACING + 10;
        
        if (draggingAlpha) {
            updateAlpha(mouseX);
            return true;
        }
        currentY += SLIDER_HEIGHT + SPACING;
        
        if (draggingRed) {
            updateRGB(mouseX, 0);
            return true;
        }
        if (draggingGreen) {
            updateRGB(mouseX, 1);
            return true;
        }
        if (draggingBlue) {
            updateRGB(mouseX, 2);
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingSV = false;
        draggingHue = false;
        draggingAlpha = false;
        draggingRed = false;
        draggingGreen = false;
        draggingBlue = false;
        return true;
    }
    
    private void updateSV(double mouseX, double mouseY, int pickerY) {
        saturation = MathHelper.clamp((float)(mouseX - getX()) / SV_SIZE, 0.0f, 1.0f);
        value = 1.0f - MathHelper.clamp((float)(mouseY - pickerY) / SV_SIZE, 0.0f, 1.0f);
        notifyChange();
    }
    
    private void updateHue(double mouseX) {
        hue = MathHelper.clamp((float)(mouseX - getX()) / width, 0.0f, 1.0f) * 360.0f;
        notifyChange();
    }
    
    private void updateAlpha(double mouseX) {
        alpha = MathHelper.clamp((float)(mouseX - getX()) / width, 0.0f, 1.0f);
        notifyChange();
    }
    
    private void updateRGB(double mouseX, int channel) {
        int[] rgb = hsvToRgb(hue, saturation, value);
        int newValue = (int)(MathHelper.clamp((float)(mouseX - getX()) / width, 0.0f, 1.0f) * 255);
        rgb[channel] = newValue;
        
        float[] hsv = rgbToHsv(rgb[0], rgb[1], rgb[2]);
        hue = hsv[0];
        saturation = hsv[1];
        value = hsv[2];
        notifyChange();
    }
    
    private void notifyChange() {
        if (onColorChanged != null) {
            onColorChanged.accept(getARGB());
        }
    }
    
    private boolean isMouseOver(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
    
    // HSV <-> RGB Konvertierung
    private static int[] hsvToRgb(float h, float s, float v) {
        float c = v * s;
        float x = c * (1 - Math.abs(((h / 60.0f) % 2) - 1));
        float m = v - c;
        
        float r = 0, g = 0, b = 0;
        if (h < 60) { r = c; g = x; b = 0; }
        else if (h < 120) { r = x; g = c; b = 0; }
        else if (h < 180) { r = 0; g = c; b = x; }
        else if (h < 240) { r = 0; g = x; b = c; }
        else if (h < 300) { r = x; g = 0; b = c; }
        else { r = c; g = 0; b = x; }
        
        return new int[] {
            (int)((r + m) * 255),
            (int)((g + m) * 255),
            (int)((b + m) * 255)
        };
    }
    
    private static float[] rgbToHsv(int r, int g, int b) {
        float rf = r / 255.0f;
        float gf = g / 255.0f;
        float bf = b / 255.0f;
        
        float max = Math.max(rf, Math.max(gf, bf));
        float min = Math.min(rf, Math.min(gf, bf));
        float delta = max - min;
        
        float h = 0;
        if (delta != 0) {
            if (max == rf) h = 60 * (((gf - bf) / delta) % 6);
            else if (max == gf) h = 60 * (((bf - rf) / delta) + 2);
            else h = 60 * (((rf - gf) / delta) + 4);
        }
        if (h < 0) h += 360;
        
        float s = (max == 0) ? 0 : (delta / max);
        float v = max;
        
        return new float[] { h, s, v };
    }
    
    @Override
    protected void appendClickableNarrations(net.minecraft.client.gui.screen.narration.NarrationMessageBuilder builder) {
    }
}
