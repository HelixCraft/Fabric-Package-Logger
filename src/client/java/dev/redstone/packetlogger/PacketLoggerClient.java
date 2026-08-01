package dev.redstone.packetlogger;

import dev.redstone.packetlogger.config.ModConfig;
import dev.redstone.packetlogger.screen.SimpleConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

public class PacketLoggerClient implements ClientModInitializer {
	private static KeyMapping configKeyBinding;

	// Eigene Keybinding-Kategorie (in 26.2 ist die Kategorie ein KeyMapping.Category-Objekt, kein String)
	private static final KeyMapping.Category CATEGORY =
		KeyMapping.Category.register(Identifier.fromNamespaceAndPath("packet-logger", "main"));

	@Override
	public void onInitializeClient() {
		// Lade Konfiguration
		ModConfig.load();

		// Registriere Keybinding (F6 = Config)
		configKeyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.packet-logger.config",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_F6,
			CATEGORY
		));

		// Registriere Tick-Event für Keybinding
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (configKeyBinding.consumeClick()) {
				if (client.gui.screen() == null) {
					client.setScreenAndShow(new SimpleConfigScreen(null));
				}
			}
		});

		System.out.println("[PacketLogger] Initialized! Press F6 to open config.");
	}
}
