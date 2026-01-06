# Packet Logger

![Environment](https://img.shields.io/badge/Environment-Client-purple)
[![Java 21](https://img.shields.io/badge/Language-Java%2021-orange)](https://www.oracle.com/java/technologies/downloads/#java21)
[![Modrinth](https://img.shields.io/badge/Modrinth-Packet--Logger-00AF5C?logo=modrinth)](https://modrinth.com/mod/packet-logger)


A deep packet logging mod for Minecraft Fabric that captures all network traffic (´S2C´ and ´C2S´) with full NBT/Component data.

## Use Cases

- **Mod Development**: Debug network communication between client and server
- **Container Inspection**: View exact contents of inventories with full NBT/Component data and copying them
- **Reverse Engineering**: Analyze server-side mechanics and packet structures
- **Anti-Cheat Analysis**: Understand what data the server sends and receives
- **Learning**: Understand how Minecraft's network protocol works

## Usage

### Keybind

Press **F6** to open the config screen.

### Packet Selection

Use the dual-list selector to choose which packets to log:

- **S2C (Server → Client)**: Incoming packets like inventory updates, entity spawns, etc.
- **C2S (Client → Server)**: Outgoing packets like clicks, movements, etc.

### Log Modes

- **Chat**: Display packets in the game chat (truncated for readability)
- **File**: Save packets to log files with full data

### Log Files

Logs are saved to:

```
MINECRAFT_FOLDER/config/packet-logger/packets_2026-01-04_15-30-45_servername.log
```

A new log file is created when:

- Joining a world/server
- Re-enabling logging after it was disabled

### Example Output

```
[12:34:56.789] [S2C] InventoryS2CPacket {syncId:2,revision:1,id:"minecraft:generic_9x3",components:{"minecraft:container":[{item:{id:"minecraft:diamond_sword",count:1,components:{"minecraft:enchantments":{levels:{"minecraft:sharpness":5}}}},slot:0}]}}
```

### Configuration

Config saved at: `.minecraft/config/packet-logger-config.json`

```json
{
  "logPackets": true,
  "logMode": "FILE",
  "deepLogging": true,
  "selectedS2CPackets": ["InventoryS2CPacket"],
  "selectedC2SPackets": ["ClickSlotC2SPacket"]
}
```

## Installation

### Requirements

- Minecraft 1.21 - 1.21.11
- Fabric Loader 0.16.0+
- Fabric API 

<br> <a href="https://modrinth.com/mod/fabric-api"><img src="https://cdn.modrinth.com/data/cached_images/cf4bfd9c2896b8f63cd7db479ceccc0578610b21.webp" width="200" alt="Fabric API on Modrinth"></a>

### Download

You can download the mod from Modrinth. Simply place the downloaded `.jar` file into your `.minecraft/mods` folder.

[**Download on Modrinth**](https://modrinth.com/mod/fabric-packet-logger)

<a href="https://modrinth.com/mod/fabric-packet-logger">
  <img src="https://github.com/user-attachments/assets/6bc92930-84f9-4eb1-ae1d-8f79775b87c6" width="200" alt="Download on Modrinth">
</a>

## For Developers

### Building

```bash
./gradlew build
```

Output: `build/libs/packet-logger-1.0.0.jar`

### Project Structure

```
src/client/java/dev/redstone/packetlogger/
├── PackageLoggerClient.java       # Client entrypoint
├── config/
│   └── ModConfig.java             # Configuration
├── logger/
│   ├── PacketLogger.java          # Main logger
│   └── unpacker/                  # Specialized packet unpackers
│       ├── ItemStackFormatter.java
│       ├── InventoryS2CUnpacker.java
│       └── ...
├── mixin/client/
│   ├── ClientConnectionMixin.java # Packet interception
│   └── ...
└── screen/
    └── SimpleConfigScreen.java    # Config UI
```

## Available Packets

### S2C Packets (Server → Client)

<details>
<summary>Click to expand (108 packets)</summary>

- AdvancementUpdateS2CPacket
- BlockBreakingProgressS2CPacket
- BlockEntityUpdateS2CPacket
- BlockEventS2CPacket
- BlockUpdateS2CPacket
- BossBarS2CPacket
- BundleS2CPacket
- ChangeUnlockedRecipesS2CPacket
- ChatMessageS2CPacket
- ChatSuggestionsS2CPacket
- ChunkBiomeDataS2CPacket
- ChunkDataS2CPacket
- ChunkDeltaUpdateS2CPacket
- ChunkLoadDistanceS2CPacket
- ChunkRenderDistanceCenterS2CPacket
- ChunkSentS2CPacket
- ClearTitleS2CPacket
- CloseScreenS2CPacket
- CommandSuggestionsS2CPacket
- CommandTreeS2CPacket
- CooldownUpdateS2CPacket
- CraftFailedResponseS2CPacket
- DamageTiltS2CPacket
- DeathMessageS2CPacket
- DebugSampleS2CPacket
- DifficultyS2CPacket
- EndCombatS2CPacket
- EnterCombatS2CPacket
- EnterReconfigurationS2CPacket
- EntitiesDestroyS2CPacket
- EntityAnimationS2CPacket
- EntityAttachS2CPacket
- EntityAttributesS2CPacket
- EntityDamageS2CPacket
- EntityEquipmentUpdateS2CPacket
- EntityPassengersSetS2CPacket
- EntityPositionS2CPacket
- EntityS2CPacket
- EntitySetHeadYawS2CPacket
- EntitySpawnS2CPacket
- EntityStatusEffectS2CPacket
- EntityStatusS2CPacket
- EntityTrackerUpdateS2CPacket
- EntityVelocityUpdateS2CPacket
- ExperienceBarUpdateS2CPacket
- ExperienceOrbSpawnS2CPacket
- ExplosionS2CPacket
- GameJoinS2CPacket
- GameMessageS2CPacket
- GameStateChangeS2CPacket
- HealthUpdateS2CPacket
- InventoryS2CPacket
- ItemPickupAnimationS2CPacket
- LightUpdateS2CPacket
- LookAtS2CPacket
- MapUpdateS2CPacket
- NbtQueryResponseS2CPacket
- OpenHorseScreenS2CPacket
- OpenScreenS2CPacket
- OpenWrittenBookS2CPacket
- OverlayMessageS2CPacket
- ParticleS2CPacket
- PlayerAbilitiesS2CPacket
- PlayerActionResponseS2CPacket
- PlayerListHeaderS2CPacket
- PlayerListS2CPacket
- PlayerPositionLookS2CPacket
- PlayerRemoveS2CPacket
- PlayerRespawnS2CPacket
- PlayerSpawnPositionS2CPacket
- PlaySoundFromEntityS2CPacket
- PlaySoundS2CPacket
- ProfilelessChatMessageS2CPacket
- ProjectilePowerS2CPacket
- RemoveEntityStatusEffectS2CPacket
- RemoveMessageS2CPacket
- ScoreboardDisplayS2CPacket
- ScoreboardObjectiveUpdateS2CPacket
- ScoreboardScoreResetS2CPacket
- ScoreboardScoreUpdateS2CPacket
- ScreenHandlerPropertyUpdateS2CPacket
- ScreenHandlerSlotUpdateS2CPacket
- SelectAdvancementTabS2CPacket
- ServerMetadataS2CPacket
- SetCameraEntityS2CPacket
- SetTradeOffersS2CPacket
- SignEditorOpenS2CPacket
- SimulationDistanceS2CPacket
- StartChunkSendS2CPacket
- StatisticsS2CPacket
- StopSoundS2CPacket
- SubtitleS2CPacket
- SynchronizeRecipesS2CPacket
- TeamS2CPacket
- TickStepS2CPacket
- TitleFadeS2CPacket
- TitleS2CPacket
- UnloadChunkS2CPacket
- UpdateSelectedSlotS2CPacket
- UpdateTickRateS2CPacket
- VehicleMoveS2CPacket
- WorldBorderCenterChangedS2CPacket
- WorldBorderInitializeS2CPacket
- WorldBorderInterpolateSizeS2CPacket
- WorldBorderSizeChangedS2CPacket
- WorldBorderWarningBlocksChangedS2CPacket
- WorldBorderWarningTimeChangedS2CPacket
- WorldEventS2CPacket
- WorldTimeUpdateS2CPacket

</details>

### C2S Packets (Client → Server)

<details>
<summary>Click to expand (47 packets)</summary>

- AcknowledgeChunksC2SPacket
- AcknowledgeReconfigurationC2SPacket
- AdvancementTabC2SPacket
- BoatPaddleStateC2SPacket
- BookUpdateC2SPacket
- ButtonClickC2SPacket
- ChatCommandSignedC2SPacket
- ChatMessageC2SPacket
- ClickSlotC2SPacket
- ClientCommandC2SPacket
- ClientStatusC2SPacket
- CloseHandledScreenC2SPacket
- CommandExecutionC2SPacket
- CraftRequestC2SPacket
- CreativeInventoryActionC2SPacket
- DebugSampleSubscriptionC2SPacket
- HandSwingC2SPacket
- JigsawGeneratingC2SPacket
- MessageAcknowledgmentC2SPacket
- PickFromInventoryC2SPacket
- PlayerActionC2SPacket
- PlayerInputC2SPacket
- PlayerInteractBlockC2SPacket
- PlayerInteractEntityC2SPacket
- PlayerInteractItemC2SPacket
- PlayerMoveC2SPacket
- PlayerSessionC2SPacket
- QueryBlockNbtC2SPacket
- QueryEntityNbtC2SPacket
- RecipeBookDataC2SPacket
- RecipeCategoryOptionsC2SPacket
- RenameItemC2SPacket
- RequestCommandCompletionsC2SPacket
- SelectMerchantTradeC2SPacket
- SlotChangedStateC2SPacket
- SpectatorTeleportC2SPacket
- TeleportConfirmC2SPacket
- UpdateBeaconC2SPacket
- UpdateCommandBlockC2SPacket
- UpdateCommandBlockMinecartC2SPacket
- UpdateDifficultyC2SPacket
- UpdateDifficultyLockC2SPacket
- UpdateJigsawC2SPacket
- UpdatePlayerAbilitiesC2SPacket
- UpdateSelectedSlotC2SPacket
- UpdateSignC2SPacket
- UpdateStructureBlockC2SPacket
- VehicleMoveC2SPacket

</details>

## License

MIT

## Credits

- Fabric API Team
- Minecraft Modding Community
