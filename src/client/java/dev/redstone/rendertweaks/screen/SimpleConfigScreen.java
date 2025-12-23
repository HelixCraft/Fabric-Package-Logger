package dev.redstone.rendertweaks.screen;

import dev.redstone.rendertweaks.config.ModConfig;
import dev.redstone.rendertweaks.screen.widget.ColorSelectorWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class SimpleConfigScreen extends Screen {
    private final Screen parent;
    private final ModConfig config;
    
    // Widgets
    private CheckboxWidget exampleCheckbox;
    private CheckboxWidget featureCheckbox;
    private SliderWidget intSlider;
    private SliderWidget floatSlider;
    private TextFieldWidget stringField;
    private ColorSelectorWidget primaryColorSelector;
    private ColorSelectorWidget secondaryColorSelector;
    
    public SimpleConfigScreen(Screen parent) {
        super(Text.literal("Render Tweaks Config"));
        this.parent = parent;
        this.config = ModConfig.getInstance();
    }
    
    @Override
    protected void init() {
        super.init();
        
        int centerX = this.width / 2;
        int y = 40;
        int widgetWidth = 200;
        
        // Checkboxen
        this.exampleCheckbox = CheckboxWidget.builder(Text.literal("Example Boolean"), this.textRenderer)
            .pos(centerX - widgetWidth / 2, y)
            .checked(config.exampleBoolean)
            .build();
        this.addDrawableChild(exampleCheckbox);
        y += 25;
        
        this.featureCheckbox = CheckboxWidget.builder(Text.literal("Enable Feature"), this.textRenderer)
            .pos(centerX - widgetWidth / 2, y)
            .checked(config.enableFeature)
            .build();
        this.addDrawableChild(featureCheckbox);
        y += 30;
        
        // Integer Slider
        this.intSlider = new SliderWidget(
            centerX - widgetWidth / 2, y, widgetWidth, 20,
            Text.literal("Int: " + config.exampleInt),
            config.exampleInt / 100.0
        ) {
            @Override
            protected void updateMessage() {
                int value = (int)(this.value * 100);
                this.setMessage(Text.literal("Int: " + value));
            }
            
            @Override
            protected void applyValue() {
                config.exampleInt = (int)(this.value * 100);
            }
        };
        this.addDrawableChild(intSlider);
        y += 25;
        
        // Float Slider
        this.floatSlider = new SliderWidget(
            centerX - widgetWidth / 2, y, widgetWidth, 20,
            Text.literal("Float: " + String.format("%.2f", config.exampleFloat)),
            config.exampleFloat
        ) {
            @Override
            protected void updateMessage() {
                this.setMessage(Text.literal("Float: " + String.format("%.2f", this.value)));
            }
            
            @Override
            protected void applyValue() {
                config.exampleFloat = (float)this.value;
            }
        };
        this.addDrawableChild(floatSlider);
        y += 25;
        
        // Text Input
        this.stringField = new TextFieldWidget(
            this.textRenderer, centerX - widgetWidth / 2, y, widgetWidth, 20,
            Text.literal("String Value")
        );
        this.stringField.setMaxLength(100);
        this.stringField.setText(config.exampleString);
        this.addDrawableChild(stringField);
        y += 35;
        
        // === COLOR SELECTORS ===
        
        // Primary Color (with label above)
        y += 10; // Space for label
        this.primaryColorSelector = new ColorSelectorWidget(
            centerX - widgetWidth / 2, y, widgetWidth,
            config.primaryColor,
            color -> config.primaryColor = color,
            () -> openColorEditor(config.primaryColor, color -> {
                config.primaryColor = color;
                primaryColorSelector.setColor(color);
            })
        );
        this.addDrawableChild(primaryColorSelector);
        y += 30;
        
        // Secondary Color (with label above)
        y += 10; // Space for label
        this.secondaryColorSelector = new ColorSelectorWidget(
            centerX - widgetWidth / 2, y, widgetWidth,
            config.secondaryColor,
            color -> config.secondaryColor = color,
            () -> openColorEditor(config.secondaryColor, color -> {
                config.secondaryColor = color;
                secondaryColorSelector.setColor(color);
            })
        );
        this.addDrawableChild(secondaryColorSelector);
        y += 40;
        
        // === BUTTONS ===
        
        this.addDrawableChild(
            ButtonWidget.builder(Text.literal("Save & Close"), button -> this.saveAndClose())
                .dimensions(this.width / 2 - 105, this.height - 30, 100, 20)
                .build()
        );
        
        this.addDrawableChild(
            ButtonWidget.builder(Text.literal("Cancel"), button -> this.close())
                .dimensions(this.width / 2 + 5, this.height - 30, 100, 20)
                .build()
        );
    }
    
    private void openColorEditor(int initialColor, java.util.function.Consumer<Integer> onColorSelected) {
        if (this.client != null) {
            this.client.setScreen(new ColorEditorScreen(this, initialColor, onColorSelected));
        }
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        
        // Title
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            this.title,
            this.width / 2,
            15,
            0xFFFFFF
        );
        
        // Labels für Color Selectors (ÜBER den Widgets)
        int centerX = this.width / 2;
        int widgetWidth = 200;
        
        // Berechne Y-Position der Color Selectors
        int colorSelectorY = 40 + 25 + 30 + 25 + 25 + 35; // Nach allen anderen Widgets
        
        context.drawText(
            this.textRenderer,
            "Primary Color:",
            centerX - widgetWidth / 2,
            colorSelectorY,
            0xFFFFFF,
            false
        );
        
        context.drawText(
            this.textRenderer,
            "Secondary Color:",
            centerX - widgetWidth / 2,
            colorSelectorY + 40,
            0xFFFFFF,
            false
        );
    }
    
    private void saveAndClose() {
        config.exampleBoolean = exampleCheckbox.isChecked();
        config.enableFeature = featureCheckbox.isChecked();
        config.exampleString = stringField.getText();
        config.save();
        this.close();
    }
    
    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }
}
