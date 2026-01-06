package dev.redstone.packetlogger.screen;

import dev.redstone.packetlogger.config.ModConfig;
import dev.redstone.packetlogger.config.ModConfig.LogMode;
import dev.redstone.packetlogger.screen.widget.DualListSelectorWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class SimpleConfigScreen extends Screen {
    private final Screen parent;
    private final ModConfig config;
    
    // Widgets
    private ButtonWidget logPackagesButton;
    private ButtonWidget logModeButton;
    private DualListSelectorWidget s2cSelector;
    private DualListSelectorWidget c2sSelector;
    
    private boolean logPackagesEnabled;
    private LogMode currentLogMode;
    
    // Vollständige Liste S2C Pakete (Server to Client)
    private static final List<String> S2C_PACKAGES = Arrays.asList(
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
    private static final List<String> C2S_PACKAGES = Arrays.asList(
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
        super(Text.literal("Package Logger"));
        this.parent = parent;
        this.config = ModConfig.getInstance();
        this.logPackagesEnabled = config.logPackages;
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
        
        // Log Packages Toggle Button
        this.logPackagesButton = ButtonWidget.builder(
            Text.literal("Logging: " + (logPackagesEnabled ? "§aON" : "§cOFF")),
            button -> {
                logPackagesEnabled = !logPackagesEnabled;
                button.setMessage(Text.literal("Logging: " + (logPackagesEnabled ? "§aON" : "§cOFF")));
            })
            .dimensions(panelX, y, buttonWidth, 20)
            .build();
        this.addDrawableChild(logPackagesButton);
        
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
            S2C_PACKAGES,
            new HashSet<>(config.selectedS2CPackages),
            selection -> {}
        );
        this.addDrawableChild(s2cSelector);
        
        y += selectorHeight + 10;
        
        // C2S Selector
        this.c2sSelector = new DualListSelectorWidget(
            panelX, y, panelWidth, selectorHeight,
            "C2S Packets (Client → Server)",
            C2S_PACKAGES,
            new HashSet<>(config.selectedC2SPackages),
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
            ButtonWidget.builder(Text.literal("Cancel"), button -> this.close())
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
    
    private void saveAndClose() {
        config.logPackages = logPackagesEnabled;
        config.logMode = currentLogMode;
        config.selectedS2CPackages = new ArrayList<>(s2cSelector.getSelectedPackages());
        config.selectedC2SPackages = new ArrayList<>(c2sSelector.getSelectedPackages());
        config.save();
        this.close();
    }
    
    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
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
