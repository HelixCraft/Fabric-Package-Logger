package dev.redstone.packetlogger.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.function.Consumer;


public class ColorEditorScreen extends Screen {
    private final Screen parent;
    private final Consumer<Integer> onColorSelected;

    private float hue = 0.0f;
    private float saturation = 1.0f;
    private float value = 1.0f;
    private float alpha = 1.0f;

    private int dialogX, dialogY, dialogWidth, dialogHeight;
    private static final int PADDING = 10;
    private static final int SV_SIZE = 100;
    private static final int HUE_WIDTH = 20; // Etwas breiter für bessere Bedienung
    private static final int SLIDER_WIDTH = 100;
    private static final int SLIDER_HEIGHT = 12; // Etwas flacher für cleaneren Look
    private static final int PREVIEW_HEIGHT = 20;

    // State tracking
    private boolean draggingSV = false;
    private boolean draggingHue = false;
    private boolean draggingR = false, draggingG = false, draggingB = false;
    private boolean draggingA = false;
    private boolean draggingH = false, draggingS = false, draggingV = false;

    // Widgets - no hField (H controlled by vertical bar)
    private TextFieldWidget sField, vField;
    private TextFieldWidget rField, gField, bField, aField;
    private TextFieldWidget hexField;

    // Prevent loop updates when typing
    private boolean isUpdatingFields = false;

    public ColorEditorScreen(Screen parent, int initialColor, Consumer<Integer> onColorSelected) {
        super(Text.literal("Color Editor"));
        this.parent = parent;
        this.onColorSelected = onColorSelected;
        setColorFromARGB(initialColor);
    }

    @Override
    protected void init() {
        super.init();

        this.dialogWidth = 330; // Etwas kompakter
        this.dialogHeight = 230;
        this.dialogX = (this.width - dialogWidth) / 2;
        this.dialogY = (this.height - dialogHeight) / 2;

        int startX = dialogX + PADDING;
        int startY = dialogY + 30;
        
        // Input fields layout
        int inputX = startX + SV_SIZE + HUE_WIDTH + SLIDER_WIDTH + 45; 
        int inputY = startY;
        int inputWidth = 40;
        int inputHeight = 16;
        int rowSpacing = 20;

        MinecraftClient client = MinecraftClient.getInstance();

        // Input fields - aligned with sliders: S, V, R, G, B, A
        // H is controlled by vertical hue bar only
        sField = createInputField(client, inputX, inputY, inputWidth, inputHeight);
        sField.setChangedListener(s -> updateFromHSVFields());
        inputY += rowSpacing;
        
        vField = createInputField(client, inputX, inputY, inputWidth, inputHeight);
        vField.setChangedListener(s -> updateFromHSVFields());
        inputY += rowSpacing;
        
        // RGB Inputs
        rField = createInputField(client, inputX, inputY, inputWidth, inputHeight);
        rField.setChangedListener(s -> updateFromRGBFields());
        inputY += rowSpacing;
        
        gField = createInputField(client, inputX, inputY, inputWidth, inputHeight);
        gField.setChangedListener(s -> updateFromRGBFields());
        inputY += rowSpacing;
        
        bField = createInputField(client, inputX, inputY, inputWidth, inputHeight);
        bField.setChangedListener(s -> updateFromRGBFields());
        inputY += rowSpacing;
        
        // Alpha Input
        aField = createInputField(client, inputX, inputY, inputWidth, inputHeight);
        aField.setChangedListener(s -> updateFromAlphaField());

        // Hex Field
        int hexWidth = 80;
        hexField = new TextFieldWidget(client.textRenderer, dialogX + 50, dialogY + dialogHeight - 35, hexWidth, 18, Text.empty());
        hexField.setMaxLength(9);
        hexField.setChangedListener(s -> updateFromHexField());
        this.addDrawableChild(hexField);

        // Buttons
        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Apply"),
            button -> {
                if (onColorSelected != null) onColorSelected.accept(getARGB());
                this.close();
            }
        ).dimensions(dialogX + dialogWidth - 110, dialogY + dialogHeight - 35, 45, 20).build());

        this.addDrawableChild(ButtonWidget.builder(
            Text.literal("Cancel"),
            button -> this.close()
        ).dimensions(dialogX + dialogWidth - 60, dialogY + dialogHeight - 35, 45, 20).build());

        updateFields(true); // Initial populate
    }

    private TextFieldWidget createInputField(MinecraftClient client, int x, int y, int width, int height) {
        TextFieldWidget field = new TextFieldWidget(client.textRenderer, x, y, width, height, Text.empty());
        field.setMaxLength(4);
        this.addDrawableChild(field);
        return field;
    }

    /* --- Logic & Listeners --- */

    private void updateFromHSVFields() {
        if (isUpdatingFields) return;
        try {
            float s = Float.parseFloat(sField.getText()) / 100f;
            float v = Float.parseFloat(vField.getText()) / 100f;
            this.saturation = MathHelper.clamp(s, 0, 1);
            this.value = MathHelper.clamp(v, 0, 1);
            updateFields(false); // Update others, keep focus
        } catch (NumberFormatException ignored) {}
    }

    private void updateFromRGBFields() {
        if (isUpdatingFields) return;
        try {
            int r = Integer.parseInt(rField.getText());
            int g = Integer.parseInt(gField.getText());
            int b = Integer.parseInt(bField.getText());
            float[] hsv = rgbToHsv(MathHelper.clamp(r, 0, 255), MathHelper.clamp(g, 0, 255), MathHelper.clamp(b, 0, 255));
            this.hue = hsv[0];
            this.saturation = hsv[1];
            this.value = hsv[2];
            updateFields(false);
        } catch (NumberFormatException ignored) {}
    }

    private void updateFromAlphaField() {
        if (isUpdatingFields) return;
        try {
            int a = Integer.parseInt(aField.getText());
            this.alpha = MathHelper.clamp(a, 0, 255) / 255f;
            updateFields(false);
        } catch (NumberFormatException ignored) {}
    }

    private void updateFromHexField() {
        if (isUpdatingFields) return;
        String text = hexField.getText().replace("#", "");
        try {
            long colorVal = Long.parseLong(text, 16);
            if (text.length() == 8) {
                // ARGB
                setColorFromARGB((int)colorVal);
            } else if (text.length() == 6) {
                // RGB (Keep Alpha)
                int r = (int)((colorVal >> 16) & 0xFF);
                int g = (int)((colorVal >> 8) & 0xFF);
                int b = (int)(colorVal & 0xFF);
                float[] hsv = rgbToHsv(r, g, b);
                this.hue = hsv[0];
                this.saturation = hsv[1];
                this.value = hsv[2];
            }
            updateFields(false);
        } catch (NumberFormatException ignored) {}
    }

    private void updateFields(boolean forceAll) {
        isUpdatingFields = true;
        int[] rgb = hsvToRgb(hue, saturation, value);
        int a = (int)(alpha * 255);

        // S and V fields (no H field anymore)
        if (forceAll || !sField.isFocused()) sField.setText(String.valueOf((int)(saturation * 100)));
        if (forceAll || !vField.isFocused()) vField.setText(String.valueOf((int)(value * 100)));

        if (forceAll || !rField.isFocused()) rField.setText(String.valueOf(rgb[0]));
        if (forceAll || !gField.isFocused()) gField.setText(String.valueOf(rgb[1]));
        if (forceAll || !bField.isFocused()) bField.setText(String.valueOf(rgb[2]));
        
        if (forceAll || !aField.isFocused()) aField.setText(String.valueOf(a));
        if (forceAll || !hexField.isFocused()) hexField.setText(String.format("#%08X", getARGB()));
        
        isUpdatingFields = false;
    }

    /* --- Rendering --- */

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Überschreiben, um den Minecraft-Blur zu verhindern.
        // Stattdessen nur ein halb-transparentes schwarzes Overlay zeichnen:
        context.fill(0, 0, this.width, this.height, 0x80000000);
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta); // Dark overlay

        // Dialog Background
        context.fill(dialogX, dialogY, dialogX + dialogWidth, dialogY + dialogHeight, 0xFF000000);
        context.drawBorder(dialogX, dialogY, dialogWidth, dialogHeight, 0xFFFFFFFF);

        context.drawText(this.textRenderer, "", dialogX + PADDING, dialogY + PADDING, 0xFFFFFF, false);

        int startX = dialogX + PADDING;
        int startY = dialogY + 30;

        // 1. SV Picker (Optimized with Gradients)
        renderSVPicker(context, startX, startY);
        
        // 2. Preview Box
        int previewY = startY + SV_SIZE + 5;
        // Checkerboard pattern for alpha
        fillCheckerboard(context, startX, previewY, SV_SIZE, PREVIEW_HEIGHT);
        context.fill(startX, previewY, startX + SV_SIZE, previewY + PREVIEW_HEIGHT, getARGB());
        context.drawBorder(startX, previewY, SV_SIZE, PREVIEW_HEIGHT, 0xFF888888);

        // 3. Hue Slider (Optimized with Gradients)
        int hueX = startX + SV_SIZE + 10;
        renderHueSlider(context, hueX, startY);

        // 4. Parameter Sliders
        int slidersX = hueX + HUE_WIDTH + 10;
        renderParameterSliders(context, slidersX, startY, mouseX, mouseY);
        
        // Hex Label
        context.drawText(this.textRenderer, "HEX:", dialogX + 15, dialogY + dialogHeight - 30, 0xFFAAAAAA, false);

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderSVPicker(DrawContext context, int x, int y) {
        // Correct SV Picker: Bottom=Black, Top=Selected Hue, Top-Left=White
        // We need to render pixel by pixel for correct color mixing
        int hueColor = hsvToRgbInt(hue, 1.0f, 1.0f);
        int[] hueRgb = hsvToRgb(hue, 1.0f, 1.0f);
        
        for (int px = 0; px < SV_SIZE; px++) {
            for (int py = 0; py < SV_SIZE; py++) {
                float s = px / (float)SV_SIZE;  // Left=0 (white), Right=1 (saturated)
                float v = 1.0f - (py / (float)SV_SIZE);  // Top=1 (bright), Bottom=0 (black)
                
                // Mix: Start with hue color, desaturate towards white, darken towards black
                int r = (int)(hueRgb[0] * s * v + 255 * (1 - s) * v);
                int g = (int)(hueRgb[1] * s * v + 255 * (1 - s) * v);
                int b = (int)(hueRgb[2] * s * v + 255 * (1 - s) * v);
                
                int color = packArgb(255, r, g, b);
                context.fill(x + px, y + py, x + px + 1, y + py + 1, color);
            }
        }

        // Selection Circle
        int circleX = x + (int)(saturation * SV_SIZE);
        int circleY = y + (int)((1.0f - value) * SV_SIZE);
        context.drawBorder(circleX - 2, circleY - 2, 5, 5, 0xFF000000); // Inner black
        context.drawBorder(circleX - 3, circleY - 3, 7, 7, 0xFFFFFFFF); // Outer white
    }

    private void renderHueSlider(DrawContext context, int x, int y) {
        // Draw rainbow using segments (much faster than per-pixel)
        int segmentHeight = SV_SIZE / 6;
        // R -> Y -> G -> C -> B -> M -> R
        int[] colors = {0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF, 0xFF0000FF, 0xFFFF00FF, 0xFFFF0000};
        
        for (int i = 0; i < 6; i++) {
            int y1 = y + (i * SV_SIZE) / 6;
            int y2 = y + ((i + 1) * SV_SIZE) / 6;
            context.fillGradient(x, y1, x + HUE_WIDTH, y2, colors[i], colors[i+1]);
        }

        // Handle
        int handleY = y + (int)((hue / 360.0f) * SV_SIZE);
        handleY = MathHelper.clamp(handleY, y, y + SV_SIZE - 2);
        
        context.fill(x - 2, handleY, x + HUE_WIDTH + 2, handleY + 2, 0xFFFFFFFF);
    }

    private void renderParameterSliders(DrawContext context, int x, int y, int mouseX, int mouseY) {
        String[] labels = {"S:", "V:", "R:", "G:", "B:", "A:"};
        float[] vals = {saturation, value, 0, 0, 0, alpha};
        int[] rgb = hsvToRgb(hue, saturation, value);
        vals[2] = rgb[0]/255f; vals[3] = rgb[1]/255f; vals[4] = rgb[2]/255f;

        for (int i = 0; i < 6; i++) {
            int cy = y + i * 20;
            context.drawText(this.textRenderer, labels[i], x, cy + 2, 0xFFFFFF, false);
            
            int barX = x + 15;
            // Map slider types: S=1, V=2, R=3, G=4, B=5, A=6
            int sliderType = i + 1;
            drawSliderTrack(context, barX, cy, sliderType);
            
            // Draw Handle
            int handleX = barX + (int)(vals[i] * SLIDER_WIDTH);
            context.fill(handleX - 1, cy - 1, handleX + 1, cy + SLIDER_HEIGHT + 1, 0xFFFFFFFF);
        }
    }

    private void drawSliderTrack(DrawContext context, int x, int y, int type) {
        int w = SLIDER_WIDTH;
        int h = SLIDER_HEIGHT;
        
        // Alpha slider needs special checkerboard rendering
        if (type == 6) {
            // Draw checkerboard background first
            for (int px = 0; px < w; px++) {
                for (int py = 0; py < h; py++) {
                    boolean isLight = ((px / 4) + (py / 4)) % 2 == 0;
                    int bgColor = isLight ? 0xFFC0C0C0 : 0xFF808080;
                    context.fill(x + px, y + py, x + px + 1, y + py + 1, bgColor);
                }
            }
            // Now blend current color on top with increasing alpha
            int[] rgb = hsvToRgb(hue, saturation, value);
            for (int px = 0; px < w; px++) {
                float t = px / (float)w;
                int a = (int)(t * 255);
                // Blend with checkerboard
                for (int py = 0; py < h; py++) {
                    boolean isLight = ((px / 4) + (py / 4)) % 2 == 0;
                    int bgR = isLight ? 0xC0 : 0x80;
                    int bgG = isLight ? 0xC0 : 0x80;
                    int bgB = isLight ? 0xC0 : 0x80;
                    
                    int finalR = (rgb[0] * a + bgR * (255 - a)) / 255;
                    int finalG = (rgb[1] * a + bgG * (255 - a)) / 255;
                    int finalB = (rgb[2] * a + bgB * (255 - a)) / 255;
                    
                    context.fill(x + px, y + py, x + px + 1, y + py + 1, packArgb(255, finalR, finalG, finalB));
                }
            }
        } else {
            // Normal slider - horizontal gradient via loop
            for (int i = 0; i < w; i++) {
                float t = i / (float)w;
                int color = getSliderColor(type, t);
                context.fill(x + i, y, x + i + 1, y + h, color);
            }
        }
        
        context.drawBorder(x - 1, y - 1, w + 2, h + 2, 0xFF444444);
    }

    private int getSliderColor(int type, float t) {
        int[] rgb = hsvToRgb(hue, saturation, value);
        switch (type) {
            case 1: return hsvToRgbInt(hue, t, value); // Saturation
            case 2: return hsvToRgbInt(hue, saturation, t); // Value
            case 3: return packArgb(255, (int)(t*255), rgb[1], rgb[2]); // R
            case 4: return packArgb(255, rgb[0], (int)(t*255), rgb[2]); // G
            case 5: return packArgb(255, rgb[0], rgb[1], (int)(t*255)); // B
            // case 6 (Alpha) is handled separately in drawSliderTrack
            default: return 0xFFFFFFFF;
        }
    }
    
    // Checkboard helper
    private boolean iPixel(float x, float y) {
        return ((int)(x / 4) + (int)(y / 4)) % 2 == 0;
    }
    
    private void fillCheckerboard(DrawContext context, int x, int y, int w, int h) {
        for(int px=0; px<w; px+=4) {
            for(int py=0; py<h; py+=4) {
                int color = ((px/4 + py/4) % 2 == 0) ? 0xFF808080 : 0xFFC0C0C0;
                context.fill(x+px, y+py, Math.min(x+px+4, x+w), Math.min(y+py+4, y+h), color);
            }
        }
    }

    /* --- Mouse Interaction --- */

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;

        int startX = dialogX + PADDING;
        int startY = dialogY + 30;

        // SV Picker
        if (isMouseOver(mouseX, mouseY, startX, startY, SV_SIZE, SV_SIZE)) {
            draggingSV = true;
            updateSV(mouseX, mouseY, startX, startY);
            return true;
        }

        // Hue Slider
        int hueX = startX + SV_SIZE + 10;
        if (isMouseOver(mouseX, mouseY, hueX, startY, HUE_WIDTH, SV_SIZE)) {
            draggingHue = true;
            updateHue(mouseY, startY);
            return true;
        }

        // Parameter Sliders - Map correctly: i=0->S(type1), i=1->V(type2), i=2->R(type3), i=3->G(type4), i=4->B(type5), i=5->A(type6)
        int slidersX = hueX + HUE_WIDTH + 10 + 15; // +15 for the text offset
        for (int i = 0; i < 6; i++) {
            int cy = startY + i * 20;
            if (isMouseOver(mouseX, mouseY, slidersX, cy, SLIDER_WIDTH, SLIDER_HEIGHT)) {
                int sliderType = i + 1; // S=1, V=2, R=3, G=4, B=5, A=6
                setDragging(sliderType, true);
                updateSlider(sliderType, mouseX, slidersX);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        int startX = dialogX + PADDING;
        int startY = dialogY + 30;

        if (draggingSV) {
            updateSV(mouseX, mouseY, startX, startY);
            return true;
        }
        if (draggingHue) {
            updateHue(mouseY, startY);
            return true;
        }
        
        int slidersX = startX + SV_SIZE + 10 + HUE_WIDTH + 10 + 15;
        for (int i = 0; i < 6; i++) {
            int sliderType = i + 1; // S=1, V=2, R=3, G=4, B=5, A=6
            if (isDragging(sliderType)) {
                updateSlider(sliderType, mouseX, slidersX);
                return true;
            }
        }
        
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingSV = false;
        draggingHue = false;
        draggingH = draggingS = draggingV = draggingR = draggingG = draggingB = draggingA = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void updateSV(double mouseX, double mouseY, int x, int y) {
        this.saturation = MathHelper.clamp((float)(mouseX - x) / SV_SIZE, 0f, 1f);
        this.value = MathHelper.clamp(1.0f - (float)(mouseY - y) / SV_SIZE, 0f, 1f);
        updateFields(true);
    }

    private void updateHue(double mouseY, int y) {
        this.hue = MathHelper.clamp((float)(mouseY - y) / SV_SIZE, 0f, 1f) * 360f;
        updateFields(true);
    }
    
    private void setDragging(int i, boolean val) {
        switch(i) {
            case 0: draggingH = val; break;
            case 1: draggingS = val; break;
            case 2: draggingV = val; break;
            case 3: draggingR = val; break;
            case 4: draggingG = val; break;
            case 5: draggingB = val; break;
            case 6: draggingA = val; break;
        }
    }
    
    private boolean isDragging(int i) {
        switch(i) {
            case 0: return draggingH;
            case 1: return draggingS;
            case 2: return draggingV;
            case 3: return draggingR;
            case 4: return draggingG;
            case 5: return draggingB;
            case 6: return draggingA;
            default: return false;
        }
    }

    private void updateSlider(int type, double mouseX, int x) {
        float t = MathHelper.clamp((float)(mouseX - x) / SLIDER_WIDTH, 0f, 1f);
        switch (type) {
            case 0: hue = t * 360f; break;
            case 1: saturation = t; break;
            case 2: value = t; break;
            case 3: case 4: case 5: {
                int[] rgb = hsvToRgb(hue, saturation, value);
                rgb[type - 3] = (int)(t * 255);
                float[] hsv = rgbToHsv(rgb[0], rgb[1], rgb[2]);
                hue = hsv[0]; saturation = hsv[1]; value = hsv[2];
                break;
            }
            case 6: alpha = t; break;
        }
        updateFields(true);
    }

    /* --- Helpers --- */

    private int getARGB() {
        int[] rgb = hsvToRgb(hue, saturation, value);
        return packArgb((int)(alpha * 255), rgb[0], rgb[1], rgb[2]);
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
    
    private boolean isMouseOver(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    private static int packArgb(int a, int r, int g, int b) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
    
    private static int hsvToRgbInt(float h, float s, float v) {
        int[] rgb = hsvToRgb(h, s, v);
        return packArgb(255, rgb[0], rgb[1], rgb[2]);
    }

    private static int[] hsvToRgb(float h, float s, float v) {
        float c = v * s;
        float x = c * (1 - Math.abs(((h / 60.0f) % 2) - 1));
        float m = v - c;
        float r=0, g=0, b=0;
        if(h < 60) {r=c;g=x;b=0;}
        else if(h < 120) {r=x;g=c;b=0;}
        else if(h < 180) {r=0;g=c;b=x;}
        else if(h < 240) {r=0;g=x;b=c;}
        else if(h < 300) {r=x;g=0;b=c;}
        else {r=c;g=0;b=x;}
        return new int[]{(int)((r+m)*255), (int)((g+m)*255), (int)((b+m)*255)};
    }
    
    private static float[] rgbToHsv(int r, int g, int b) {
        float rf = r/255f, gf = g/255f, bf = b/255f;
        float max = Math.max(rf, Math.max(gf, bf));
        float min = Math.min(rf, Math.min(gf, bf));
        float delta = max - min;
        float h = 0;
        if(delta != 0) {
            if(max == rf) h = 60 * (((gf - bf) / delta) % 6);
            else if(max == gf) h = 60 * (((bf - rf) / delta) + 2);
            else h = 60 * (((rf - gf) / delta) + 4);
        }
        if(h < 0) h += 360;
        float s = max == 0 ? 0 : delta/max;
        return new float[]{h, s, max};
    }
    
    @Override
    public void close() {
        if (this.client != null) this.client.setScreen(parent);
    }
    
    @Override
    public boolean shouldPause() { return false; }
}