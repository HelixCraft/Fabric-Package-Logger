package dev.redstone.packetlogger.mixin.client;

import dev.redstone.packetlogger.logger.PacketLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin um World Leave Event zu erkennen.
 */
@Mixin(Minecraft.class)
public class MinecraftClientMixin {

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;Z)V", at = @At("HEAD"))
    private void onDisconnect(Screen disconnectionScreen, boolean transferring, CallbackInfo ci) {
        PacketLogger.onWorldLeave();
    }
}
