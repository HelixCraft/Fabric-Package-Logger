package dev.redstone.packetlogger;

import dev.redstone.packetlogger.config.ModConfig;
import dev.redstone.packetlogger.screen.SimpleConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
//? if >=26.1 {
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.InputConstants;
//?} else {
/*import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
*///?}
import org.lwjgl.glfw.GLFW;

public class PacketLoggerClient implements ClientModInitializer {
	//? if >=26.1 {
	private static KeyMapping configKeyBinding;
	private static final KeyMapping.Category CATEGORY =
		KeyMapping.Category.register(Identifier.fromNamespaceAndPath("packet-logger", "main"));
	//?} else {
	/*private static KeyBinding configKeyBinding;
	*///?}
	
	@Override
	public void onInitializeClient() {
		// Lade Konfiguration
		ModConfig.load();
		
		// Registriere Keybinding (F6 = Config)
		//? if >=26.1 {
		configKeyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.packet-logger.config",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_F6,
			CATEGORY
		));
		//?} elif >=1.21.9 {
		/*configKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.packet-logger.config",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_F6,
				new net.minecraft.client.option.KeyBinding.Category(
						net.minecraft.util.Identifier.of("packet-logger", "category"))));
		*///?} else {
		/*configKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.packet-logger.config",
			InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_F6,
			"category.packet-logger"
		));
		*///?}
		
		// Registriere Tick-Event für Keybinding
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			//? if >=26.2 {
			while (configKeyBinding.consumeClick()) {
				if (client.gui.screen() == null) {
					client.gui.setScreen(new SimpleConfigScreen(null));
				}
			}
			//?} elif >=26.1 {
			/*while (configKeyBinding.consumeClick()) {
				if (client.screen == null) {
					client.setScreenAndShow(new SimpleConfigScreen(null));
				}
			}
			*///?} else {
			/*while (configKeyBinding.wasPressed()) {
				if (client.currentScreen == null) {
					client.setScreen(new SimpleConfigScreen(null));
				}
			}
			*///?}
		});
		
		System.out.println("[PacketLogger] Initialized! Press F6 to open config.");
	}
}