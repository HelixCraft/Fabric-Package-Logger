package dev.redstone.packetlogger.screen;

import dev.redstone.packetlogger.config.ModConfig;
import dev.redstone.packetlogger.config.ModConfig.LogMode;
import dev.redstone.packetlogger.logger.PacketCatalog;
import dev.redstone.packetlogger.PacketLoggerClient;
import dev.redstone.packetlogger.screen.widget.DualListSelectorWidget;
//? if >=26.1 {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.platform.InputConstants;
*///?} elif >=1.21.9 {
/*import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.client.util.InputUtil;
*///?} else {
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.client.util.InputUtil;
//?}

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//? if >=26.1 {
/*public class SimpleConfigScreen extends Screen {
    private final Screen parent;
    private final ModConfig config;
    
    // Widgets
    private Button logPacketsButton;
    private Button logModeButton;
    private Button keybindButton;
    private DualListSelectorWidget s2cSelector;
    private DualListSelectorWidget c2sSelector;
    
    private boolean logPacketsEnabled;
    private LogMode currentLogMode;
    private boolean waitingForKeybind;
    private final Set<String> initialS2C;
    private final Set<String> initialC2S;
    private final int initialKeybind;
    
    // Vollständige Liste S2C Pakete (Server to Client)
    private static final List<String> S2C_PACKETS = PacketCatalog.getS2CPacketNames();
    
    // Vollständige Liste C2S Pakete (Client to Server)
    private static final List<String> C2S_PACKETS = PacketCatalog.getC2SPacketNames();

    public SimpleConfigScreen(Screen parent) {
        super(Component.literal("Packet Logger"));
        this.parent = parent;
        this.config = ModConfig.getInstance();
        this.logPacketsEnabled = config.logPackets;
        this.currentLogMode = config.logMode;
        this.initialS2C = new HashSet<>(config.selectedS2CPackets);
        this.initialC2S = new HashSet<>(config.selectedC2SPackets);
        this.initialKeybind = config.loggingKeybind;
    }
    
    @Override
    protected void init() {
        super.init();
        
        int panelWidth = Math.min(500, this.width - 40);
        int panelX = (this.width - panelWidth) / 2;
        int panelY = 25;
        
        int buttonWidth = (panelWidth - 10) / 2;
        int KEYBIND_GAP = 3;
        int logButtonWidth = (buttonWidth - KEYBIND_GAP) * 2 / 3;
        int keybindButtonWidth = buttonWidth - KEYBIND_GAP - logButtonWidth;
        int y = panelY + 5;
        
        // Log Packets Toggle Button
        this.logPacketsButton = Button.builder(
            Component.literal("Logging: " + (logPacketsEnabled ? "§aON" : "§cOFF")),
            button -> {
                logPacketsEnabled = !logPacketsEnabled;
                button.setMessage(Component.literal("Logging: " + (logPacketsEnabled ? "§aON" : "§cOFF")));
            })
            .bounds(panelX, y, logButtonWidth, 20)
            .build();
        this.addRenderableWidget(logPacketsButton);
        
        // Logging Keybind Setting Button
        this.keybindButton = Button.builder(
            Component.literal(keybindButtonLabel()),
            button -> {
                waitingForKeybind = true;
                button.setMessage(Component.literal("..."));
            })
            .bounds(panelX + logButtonWidth + KEYBIND_GAP, y, keybindButtonWidth, 20)
            .build();
        this.addRenderableWidget(keybindButton);
        
        // Log Mode Toggle Button
        this.logModeButton = Button.builder(
            Component.literal("Output: " + currentLogMode.getDisplayName()),
            button -> {
                currentLogMode = currentLogMode.next();
                button.setMessage(Component.literal("Output: " + currentLogMode.getDisplayName()));
            })
            .bounds(panelX + buttonWidth + 10, y, buttonWidth, 20)
            .build();
        this.addRenderableWidget(logModeButton);
        
        y += 30;
        
        int selectorHeight = (this.height - y - 50) / 2 - 5;
        
        // S2C Selector
        this.s2cSelector = new DualListSelectorWidget(
            panelX, y, panelWidth, selectorHeight,
            "S2C Packets (Server → Client)",
            S2C_PACKETS,
            new HashSet<>(config.selectedS2CPackets),
            selection -> {}
        );
        this.addRenderableWidget(s2cSelector);
        
        y += selectorHeight + 10;
        
        // C2S Selector
        this.c2sSelector = new DualListSelectorWidget(
            panelX, y, panelWidth, selectorHeight,
            "C2S Packets (Client → Server)",
            C2S_PACKETS,
            new HashSet<>(config.selectedC2SPackets),
            selection -> {}
        );
        this.addRenderableWidget(c2sSelector);
        
        int bottomY = this.height - 28;
        int bottomButtonWidth = 100;
        
        this.addRenderableWidget(
            Button.builder(Component.literal("Save"), button -> this.saveAndClose())
                .bounds(this.width / 2 - bottomButtonWidth - 5, bottomY, bottomButtonWidth, 20)
                .build()
        );
        
        this.addRenderableWidget(
            Button.builder(Component.literal("Cancel"), button -> this.cancelAndClose())
                .bounds(this.width / 2 + 5, bottomY, bottomButtonWidth, 20)
                .build()
        );
    }
    
    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
    }
    
    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.fillGradient(0, 0, this.width, this.height, 0xA0101010, 0xB0101010);
        
        int panelWidth = Math.min(500, this.width - 40);
        int panelX = (this.width - panelWidth) / 2;
        int panelY = 20;
        int panelHeight = this.height - 55;
        
        graphics.fill(panelX - 2, panelY - 2, panelX + panelWidth + 2, panelY + panelHeight + 2, 0xFF2A2A2A);
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xE0181818);
        
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        
        graphics.centeredText(this.font, this.title, this.width / 2, 8, 0xFFFFFF);
    }
    
    private void persistToConfig() {
        config.logPackets = logPacketsEnabled;
        config.logMode = currentLogMode;
        config.selectedS2CPackets = new ArrayList<>(s2cSelector.getSelectedPackets());
        config.selectedC2SPackets = new ArrayList<>(c2sSelector.getSelectedPackets());
        config.save();
    }
    
    private void saveAndClose() {
        persistToConfig();
        this.close();
    }
    
    private void cancelAndClose() {
        logPacketsEnabled = config.logPackets;
        currentLogMode = config.logMode;
        config.loggingKeybind = initialKeybind;
        PacketLoggerClient.setLoggingKeybind(config.loggingKeybind);
        s2cSelector.setSelectedPackets(initialS2C);
        c2sSelector.setSelectedPackets(initialC2S);
        persistToConfig();
        this.close();
    }
    
    private void close() {
        if (this.minecraft != null) {
            //? if >=26.2 {
            /^this.minecraft.gui.setScreen(this.parent);
            ^///?} else {
            this.minecraft.setScreen(this.parent);
            //?}
        }
    }
    
    private String keybindButtonLabel() {
        if (config.loggingKeybind == -1) {
            return "Keybind";
        }
        String name = InputConstants.Type.KEYSYM.getOrCreate(config.loggingKeybind).getName();
        return (name.startsWith("key.keyboard.") ? name.substring("key.keyboard.".length()) : name).toUpperCase();
    }
    
    @Override
    public boolean keyPressed(KeyEvent event) {
        if (waitingForKeybind) {
            if (event.key() == InputConstants.KEY_ESCAPE) {
                config.loggingKeybind = -1;
            } else {
                config.loggingKeybind = event.key();
            }
            waitingForKeybind = false;
            PacketLoggerClient.setLoggingKeybind(config.loggingKeybind);
            this.keybindButton.setMessage(Component.literal(keybindButtonLabel()));
            return true;
        }
        if (s2cSelector != null && s2cSelector.keyPressed(event)) {
            return true;
        }
        if (c2sSelector != null && c2sSelector.keyPressed(event)) {
            return true;
        }
        return super.keyPressed(event);
    }
    
    @Override
    public boolean charTyped(CharacterEvent event) {
        if (s2cSelector != null && s2cSelector.charTyped(event)) {
            return true;
        }
        if (c2sSelector != null && c2sSelector.charTyped(event)) {
            return true;
        }
        return super.charTyped(event);
    }
}
*///?} elif >=1.21.9 {
/*public class SimpleConfigScreen extends Screen {
    private final Screen parent;
    private final ModConfig config;
    
    // Widgets
    private ButtonWidget logPacketsButton;
    private ButtonWidget logModeButton;
    private ButtonWidget keybindButton;
    private DualListSelectorWidget s2cSelector;
    private DualListSelectorWidget c2sSelector;
    
    private boolean logPacketsEnabled;
    private LogMode currentLogMode;
    private boolean waitingForKeybind;
    private final Set<String> initialS2C;
    private final Set<String> initialC2S;
    private final int initialKeybind;
    
    // Vollständige Liste S2C Pakete (Server to Client)
    private static final List<String> S2C_PACKETS = PacketCatalog.getS2CPacketNames();
    
    // Vollständige Liste C2S Pakete (Client to Server)
    private static final List<String> C2S_PACKETS = PacketCatalog.getC2SPacketNames();

    public SimpleConfigScreen(Screen parent) {
        super(Text.literal("Packet Logger"));
        this.parent = parent;
        this.config = ModConfig.getInstance();
        this.logPacketsEnabled = config.logPackets;
        this.currentLogMode = config.logMode;
        this.initialS2C = new HashSet<>(config.selectedS2CPackets);
        this.initialC2S = new HashSet<>(config.selectedC2SPackets);
        this.initialKeybind = config.loggingKeybind;
    }
    
    @Override
    protected void init() {
        super.init();
        
        int panelWidth = Math.min(500, this.width - 40);
        int panelX = (this.width - panelWidth) / 2;
        int panelY = 25;
        
        int buttonWidth = (panelWidth - 10) / 2;
        int KEYBIND_GAP = 3;
        int logButtonWidth = (buttonWidth - KEYBIND_GAP) * 2 / 3;
        int keybindButtonWidth = buttonWidth - KEYBIND_GAP - logButtonWidth;
        int y = panelY + 5;
        
        // Log Packets Toggle Button
        this.logPacketsButton = ButtonWidget.builder(
            Text.literal("Logging: " + (logPacketsEnabled ? "§aON" : "§cOFF")),
            button -> {
                logPacketsEnabled = !logPacketsEnabled;
                button.setMessage(Text.literal("Logging: " + (logPacketsEnabled ? "§aON" : "§cOFF")));
            })
            .dimensions(panelX, y, logButtonWidth, 20)
            .build();
        this.addDrawableChild(logPacketsButton);
        
        // Logging Keybind Setting Button
        this.keybindButton = ButtonWidget.builder(
            Text.literal(keybindButtonLabel()),
            button -> {
                waitingForKeybind = true;
                button.setMessage(Text.literal("..."));
            })
            .dimensions(panelX + logButtonWidth + KEYBIND_GAP, y, keybindButtonWidth, 20)
            .build();
        this.addDrawableChild(keybindButton);
        
        // Log Mode Toggle Button
        this.logModeButton = ButtonWidget.builder(
            Text.literal("Output: " + currentLogMode.getDisplayName()),
            button -> {
                currentLogMode = currentLogMode.next();
                button.setMessage(Text.literal("Output: " + currentLogMode.getDisplayName()));
            })
            .dimensions(panelX + buttonWidth + 10, y, buttonWidth, 20)
            .build();
        this.addDrawableChild(logModeButton);
        
        y += 30;
        
        int selectorHeight = (this.height - y - 50) / 2 - 5;
        
        // S2C Selector
        this.s2cSelector = new DualListSelectorWidget(
            panelX, y, panelWidth, selectorHeight,
            "S2C Packets (Server → Client)",
            S2C_PACKETS,
            new HashSet<>(config.selectedS2CPackets),
            selection -> {}
        );
        this.addDrawableChild(s2cSelector);
        
        y += selectorHeight + 10;
        
        // C2S Selector
        this.c2sSelector = new DualListSelectorWidget(
            panelX, y, panelWidth, selectorHeight,
            "C2S Packets (Client → Server)",
            C2S_PACKETS,
            new HashSet<>(config.selectedC2SPackets),
            selection -> {}
        );
        this.addDrawableChild(c2sSelector);
        
        int bottomY = this.height - 28;
        int bottomButtonWidth = 100;
        
        this.addDrawableChild(
            ButtonWidget.builder(Text.literal("Save"), button -> this.saveAndClose())
                .dimensions(this.width / 2 - bottomButtonWidth - 5, bottomY, bottomButtonWidth, 20)
                .build()
        );
        
        this.addDrawableChild(
            ButtonWidget.builder(Text.literal("Cancel"), button -> this.cancelAndClose())
                .dimensions(this.width / 2 + 5, bottomY, bottomButtonWidth, 20)
                .build()
        );
    }
    
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fillGradient(0, 0, this.width, this.height, 0xA0101010, 0xB0101010);
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        
        int panelWidth = Math.min(500, this.width - 40);
        int panelX = (this.width - panelWidth) / 2;
        int panelY = 20;
        int panelHeight = this.height - 55;
        
        context.fill(panelX - 2, panelY - 2, panelX + panelWidth + 2, panelY + panelHeight + 2, 0xFF2A2A2A);
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xE0181818);
        
        super.render(context, mouseX, mouseY, delta);
        
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 8, 0xFFFFFF);
    }
    
    private void persistToConfig() {
        config.logPackets = logPacketsEnabled;
        config.logMode = currentLogMode;
        config.selectedS2CPackets = new ArrayList<>(s2cSelector.getSelectedPackets());
        config.selectedC2SPackets = new ArrayList<>(c2sSelector.getSelectedPackets());
        config.save();
    }
    
    private void saveAndClose() {
        persistToConfig();
        this.close();
    }
    
    private void cancelAndClose() {
        logPacketsEnabled = config.logPackets;
        currentLogMode = config.logMode;
        config.loggingKeybind = initialKeybind;
        PacketLoggerClient.setLoggingKeybind(config.loggingKeybind);
        s2cSelector.setSelectedPackets(initialS2C);
        c2sSelector.setSelectedPackets(initialC2S);
        persistToConfig();
        this.close();
    }
    
    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }
    
    private String keybindButtonLabel() {
        if (config.loggingKeybind == -1) {
            return "Keybind";
        }
        String name = InputUtil.fromKeyCode(new KeyInput(config.loggingKeybind, 0, 0)).getTranslationKey();
        return (name.startsWith("key.keyboard.") ? name.substring("key.keyboard.".length()) : name).toUpperCase();
    }
    
    @Override
    public boolean keyPressed(KeyInput input) {
        if (waitingForKeybind) {
            if (input.key() == InputUtil.GLFW_KEY_ESCAPE) {
                config.loggingKeybind = -1;
            } else {
                config.loggingKeybind = input.key();
            }
            waitingForKeybind = false;
            PacketLoggerClient.setLoggingKeybind(config.loggingKeybind);
            this.keybindButton.setMessage(Text.literal(keybindButtonLabel()));
            return true;
        }
        if (s2cSelector != null && s2cSelector.keyPressed(input)) {
            return true;
        }
        if (c2sSelector != null && c2sSelector.keyPressed(input)) {
            return true;
        }
        return super.keyPressed(input);
    }
    
    @Override
    public boolean charTyped(CharInput input) {
        if (s2cSelector != null && s2cSelector.charTyped(input)) {
            return true;
        }
        if (c2sSelector != null && c2sSelector.charTyped(input)) {
            return true;
        }
        return super.charTyped(input);
    }
}
*///?} else {
public class SimpleConfigScreen extends Screen {
    private final Screen parent;
    private final ModConfig config;
    
    // Widgets
    private ButtonWidget logPacketsButton;
    private ButtonWidget logModeButton;
    private ButtonWidget keybindButton;
    private DualListSelectorWidget s2cSelector;
    private DualListSelectorWidget c2sSelector;
    
    private boolean logPacketsEnabled;
    private LogMode currentLogMode;
    private boolean waitingForKeybind;
    private final Set<String> initialS2C;
    private final Set<String> initialC2S;
    private final int initialKeybind;
    
    // Vollständige Liste S2C Pakete (Server to Client)
    private static final List<String> S2C_PACKETS = PacketCatalog.getS2CPacketNames();
    
    // Vollständige Liste C2S Pakete (Client to Server)
    private static final List<String> C2S_PACKETS = PacketCatalog.getC2SPacketNames();

    public SimpleConfigScreen(Screen parent) {
        super(Text.literal("Packet Logger"));
        this.parent = parent;
        this.config = ModConfig.getInstance();
        this.logPacketsEnabled = config.logPackets;
        this.currentLogMode = config.logMode;
        this.initialS2C = new HashSet<>(config.selectedS2CPackets);
        this.initialC2S = new HashSet<>(config.selectedC2SPackets);
        this.initialKeybind = config.loggingKeybind;
    }
    
    @Override
    protected void init() {
        super.init();
        
        int panelWidth = Math.min(500, this.width - 40);
        int panelX = (this.width - panelWidth) / 2;
        int panelY = 25;
        
        int buttonWidth = (panelWidth - 10) / 2;
        int KEYBIND_GAP = 3;
        int logButtonWidth = (buttonWidth - KEYBIND_GAP) * 2 / 3;
        int keybindButtonWidth = buttonWidth - KEYBIND_GAP - logButtonWidth;
        int y = panelY + 5;
        
        // Log Packets Toggle Button
        this.logPacketsButton = ButtonWidget.builder(
            Text.literal("Logging: " + (logPacketsEnabled ? "§aON" : "§cOFF")),
            button -> {
                logPacketsEnabled = !logPacketsEnabled;
                button.setMessage(Text.literal("Logging: " + (logPacketsEnabled ? "§aON" : "§cOFF")));
            })
            .dimensions(panelX, y, logButtonWidth, 20)
            .build();
        this.addDrawableChild(logPacketsButton);
        
        // Logging Keybind Setting Button
        this.keybindButton = ButtonWidget.builder(
            Text.literal(keybindButtonLabel()),
            button -> {
                waitingForKeybind = true;
                button.setMessage(Text.literal("..."));
            })
            .dimensions(panelX + logButtonWidth + KEYBIND_GAP, y, keybindButtonWidth, 20)
            .build();
        this.addDrawableChild(keybindButton);
        
        // Log Mode Toggle Button
        this.logModeButton = ButtonWidget.builder(
            Text.literal("Output: " + currentLogMode.getDisplayName()),
            button -> {
                currentLogMode = currentLogMode.next();
                button.setMessage(Text.literal("Output: " + currentLogMode.getDisplayName()));
            })
            .dimensions(panelX + buttonWidth + 10, y, buttonWidth, 20)
            .build();
        this.addDrawableChild(logModeButton);
        
        y += 30;
        
        int selectorHeight = (this.height - y - 50) / 2 - 5;
        
        // S2C Selector
        this.s2cSelector = new DualListSelectorWidget(
            panelX, y, panelWidth, selectorHeight,
            "S2C Packets (Server → Client)",
            S2C_PACKETS,
            new HashSet<>(config.selectedS2CPackets),
            selection -> {}
        );
        this.addDrawableChild(s2cSelector);
        
        y += selectorHeight + 10;
        
        // C2S Selector
        this.c2sSelector = new DualListSelectorWidget(
            panelX, y, panelWidth, selectorHeight,
            "C2S Packets (Client → Server)",
            C2S_PACKETS,
            new HashSet<>(config.selectedC2SPackets),
            selection -> {}
        );
        this.addDrawableChild(c2sSelector);
        
        int bottomY = this.height - 28;
        int bottomButtonWidth = 100;
        
        this.addDrawableChild(
            ButtonWidget.builder(Text.literal("Save"), button -> this.saveAndClose())
                .dimensions(this.width / 2 - bottomButtonWidth - 5, bottomY, bottomButtonWidth, 20)
                .build()
        );
        
        this.addDrawableChild(
            ButtonWidget.builder(Text.literal("Cancel"), button -> this.cancelAndClose())
                .dimensions(this.width / 2 + 5, bottomY, bottomButtonWidth, 20)
                .build()
        );
    }
    
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fillGradient(0, 0, this.width, this.height, 0xA0101010, 0xB0101010);
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        
        int panelWidth = Math.min(500, this.width - 40);
        int panelX = (this.width - panelWidth) / 2;
        int panelY = 20;
        int panelHeight = this.height - 55;
        
        context.fill(panelX - 2, panelY - 2, panelX + panelWidth + 2, panelY + panelHeight + 2, 0xFF2A2A2A);
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xE0181818);
        
        super.render(context, mouseX, mouseY, delta);
        
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 8, 0xFFFFFF);
    }
    
    private void persistToConfig() {
        config.logPackets = logPacketsEnabled;
        config.logMode = currentLogMode;
        config.selectedS2CPackets = new ArrayList<>(s2cSelector.getSelectedPackets());
        config.selectedC2SPackets = new ArrayList<>(c2sSelector.getSelectedPackets());
        config.save();
    }
    
    private void saveAndClose() {
        persistToConfig();
        this.close();
    }
    
    private void cancelAndClose() {
        logPacketsEnabled = config.logPackets;
        currentLogMode = config.logMode;
        config.loggingKeybind = initialKeybind;
        PacketLoggerClient.setLoggingKeybind(config.loggingKeybind);
        s2cSelector.setSelectedPackets(initialS2C);
        c2sSelector.setSelectedPackets(initialC2S);
        persistToConfig();
        this.close();
    }
    
    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }
    
    private String keybindButtonLabel() {
        if (config.loggingKeybind == -1) {
            return "Keybind";
        }
        String name = InputUtil.fromKeyCode(config.loggingKeybind, 0).getTranslationKey();
        return (name.startsWith("key.keyboard.") ? name.substring("key.keyboard.".length()) : name).toUpperCase();
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (waitingForKeybind) {
            if (keyCode == InputUtil.GLFW_KEY_ESCAPE) {
                config.loggingKeybind = -1;
            } else {
                config.loggingKeybind = keyCode;
            }
            waitingForKeybind = false;
            PacketLoggerClient.setLoggingKeybind(config.loggingKeybind);
            this.keybindButton.setMessage(Text.literal(keybindButtonLabel()));
            return true;
        }
        if (s2cSelector != null && s2cSelector.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (c2sSelector != null && c2sSelector.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (s2cSelector != null && s2cSelector.charTyped(chr, modifiers)) {
            return true;
        }
        if (c2sSelector != null && c2sSelector.charTyped(chr, modifiers)) {
            return true;
        }
        return super.charTyped(chr, modifiers);
    }
}
//?}