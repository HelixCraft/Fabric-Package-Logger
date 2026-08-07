package dev.redstone.packetlogger.logger;

import dev.redstone.packetlogger.config.ModConfig;
import dev.redstone.packetlogger.logger.unpacker.*;
import net.fabricmc.loader.api.FabricLoader;
//? if >=26.1 {
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
//?} else {
/*import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
*///?}

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Deep Packet Logger - Loggt alle Netzwerk-Pakete mit vollständigen Daten.
 */
public class PacketLogger {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private static Path currentLogFile = null;
    private static String currentSessionId = null;
    private static boolean wasLoggingEnabled = false;

    private static final Map<Class<?>, PacketUnpacker<?>> UNPACKERS = new HashMap<>();
    private static final Map<Class<?>, String> PACKET_NAMES = new HashMap<>();

    static {
        registerUnpackers();
    }

    //? if >=26.1 {
    private static void registerUnpackers() {
        // Inventory/Item Pakete
        registerPacket(ClientboundContainerSetContentPacket.class, "InventoryS2CPacket", new InventoryS2CUnpacker());
        registerPacket(ClientboundContainerSetSlotPacket.class, "ScreenHandlerSlotUpdateS2CPacket", new SlotUpdateS2CUnpacker());
        registerPacket(ServerboundSetCreativeModeSlotPacket.class, "CreativeInventoryActionC2SPacket", new CreativeInventoryC2SUnpacker());
        registerPacket(ServerboundContainerClickPacket.class, "ClickSlotC2SPacket", new ClickSlotC2SUnpacker());

        // Block Pakete
        registerPacket(ClientboundBlockEntityDataPacket.class, "BlockEntityUpdateS2CPacket", new BlockEntityUpdateS2CUnpacker());
        registerPacket(ClientboundBlockUpdatePacket.class, "BlockUpdateS2CPacket", new BlockUpdateS2CUnpacker());
        registerPacket(ClientboundSectionBlocksUpdatePacket.class, "ChunkDeltaUpdateS2CPacket", new ChunkDeltaUpdateS2CUnpacker());

        // Entity Pakete
        registerPacket(ClientboundSetEntityDataPacket.class, "EntityTrackerUpdateS2CPacket", new EntityTrackerUpdateS2CUnpacker());
        registerPacket(ClientboundUpdateAttributesPacket.class, "EntityAttributesS2CPacket", new EntityAttributesS2CUnpacker());
        registerPacket(ClientboundAddEntityPacket.class, "EntitySpawnS2CPacket", new EntitySpawnS2CUnpacker());

        // Chunk Pakete
        registerPacket(ClientboundLevelChunkWithLightPacket.class, "ChunkDataS2CPacket", new ChunkDataS2CUnpacker());

        // NBT/Custom Pakete
        registerPacket(ClientboundTagQueryPacket.class, "NbtQueryResponseS2CPacket", new NbtQueryResponseS2CUnpacker());
        registerPacket(ClientboundCustomPayloadPacket.class, "CustomPayloadS2CPacket", new CustomPayloadS2CUnpacker());
        registerPacket(ServerboundCustomPayloadPacket.class, "CustomPayloadC2SPacket", new CustomPayloadC2SUnpacker());

        // Weitere Pakete (nur Namen-Mapping)
        registerPacketName(ClientboundLoginPacket.class, "GameJoinS2CPacket");
        registerPacketName(ClientboundPlayerPositionPacket.class, "PlayerPositionLookS2CPacket");
        registerPacketName(ClientboundOpenScreenPacket.class, "OpenScreenS2CPacket");
        registerPacketName(ClientboundContainerClosePacket.class, "CloseScreenS2CPacket");
        registerPacketName(ClientboundSetEquipmentPacket.class, "EntityEquipmentUpdateS2CPacket");
        registerPacketName(ClientboundEntityPositionSyncPacket.class, "EntityPositionS2CPacket");
        registerPacketName(ClientboundSetEntityMotionPacket.class, "EntityVelocityUpdateS2CPacket");
        registerPacketName(ClientboundSetHealthPacket.class, "HealthUpdateS2CPacket");
        registerPacketName(ClientboundSetExperiencePacket.class, "ExperienceBarUpdateS2CPacket");
        registerPacketName(ClientboundPlayerChatPacket.class, "ChatMessageS2CPacket");
        registerPacketName(ClientboundSystemChatPacket.class, "GameMessageS2CPacket");
        registerPacketName(ClientboundLevelParticlesPacket.class, "ParticleS2CPacket");
        registerPacketName(ClientboundSoundPacket.class, "PlaySoundS2CPacket");
        registerPacketName(ClientboundSetTimePacket.class, "WorldTimeUpdateS2CPacket");

        // Bindung/Bewegung von Entities - fehlten in PACKET_NAMES, daher wurde ihr Mojang-Name
        // (z.B. "ClientboundSetPassengersPacket") nie gegen den Yarn-Namen im Whitelist gematcht
        // und sie tauchten NIE im Log auf. Jetzt korrekt gemappt:
        registerPacketName(ClientboundBundlePacket.class, "BundleS2CPacket");
        registerPacketName(ClientboundSetPassengersPacket.class, "EntityPassengersSetS2CPacket");
        registerPacketName(ClientboundSetEntityLinkPacket.class, "EntityAttachS2CPacket");
        registerPacketName(ClientboundTeleportEntityPacket.class, "EntityPositionS2CPacket");
        registerPacketName(ClientboundMoveEntityPacket.Pos.class, "EntityS2CPacket");
        registerPacketName(ClientboundMoveEntityPacket.Rot.class, "EntityS2CPacket");
        registerPacketName(ClientboundMoveEntityPacket.PosRot.class, "EntityS2CPacket");
        registerPacketName(ClientboundRemoveEntitiesPacket.class, "EntitiesDestroyS2CPacket");

        // --- Restliche Whitelist-Pakete (waren in der GUI auswaehlbar, aber nie gemappt) ---
        // Ohne diese Zeilen matchte der Yarn-Name aus der Whitelist nie gegen den Mojang-Klassennamen,
        // d.h. Sounds/Titles/Subtitles/Scoreboard/WorldBorder etc. tauchten NIE im Log auf.
        // S2C (alle in net.minecraft.network.protocol.game.*):
        registerPacketName(ClientboundUpdateAdvancementsPacket.class, "AdvancementUpdateS2CPacket");
        registerPacketName(ClientboundBlockDestructionPacket.class, "BlockBreakingProgressS2CPacket");
        registerPacketName(ClientboundBlockEventPacket.class, "BlockEventS2CPacket");
        registerPacketName(ClientboundBossEventPacket.class, "BossBarS2CPacket");
        registerPacketName(ClientboundCustomChatCompletionsPacket.class, "ChatSuggestionsS2CPacket");
        registerPacketName(ClientboundChunksBiomesPacket.class, "ChunkBiomeDataS2CPacket");
        registerPacketName(ClientboundSetChunkCacheRadiusPacket.class, "ChunkLoadDistanceS2CPacket");
        registerPacketName(ClientboundSetChunkCacheCenterPacket.class, "ChunkRenderDistanceCenterS2CPacket");
        registerPacketName(ClientboundChunkBatchFinishedPacket.class, "ChunkSentS2CPacket");
        registerPacketName(ClientboundClearTitlesPacket.class, "ClearTitleS2CPacket");
        registerPacketName(ClientboundCommandSuggestionsPacket.class, "CommandSuggestionsS2CPacket");
        registerPacketName(ClientboundCommandsPacket.class, "CommandTreeS2CPacket");
        registerPacketName(ClientboundCooldownPacket.class, "CooldownUpdateS2CPacket");
        registerPacketName(ClientboundPlaceGhostRecipePacket.class, "CraftFailedResponseS2CPacket");
        registerPacketName(ClientboundHurtAnimationPacket.class, "DamageTiltS2CPacket");
        registerPacketName(ClientboundPlayerCombatKillPacket.class, "DeathMessageS2CPacket");
        registerPacketName(ClientboundDebugSamplePacket.class, "DebugSampleS2CPacket");
        registerPacketName(ClientboundChangeDifficultyPacket.class, "DifficultyS2CPacket");
        registerPacketName(ClientboundPlayerCombatEndPacket.class, "EndCombatS2CPacket");
        registerPacketName(ClientboundPlayerCombatEnterPacket.class, "EnterCombatS2CPacket");
        registerPacketName(ClientboundStartConfigurationPacket.class, "EnterReconfigurationS2CPacket");
        registerPacketName(ClientboundAnimatePacket.class, "EntityAnimationS2CPacket");
        registerPacketName(ClientboundDamageEventPacket.class, "EntityDamageS2CPacket");
        registerPacketName(ClientboundRotateHeadPacket.class, "EntitySetHeadYawS2CPacket");
        registerPacketName(ClientboundUpdateMobEffectPacket.class, "EntityStatusEffectS2CPacket");
        registerPacketName(ClientboundEntityEventPacket.class, "EntityStatusS2CPacket");
        registerPacketName(ClientboundExplodePacket.class, "ExplosionS2CPacket");
        registerPacketName(ClientboundGameEventPacket.class, "GameStateChangeS2CPacket");
        registerPacketName(ClientboundTakeItemEntityPacket.class, "ItemPickupAnimationS2CPacket");
        registerPacketName(ClientboundLightUpdatePacket.class, "LightUpdateS2CPacket");
        registerPacketName(ClientboundPlayerLookAtPacket.class, "LookAtS2CPacket");
        registerPacketName(ClientboundMapItemDataPacket.class, "MapUpdateS2CPacket");
        registerPacketName(ClientboundMountScreenOpenPacket.class, "OpenHorseScreenS2CPacket");
        registerPacketName(ClientboundOpenBookPacket.class, "OpenWrittenBookS2CPacket");
        registerPacketName(ClientboundSetActionBarTextPacket.class, "OverlayMessageS2CPacket");
        registerPacketName(ClientboundSoundEntityPacket.class, "PlaySoundFromEntityS2CPacket");
        registerPacketName(ClientboundPlayerAbilitiesPacket.class, "PlayerAbilitiesS2CPacket");
        registerPacketName(ClientboundBlockChangedAckPacket.class, "PlayerActionResponseS2CPacket");
        registerPacketName(ClientboundTabListPacket.class, "PlayerListHeaderS2CPacket");
        registerPacketName(ClientboundPlayerInfoUpdatePacket.class, "PlayerListS2CPacket");
        registerPacketName(ClientboundPlayerInfoRemovePacket.class, "PlayerRemoveS2CPacket");
        registerPacketName(ClientboundRespawnPacket.class, "PlayerRespawnS2CPacket");
        registerPacketName(ClientboundSetDefaultSpawnPositionPacket.class, "PlayerSpawnPositionS2CPacket");
        registerPacketName(ClientboundDisguisedChatPacket.class, "ProfilelessChatMessageS2CPacket");
        registerPacketName(ClientboundProjectilePowerPacket.class, "ProjectilePowerS2CPacket");
        registerPacketName(ClientboundRemoveMobEffectPacket.class, "RemoveEntityStatusEffectS2CPacket");
        registerPacketName(ClientboundDeleteChatPacket.class, "RemoveMessageS2CPacket");
        registerPacketName(ClientboundSetDisplayObjectivePacket.class, "ScoreboardDisplayS2CPacket");
        registerPacketName(ClientboundSetObjectivePacket.class, "ScoreboardObjectiveUpdateS2CPacket");
        registerPacketName(ClientboundResetScorePacket.class, "ScoreboardScoreResetS2CPacket");
        registerPacketName(ClientboundSetScorePacket.class, "ScoreboardScoreUpdateS2CPacket");
        registerPacketName(ClientboundContainerSetDataPacket.class, "ScreenHandlerPropertyUpdateS2CPacket");
        registerPacketName(ClientboundSelectAdvancementsTabPacket.class, "SelectAdvancementTabS2CPacket");
        registerPacketName(ClientboundServerDataPacket.class, "ServerMetadataS2CPacket");
        registerPacketName(ClientboundSetCameraPacket.class, "SetCameraEntityS2CPacket");
        registerPacketName(ClientboundMerchantOffersPacket.class, "SetTradeOffersS2CPacket");
        registerPacketName(ClientboundOpenSignEditorPacket.class, "SignEditorOpenS2CPacket");
        registerPacketName(ClientboundSetSimulationDistancePacket.class, "SimulationDistanceS2CPacket");
        registerPacketName(ClientboundChunkBatchStartPacket.class, "StartChunkSendS2CPacket");
        registerPacketName(ClientboundAwardStatsPacket.class, "StatisticsS2CPacket");
        registerPacketName(ClientboundStopSoundPacket.class, "StopSoundS2CPacket");
        registerPacketName(ClientboundSetSubtitleTextPacket.class, "SubtitleS2CPacket");
        registerPacketName(ClientboundUpdateRecipesPacket.class, "SynchronizeRecipesS2CPacket");
        registerPacketName(ClientboundSetPlayerTeamPacket.class, "TeamS2CPacket");
        registerPacketName(ClientboundTickingStepPacket.class, "TickStepS2CPacket");
        registerPacketName(ClientboundSetTitlesAnimationPacket.class, "TitleFadeS2CPacket");
        registerPacketName(ClientboundSetTitleTextPacket.class, "TitleS2CPacket");
        registerPacketName(ClientboundForgetLevelChunkPacket.class, "UnloadChunkS2CPacket");
        registerPacketName(ClientboundSetHeldSlotPacket.class, "UpdateSelectedSlotS2CPacket");
        registerPacketName(ClientboundTickingStatePacket.class, "UpdateTickRateS2CPacket");
        registerPacketName(ClientboundMoveVehiclePacket.class, "VehicleMoveS2CPacket");
        registerPacketName(ClientboundSetBorderCenterPacket.class, "WorldBorderCenterChangedS2CPacket");
        registerPacketName(ClientboundInitializeBorderPacket.class, "WorldBorderInitializeS2CPacket");
        registerPacketName(ClientboundSetBorderLerpSizePacket.class, "WorldBorderInterpolateSizeS2CPacket");
        registerPacketName(ClientboundSetBorderSizePacket.class, "WorldBorderSizeChangedS2CPacket");
        registerPacketName(ClientboundSetBorderWarningDistancePacket.class, "WorldBorderWarningBlocksChangedS2CPacket");
        registerPacketName(ClientboundSetBorderWarningDelayPacket.class, "WorldBorderWarningTimeChangedS2CPacket");
        registerPacketName(ClientboundLevelEventPacket.class, "WorldEventS2CPacket");
        // ChangeUnlockedRecipes wurde in 26.2 in drei Pakete aufgeteilt -> alle auf den alten Namen mappen:
        registerPacketName(ClientboundRecipeBookAddPacket.class, "ChangeUnlockedRecipesS2CPacket");
        registerPacketName(ClientboundRecipeBookRemovePacket.class, "ChangeUnlockedRecipesS2CPacket");
        registerPacketName(ClientboundRecipeBookSettingsPacket.class, "ChangeUnlockedRecipesS2CPacket");
        // ExperienceOrbSpawnS2CPacket: in 26.2 entfernt (Orbs spawnen via ClientboundAddEntityPacket) -> kein Mapping.

        // C2S (alle in net.minecraft.network.protocol.game.*):
        registerPacketName(ServerboundChunkBatchReceivedPacket.class, "AcknowledgeChunksC2SPacket");
        registerPacketName(ServerboundConfigurationAcknowledgedPacket.class, "AcknowledgeReconfigurationC2SPacket");
        registerPacketName(ServerboundSeenAdvancementsPacket.class, "AdvancementTabC2SPacket");
        registerPacketName(ServerboundPaddleBoatPacket.class, "BoatPaddleStateC2SPacket");
        registerPacketName(ServerboundEditBookPacket.class, "BookUpdateC2SPacket");
        registerPacketName(ServerboundContainerButtonClickPacket.class, "ButtonClickC2SPacket");
        registerPacketName(ServerboundChatCommandSignedPacket.class, "ChatCommandSignedC2SPacket");
        registerPacketName(ServerboundChatPacket.class, "ChatMessageC2SPacket");
        registerPacketName(ServerboundPlayerCommandPacket.class, "ClientCommandC2SPacket");
        registerPacketName(ServerboundClientCommandPacket.class, "ClientStatusC2SPacket");
        registerPacketName(ServerboundContainerClosePacket.class, "CloseHandledScreenC2SPacket");
        registerPacketName(ServerboundChatCommandPacket.class, "CommandExecutionC2SPacket");
        registerPacketName(ServerboundPlaceRecipePacket.class, "CraftRequestC2SPacket");
        registerPacketName(ServerboundDebugSubscriptionRequestPacket.class, "DebugSampleSubscriptionC2SPacket");
        registerPacketName(ServerboundSwingPacket.class, "HandSwingC2SPacket");
        registerPacketName(ServerboundJigsawGeneratePacket.class, "JigsawGeneratingC2SPacket");
        registerPacketName(ServerboundChatAckPacket.class, "MessageAcknowledgmentC2SPacket");
        registerPacketName(ServerboundPlayerActionPacket.class, "PlayerActionC2SPacket");
        registerPacketName(ServerboundPlayerInputPacket.class, "PlayerInputC2SPacket");
        registerPacketName(ServerboundUseItemOnPacket.class, "PlayerInteractBlockC2SPacket");
        registerPacketName(ServerboundInteractPacket.class, "PlayerInteractEntityC2SPacket");
        registerPacketName(ServerboundUseItemPacket.class, "PlayerInteractItemC2SPacket");
        // PlayerMove: Basis ServerboundMovePlayerPacket ist abstrakt -> 4 Wire-Subklassen einzeln mappen:
        registerPacketName(ServerboundMovePlayerPacket.Pos.class, "PlayerMoveC2SPacket");
        registerPacketName(ServerboundMovePlayerPacket.PosRot.class, "PlayerMoveC2SPacket");
        registerPacketName(ServerboundMovePlayerPacket.Rot.class, "PlayerMoveC2SPacket");
        registerPacketName(ServerboundMovePlayerPacket.StatusOnly.class, "PlayerMoveC2SPacket");
        registerPacketName(ServerboundChatSessionUpdatePacket.class, "PlayerSessionC2SPacket");
        registerPacketName(ServerboundBlockEntityTagQueryPacket.class, "QueryBlockNbtC2SPacket");
        registerPacketName(ServerboundEntityTagQueryPacket.class, "QueryEntityNbtC2SPacket");
        registerPacketName(ServerboundRecipeBookSeenRecipePacket.class, "RecipeBookDataC2SPacket");
        registerPacketName(ServerboundRecipeBookChangeSettingsPacket.class, "RecipeCategoryOptionsC2SPacket");
        registerPacketName(ServerboundRenameItemPacket.class, "RenameItemC2SPacket");
        registerPacketName(ServerboundCommandSuggestionPacket.class, "RequestCommandCompletionsC2SPacket");
        registerPacketName(ServerboundSelectTradePacket.class, "SelectMerchantTradeC2SPacket");
        registerPacketName(ServerboundContainerSlotStateChangedPacket.class, "SlotChangedStateC2SPacket");
        registerPacketName(ServerboundTeleportToEntityPacket.class, "SpectatorTeleportC2SPacket");
        registerPacketName(ServerboundAcceptTeleportationPacket.class, "TeleportConfirmC2SPacket");
        registerPacketName(ServerboundSetBeaconPacket.class, "UpdateBeaconC2SPacket");
        registerPacketName(ServerboundSetCommandBlockPacket.class, "UpdateCommandBlockC2SPacket");
        registerPacketName(ServerboundSetCommandMinecartPacket.class, "UpdateCommandBlockMinecartC2SPacket");
        registerPacketName(ServerboundChangeDifficultyPacket.class, "UpdateDifficultyC2SPacket");
        registerPacketName(ServerboundLockDifficultyPacket.class, "UpdateDifficultyLockC2SPacket");
        registerPacketName(ServerboundSetJigsawBlockPacket.class, "UpdateJigsawC2SPacket");
        registerPacketName(ServerboundPlayerAbilitiesPacket.class, "UpdatePlayerAbilitiesC2SPacket");
        registerPacketName(ServerboundSetCarriedItemPacket.class, "UpdateSelectedSlotC2SPacket");
        registerPacketName(ServerboundSignUpdatePacket.class, "UpdateSignC2SPacket");
        registerPacketName(ServerboundSetStructureBlockPacket.class, "UpdateStructureBlockC2SPacket");
        registerPacketName(ServerboundMoveVehiclePacket.class, "VehicleMoveC2SPacket");
        // PickFromInventory wurde in 26.2 in zwei Pakete aufgeteilt:
        registerPacketName(ServerboundPickItemFromBlockPacket.class, "PickFromInventoryC2SPacket");
        registerPacketName(ServerboundPickItemFromEntityPacket.class, "PickFromInventoryC2SPacket");
    }
    //?} else {
    /*private static void registerUnpackers() {
        // Inventory/Item Pakete
        registerPacket(InventoryS2CPacket.class, "InventoryS2CPacket", new InventoryS2CUnpacker());
        registerPacket(ScreenHandlerSlotUpdateS2CPacket.class, "ScreenHandlerSlotUpdateS2CPacket", new SlotUpdateS2CUnpacker());
        registerPacket(CreativeInventoryActionC2SPacket.class, "CreativeInventoryActionC2SPacket", new CreativeInventoryC2SUnpacker());
        registerPacket(ClickSlotC2SPacket.class, "ClickSlotC2SPacket", new ClickSlotC2SUnpacker());

        // Block Pakete
        registerPacket(BlockEntityUpdateS2CPacket.class, "BlockEntityUpdateS2CPacket", new BlockEntityUpdateS2CUnpacker());
        registerPacket(BlockUpdateS2CPacket.class, "BlockUpdateS2CPacket", new BlockUpdateS2CUnpacker());
        registerPacket(ChunkDeltaUpdateS2CPacket.class, "ChunkDeltaUpdateS2CPacket", new ChunkDeltaUpdateS2CUnpacker());

        // Entity Pakete
        registerPacket(EntityTrackerUpdateS2CPacket.class, "EntityTrackerUpdateS2CPacket", new EntityTrackerUpdateS2CUnpacker());
        registerPacket(EntityAttributesS2CPacket.class, "EntityAttributesS2CPacket", new EntityAttributesS2CUnpacker());
        registerPacket(EntitySpawnS2CPacket.class, "EntitySpawnS2CPacket", new EntitySpawnS2CUnpacker());

        // Chunk Pakete
        registerPacket(ChunkDataS2CPacket.class, "ChunkDataS2CPacket", new ChunkDataS2CUnpacker());

        // NBT/Custom Pakete
        registerPacket(NbtQueryResponseS2CPacket.class, "NbtQueryResponseS2CPacket", new NbtQueryResponseS2CUnpacker());
        registerPacket(CustomPayloadS2CPacket.class, "CustomPayloadS2CPacket", new CustomPayloadS2CUnpacker());
        registerPacket(CustomPayloadC2SPacket.class, "CustomPayloadC2SPacket", new CustomPayloadC2SUnpacker());

        // Weitere Pakete (nur Namen-Mapping)
        registerPacketName(GameJoinS2CPacket.class, "GameJoinS2CPacket");
        registerPacketName(PlayerPositionLookS2CPacket.class, "PlayerPositionLookS2CPacket");
        registerPacketName(OpenScreenS2CPacket.class, "OpenScreenS2CPacket");
        registerPacketName(CloseScreenS2CPacket.class, "CloseScreenS2CPacket");
        registerPacketName(EntityEquipmentUpdateS2CPacket.class, "EntityEquipmentUpdateS2CPacket");
        registerPacketName(EntityPositionS2CPacket.class, "EntityPositionS2CPacket");
        registerPacketName(EntityVelocityUpdateS2CPacket.class, "EntityVelocityUpdateS2CPacket");
        registerPacketName(HealthUpdateS2CPacket.class, "HealthUpdateS2CPacket");
        registerPacketName(ExperienceBarUpdateS2CPacket.class, "ExperienceBarUpdateS2CPacket");
        registerPacketName(ChatMessageS2CPacket.class, "ChatMessageS2CPacket");
        registerPacketName(GameMessageS2CPacket.class, "GameMessageS2CPacket");
        registerPacketName(ParticleS2CPacket.class, "ParticleS2CPacket");
        registerPacketName(PlaySoundS2CPacket.class, "PlaySoundS2CPacket");
        registerPacketName(WorldTimeUpdateS2CPacket.class, "WorldTimeUpdateS2CPacket");
    }
    *///?}

    private static <T extends Packet<?>> void registerPacket(Class<T> clazz, String name, PacketUnpacker<T> unpacker) {
        PACKET_NAMES.put(clazz, name);
        if (unpacker != null) {
            UNPACKERS.put(clazz, unpacker);
        }
    }

    private static void registerPacketName(Class<?> clazz, String name) {
        PACKET_NAMES.put(clazz, name);
    }

    public static void onWorldJoin(String worldName) {
        if (ModConfig.getInstance().logMode == ModConfig.LogMode.FILE) {
            currentSessionId = null;
            currentLogFile = null;
            System.out.println("[PacketLogger] New session started: " + worldName);
        }
    }

    public static void onWorldLeave() {
        if (currentLogFile != null && ModConfig.getInstance().logMode == ModConfig.LogMode.FILE) {
            try {
                synchronized (PacketLogger.class) {
                    try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(currentLogFile.toFile(), true)))) {
                        writer.println();
                        writer.println("=== Session ended: " + LocalDateTime.now().format(FILE_DATE_FORMAT) + " ===");
                    }
                }
            } catch (IOException e) { }
        }
        currentSessionId = null;
        currentLogFile = null;
    }

    public static void logIncoming(Packet<?> packet) {
        //? if >=26.1 {
        // BundleS2CPacket ist ein Container: der Server packt Spawn/Metadata/SetPassengers/Teleport
        // haeufig hier hinein. Er kommt als EIN Objekt durch channelRead0 - ohne Entpacken bleiben
        // alle inneren Pakete (u.a. EntityPassengersSetS2CPacket) unsichtbar. Also rekursiv entpacken.
        if (packet instanceof ClientboundBundlePacket bundle) {
            for (Packet<?> sub : bundle.subPackets()) {
                logIncoming(sub);
            }
            return;
        }
        //?}
        logPacket(packet, true);
    }

    public static void logOutgoing(Packet<?> packet) {
        logPacket(packet, false);
    }

    private static void logPacket(Packet<?> packet, boolean incoming) {
        ModConfig config = ModConfig.getInstance();

        if (config.logPackets && !wasLoggingEnabled && config.logMode == ModConfig.LogMode.FILE) {
            currentSessionId = null;
            currentLogFile = null;
        }
        wasLoggingEnabled = config.logPackets;

        if (!config.logPackets) return;

        String simpleName = getDeobfuscatedName(packet);

        if (incoming) {
            if (!shouldLogS2C(simpleName, config)) return;
        } else {
            if (!shouldLogC2S(simpleName, config)) return;
        }

        String timestamp = LocalTime.now().format(TIME_FORMAT);
        String direction = incoming ? "S2C" : "C2S";
        String packetData = unpackPacket(packet);

        if (config.logMode == ModConfig.LogMode.CHAT) {
            logToChat(timestamp, direction, simpleName, packetData, incoming);
        } else {
            logToFile(timestamp, direction, simpleName, packetData);
        }
    }

    private static String getDeobfuscatedName(Packet<?> packet) {
        Class<?> clazz = packet.getClass();
        String mappedName = PACKET_NAMES.get(clazz);
        if (mappedName != null) return mappedName;

        String simpleName = clazz.getSimpleName();
        if (simpleName.contains("Packet")) return simpleName;
        return simpleName;
    }

    private static boolean shouldLogS2C(String simpleName, ModConfig config) {
        if (config.selectedS2CPackets.isEmpty()) return false;
        for (String selected : config.selectedS2CPackets) {
            if (simpleName.equals(selected) || simpleName.endsWith(selected)) return true;
        }
        return false;
    }

    private static boolean shouldLogC2S(String simpleName, ModConfig config) {
        if (config.selectedC2SPackets.isEmpty()) return false;
        for (String selected : config.selectedC2SPackets) {
            if (simpleName.equals(selected) || simpleName.endsWith(selected)) return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static String unpackPacket(Packet<?> packet) {
        try {
            PacketUnpacker<Packet<?>> unpacker = (PacketUnpacker<Packet<?>>) UNPACKERS.get(packet.getClass());
            if (unpacker != null) return unpacker.unpack(packet);
            return ReflectionUnpacker.unpackWithReflection(packet);
        } catch (Exception e) {
            return "{error: \"" + e.getMessage() + "\"}";
        }
    }

    private static void logToChat(String timestamp, String direction, String packetName, String packetData, boolean incoming) {
        //? if >=26.1 {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        MutableComponent timeText = Component.literal("[" + timestamp + "] ").withStyle(ChatFormatting.GRAY);
        MutableComponent dirText = Component.literal("[" + direction + "] ").withStyle(incoming ? ChatFormatting.GREEN : ChatFormatting.RED);
        MutableComponent nameText = Component.literal(packetName + " ").withStyle(ChatFormatting.YELLOW);
        String shortData = packetData.length() > 300 ? packetData.substring(0, 300) + "..." : packetData;
        MutableComponent dataText = Component.literal(shortData).withStyle(ChatFormatting.WHITE);

        MutableComponent fullMessage = Component.empty().append(timeText).append(dirText).append(nameText).append(dataText);
        client.player.sendSystemMessage(fullMessage);
        //?} else {
        /*MinecraftClient client = MinecraftClient.getInstance();
        if (client.inGameHud == null || client.inGameHud.getChatHud() == null) return;

        MutableText timeText = Text.literal("[" + timestamp + "] ").formatted(Formatting.GRAY);
        MutableText dirText = Text.literal("[" + direction + "] ").formatted(incoming ? Formatting.GREEN : Formatting.RED);
        MutableText nameText = Text.literal(packetName + " ").formatted(Formatting.YELLOW);
        String shortData = packetData.length() > 300 ? packetData.substring(0, 300) + "..." : packetData;
        MutableText dataText = Text.literal(shortData).formatted(Formatting.WHITE);

        MutableText fullMessage = Text.empty().append(timeText).append(dirText).append(nameText).append(dataText);
        client.inGameHud.getChatHud().addMessage(fullMessage);
        *///?}
    }

    private static void logToFile(String timestamp, String direction, String packetName, String packetData) {
        try {
            Path logFile = getLogFile();
            String logLine = String.format("[%s] [%s] %s %s%n", timestamp, direction, packetName, packetData);
            synchronized (PacketLogger.class) {
                try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(logFile.toFile(), true)))) {
                    writer.print(logLine);
                }
            }
        } catch (IOException e) {
            System.err.println("[PacketLogger] Error writing to log file: " + e.getMessage());
        }
    }

    private static Path getLogFile() throws IOException {
        if (currentSessionId == null || currentLogFile == null) {
            currentSessionId = LocalDateTime.now().format(FILE_DATE_FORMAT);
            Path configDir = FabricLoader.getInstance().getConfigDir();
            Path logDir = configDir.resolve("packet-logger");
            Files.createDirectories(logDir);

            String worldName = getWorldName();
            String fileName = "packets_" + currentSessionId + "_" + worldName + ".log";
            currentLogFile = logDir.resolve(fileName);

            try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(currentLogFile.toFile())))) {
                writer.println("=== Deep Packet Logger ===");
                writer.println("Session: " + currentSessionId);
                writer.println("World: " + worldName);
                writer.println("Format: [TIME] [DIRECTION] PacketName {deep_data}");
                writer.println("==========================================");
                writer.println();
            }
            System.out.println("[PacketLogger] Created new log file: " + fileName);
        }
        return currentLogFile;
    }

    private static String getWorldName() {
        try {
            //? if >=26.1 {
            Minecraft client = Minecraft.getInstance();
            if (client != null) {
                if (client.getCurrentServer() != null) {
                    return sanitizeFileName(client.getCurrentServer().ip);
                }
                if (client.getSingleplayerServer() != null && client.getSingleplayerServer().getWorldData() != null) {
                    return sanitizeFileName(client.getSingleplayerServer().getWorldData().getLevelName());
                }
            }
            //?} else {
            /*MinecraftClient client = MinecraftClient.getInstance();
            if (client != null) {
                if (client.getCurrentServerEntry() != null) {
                    return sanitizeFileName(client.getCurrentServerEntry().address);
                }
                if (client.getServer() != null && client.getServer().getSaveProperties() != null) {
                    return sanitizeFileName(client.getServer().getSaveProperties().getLevelName());
                }
            }
            *///?}
        } catch (Exception e) { }
        return "unknown";
    }

    private static String sanitizeFileName(String name) {
        if (name == null) return "unknown";
        return name.replaceAll("[^a-zA-Z0-9._-]", "_").replaceAll("_+", "_").substring(0, Math.min(name.length(), 50));
    }
}
