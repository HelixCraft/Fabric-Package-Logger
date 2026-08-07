package dev.redstone.packetlogger.mixin.client;

import dev.redstone.packetlogger.logger.PacketLogger;
//? if >=26.1 {
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
//?} else {
/*import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
*///?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin um World Join/Leave Events zu erkennen.
 */
//? if >=26.1 {
@Mixin(ClientPacketListener.class)
//?} else {
/*@Mixin(ClientPlayNetworkHandler.class)*/
//?}
public class ClientPlayNetworkHandlerMixin {
    
    //? if >=26.1 {
    @Inject(method = "handleLogin", at = @At("TAIL"))
    //?} else {
    /*@Inject(method = "onGameJoin", at = @At("TAIL"))*/
    //?}
    private void onGameJoin(
//? if >=26.1 {
        ClientboundLoginPacket packet,
//?} else {
        /*GameJoinS2CPacket packet,*/
//?}
        CallbackInfo ci) {
        PacketLogger.onWorldJoin("world");
    }
}
