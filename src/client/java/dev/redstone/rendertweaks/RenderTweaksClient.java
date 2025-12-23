package dev.redstone.rendertweaks;

import dev.redstone.rendertweaks.config.ModConfig;
import dev.redstone.rendertweaks.screen.SimpleConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class RenderTweaksClient implements ClientModInitializer {
	private static KeyBinding configKeyBinding;
	
	@Override
	public void onInitializeClient() {
		// Lade Konfiguration
		ModConfig.load();
		
		// Registriere Keybinding (R + C = Config)
		configKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.render-tweaks.config",
			InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_R,
			"category.render-tweaks"
		));
		
		// Registriere Tick-Event für Keybinding
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (configKeyBinding.wasPressed()) {
				if (client.currentScreen == null) {
					client.setScreen(new SimpleConfigScreen(null));
				}
			}
		});
		
		System.out.println("Render Tweaks initialized! Press 'R' to open config.");
	}
}