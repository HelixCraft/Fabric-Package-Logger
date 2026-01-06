package dev.redstone.packetlogger.mixin.client;

import dev.redstone.packetlogger.logger.PacketLogger;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin um World Leave Event zu erkennen.
 */
@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    
    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;Z)V", at = @At("HEAD"))
    private void onDisconnect(Screen disconnectionScreen, boolean transferring, CallbackInfo ci) {
        PacketLogger.onWorldLeave();
    }
}
