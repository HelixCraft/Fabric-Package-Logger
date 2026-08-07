package dev.redstone.packetlogger.mixin.client;

import dev.redstone.packetlogger.logger.PacketLogger;
//? if >=26.1 {
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
//?} else {
/*import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
*///?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin um World Leave Event zu erkennen.
 */
//? if >=26.1 {
@Mixin(Minecraft.class)
//?} else {
/*@Mixin(MinecraftClient.class)*/
//?}
public class MinecraftClientMixin {
    
    //? if >=26.1 {
    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;Z)V", at = @At("HEAD"))
    //?} else {
    /*@Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;Z)V", at = @At("HEAD"))*/
    //?}
    private void onDisconnect(Screen disconnectionScreen, boolean transferring, CallbackInfo ci) {
        PacketLogger.onWorldLeave();
    }
}
