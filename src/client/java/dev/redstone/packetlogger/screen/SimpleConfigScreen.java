package dev.redstone.packetlogger.screen;

import dev.redstone.packetlogger.config.ModConfig;
import dev.redstone.packetlogger.config.ModConfig.LogMode;
import dev.redstone.packetlogger.screen.widget.DualListSelectorWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class SimpleConfigScreen extends Screen {
    private final Screen parent;
    private final ModConfig config;
    
    // Widgets
    private Button logPacketsButton;
    private Button logModeButton;
    private DualListSelectorWidget s2cSelector;
    private DualListSelectorWidget c2sSelector;
    
    private boolean logPacketsEnabled;
    private LogMode currentLogMode;
    
    // Vollständige Liste S2C Pakete (Server to Client)
    private static final List<String> S2C_PACKETS = Arrays.asList(
        "AdvancementUpdateS2CPacket",
        "BlockBreakingProgressS2CPacket",
        "BlockEntityUpdateS2CPacket",
        "BlockEventS2CPacket",
        "BlockUpdateS2CPacket",
        "BossBarS2CPacket",
        "BundleS2CPacket",
        "ChangeUnlockedRecipesS2CPacket",
        "ChatMessageS2CPacket",
        "ChatSuggestionsS2CPacket",
        "ChunkBiomeDataS2CPacket",
        "ChunkDataS2CPacket",
        "ChunkDeltaUpdateS2CPacket",
        "ChunkLoadDistanceS2CPacket",
        "ChunkRenderDistanceCenterS2CPacket",
        "ChunkSentS2CPacket",
        "ClearTitleS2CPacket",
        "CloseScreenS2CPacket",
        "CommandSuggestionsS2CPacket",
        "CommandTreeS2CPacket",
        "CooldownUpdateS2CPacket",
        "CraftFailedResponseS2CPacket",
        "DamageTiltS2CPacket",
        "DeathMessageS2CPacket",
        "DebugSampleS2CPacket",
        "DifficultyS2CPacket",
        "EndCombatS2CPacket",
        "EnterCombatS2CPacket",
        "EnterReconfigurationS2CPacket",
        "EntitiesDestroyS2CPacket",
        "EntityAnimationS2CPacket",
        "EntityAttachS2CPacket",
        "EntityAttributesS2CPacket",
        "EntityDamageS2CPacket",
        "EntityEquipmentUpdateS2CPacket",
        "EntityPassengersSetS2CPacket",
        "EntityPositionS2CPacket",
        "EntityS2CPacket",
        "EntitySetHeadYawS2CPacket",
        "EntitySpawnS2CPacket",
        "EntityStatusEffectS2CPacket",
        "EntityStatusS2CPacket",
        "EntityTrackerUpdateS2CPacket",
        "EntityVelocityUpdateS2CPacket",
        "ExperienceBarUpdateS2CPacket",
        "ExperienceOrbSpawnS2CPacket",
        "ExplosionS2CPacket",
        "GameJoinS2CPacket",
        "GameMessageS2CPacket",
        "GameStateChangeS2CPacket",
        "HealthUpdateS2CPacket",
        "InventoryS2CPacket",
        "ItemPickupAnimationS2CPacket",
        "LightUpdateS2CPacket",
        "LookAtS2CPacket",
        "MapUpdateS2CPacket",
        "NbtQueryResponseS2CPacket",
        "OpenHorseScreenS2CPacket",
        "OpenScreenS2CPacket",
        "OpenWrittenBookS2CPacket",
        "OverlayMessageS2CPacket",
        "ParticleS2CPacket",
        "PlayerAbilitiesS2CPacket",
        "PlayerActionResponseS2CPacket",
        "PlayerListHeaderS2CPacket",
        "PlayerListS2CPacket",
        "PlayerPositionLookS2CPacket",
        "PlayerRemoveS2CPacket",
        "PlayerRespawnS2CPacket",
        "PlayerSpawnPositionS2CPacket",
        "PlaySoundFromEntityS2CPacket",
        "PlaySoundS2CPacket",
        "ProfilelessChatMessageS2CPacket",
        "ProjectilePowerS2CPacket",
        "RemoveEntityStatusEffectS2CPacket",
        "RemoveMessageS2CPacket",
        "ScoreboardDisplayS2CPacket",
        "ScoreboardObjectiveUpdateS2CPacket",
        "ScoreboardScoreResetS2CPacket",
        "ScoreboardScoreUpdateS2CPacket",
        "ScreenHandlerPropertyUpdateS2CPacket",
        "ScreenHandlerSlotUpdateS2CPacket",
        "SelectAdvancementTabS2CPacket",
        "ServerMetadataS2CPacket",
        "SetCameraEntityS2CPacket",
        "SetTradeOffersS2CPacket",
        "SignEditorOpenS2CPacket",
        "SimulationDistanceS2CPacket",
        "StartChunkSendS2CPacket",
        "StatisticsS2CPacket",
        "StopSoundS2CPacket",
        "SubtitleS2CPacket",
        "SynchronizeRecipesS2CPacket",
        "TeamS2CPacket",
        "TickStepS2CPacket",
        "TitleFadeS2CPacket",
        "TitleS2CPacket",
        "UnloadChunkS2CPacket",
        "UpdateSelectedSlotS2CPacket",
        "UpdateTickRateS2CPacket",
        "VehicleMoveS2CPacket",
        "WorldBorderCenterChangedS2CPacket",
        "WorldBorderInitializeS2CPacket",
        "WorldBorderInterpolateSizeS2CPacket",
        "WorldBorderSizeChangedS2CPacket",
        "WorldBorderWarningBlocksChangedS2CPacket",
        "WorldBorderWarningTimeChangedS2CPacket",
        "WorldEventS2CPacket",
        "WorldTimeUpdateS2CPacket"
    );
    
    // Vollständige Liste C2S Pakete (Client to Server)
    private static final List<String> C2S_PACKETS = Arrays.asList(
        "AcknowledgeChunksC2SPacket",
        "AcknowledgeReconfigurationC2SPacket",
        "AdvancementTabC2SPacket",
        "BoatPaddleStateC2SPacket",
        "BookUpdateC2SPacket",
        "ButtonClickC2SPacket",
        "ChatCommandSignedC2SPacket",
        "ChatMessageC2SPacket",
        "ClickSlotC2SPacket",
        "ClientCommandC2SPacket",
        "ClientStatusC2SPacket",
        "CloseHandledScreenC2SPacket",
        "CommandExecutionC2SPacket",
        "CraftRequestC2SPacket",
        "CreativeInventoryActionC2SPacket",
        "DebugSampleSubscriptionC2SPacket",
        "HandSwingC2SPacket",
        "JigsawGeneratingC2SPacket",
        "MessageAcknowledgmentC2SPacket",
        "PickFromInventoryC2SPacket",
        "PlayerActionC2SPacket",
        "PlayerInputC2SPacket",
        "PlayerInteractBlockC2SPacket",
        "PlayerInteractEntityC2SPacket",
        "PlayerInteractItemC2SPacket",
        "PlayerMoveC2SPacket",
        "PlayerSessionC2SPacket",
        "QueryBlockNbtC2SPacket",
        "QueryEntityNbtC2SPacket",
        "RecipeBookDataC2SPacket",
        "RecipeCategoryOptionsC2SPacket",
        "RenameItemC2SPacket",
        "RequestCommandCompletionsC2SPacket",
        "SelectMerchantTradeC2SPacket",
        "SlotChangedStateC2SPacket",
        "SpectatorTeleportC2SPacket",
        "TeleportConfirmC2SPacket",
        "UpdateBeaconC2SPacket",
        "UpdateCommandBlockC2SPacket",
        "UpdateCommandBlockMinecartC2SPacket",
        "UpdateDifficultyC2SPacket",
        "UpdateDifficultyLockC2SPacket",
        "UpdateJigsawC2SPacket",
        "UpdatePlayerAbilitiesC2SPacket",
        "UpdateSelectedSlotC2SPacket",
        "UpdateSignC2SPacket",
        "UpdateStructureBlockC2SPacket",
        "VehicleMoveC2SPacket"
    );

    public SimpleConfigScreen(Screen parent) {
        super(Component.literal("Packet Logger"));
        this.parent = parent;
        this.config = ModConfig.getInstance();
        this.logPacketsEnabled = config.logPackets;
        this.currentLogMode = config.logMode;
    }
    
    @Override
    protected void init() {
        super.init();
        
        int panelWidth = Math.min(500, this.width - 40);
        int panelX = (this.width - panelWidth) / 2;
        int panelY = 25;
        
        int buttonWidth = (panelWidth - 10) / 2;
        int y = panelY + 5;
        
        // Log Packets Toggle Button
        this.logPacketsButton = Button.builder(
            Component.literal("Logging: " + (logPacketsEnabled ? "§aON" : "§cOFF")),
            button -> {
                logPacketsEnabled = !logPacketsEnabled;
                button.setMessage(Component.literal("Logging: " + (logPacketsEnabled ? "§aON" : "§cOFF")));
            })
            .bounds(panelX, y, buttonWidth, 20)
            .build();
        this.addRenderableWidget(logPacketsButton);

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
            Button.builder(Component.literal("Cancel"), button -> this.onClose())
                .bounds(this.width / 2 + 5, bottomY, bottomButtonWidth, 20)
                .build()
        );
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        context.fillGradient(0, 0, this.width, this.height, 0xA0101010, 0xB0101010);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        int panelWidth = Math.min(500, this.width - 40);
        int panelX = (this.width - panelWidth) / 2;
        int panelY = 20;
        int panelHeight = this.height - 55;

        context.fill(panelX - 2, panelY - 2, panelX + panelWidth + 2, panelY + panelHeight + 2, 0xFF2A2A2A);
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xE0181818);

        super.extractRenderState(context, mouseX, mouseY, delta);

        context.centeredText(this.font, this.title, this.width / 2, 8, 0xFFFFFFFF);
    }
    
    private void saveAndClose() {
        config.logPackets = logPacketsEnabled;
        config.logMode = currentLogMode;
        config.selectedS2CPackets = new ArrayList<>(s2cSelector.getSelectedPackets());
        config.selectedC2SPackets = new ArrayList<>(c2sSelector.getSelectedPackets());
        config.save();
        this.onClose();
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreenAndShow(this.parent);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
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
