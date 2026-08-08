# Packets (1.21.4)

Example overview of all packets for Minecraft version 1.21.4. The mod loads the actually available packets at runtime dynamically from the `PacketType` registries.

### **S2C (Server → Client)**

**Common**
- `CommonPingS2CPacket`
- `CookieRequestS2CPacket`
- `CustomPayloadS2CPacket`
- `CustomReportDetailsS2CPacket`
- `DisconnectS2CPacket`
- `KeepAliveS2CPacket`
- `ResourcePackRemoveS2CPacket`
- `ResourcePackSendS2CPacket`
- `ServerLinksS2CPacket`
- `ServerTransferS2CPacket`
- `StoreCookieS2CPacket`
- `SynchronizeTagsS2CPacket`

**Config**
- `DynamicRegistriesS2CPacket`
- `FeaturesS2CPacket`
- `ReadyS2CPacket`
- `ResetChatS2CPacket`
- `SelectKnownPacksS2CPacket`

**Custom**
- `DebugBeeCustomPayload`
  - `Bee`
- `DebugBrainCustomPayload`
  - `Brain`
- `DebugBreezeCustomPayload`
  - `BreezeInfo`
- `DebugGameEventCustomPayload`
- `DebugGameEventListenersCustomPayload`
- `DebugGameTestAddMarkerCustomPayload`
- `DebugGameTestClearCustomPayload`
- `DebugGoalSelectorCustomPayload`
  - `Goal`
- `DebugHiveCustomPayload`
  - `HiveInfo`
- `DebugNeighborsUpdateCustomPayload`
- `DebugPathCustomPayload`
- `DebugPoiAddedCustomPayload`
- `DebugPoiRemovedCustomPayload`
- `DebugPoiTicketCountCustomPayload`
- `DebugRaidsCustomPayload`
- `DebugRedstoneUpdateOrderCustomPayload`
  - `Wire`
- `DebugStructuresCustomPayload`
  - `Piece`
- `DebugVillageSectionsCustomPayload`
- `DebugWorldgenAttemptCustomPayload`

**Login**
- `LoginCompressionS2CPacket`
- `LoginDisconnectS2CPacket`
- `LoginHelloS2CPacket`
- `LoginQueryRequestS2CPacket`
- `LoginQueryRequestPayload`
- `LoginSuccessS2CPacket`
- `UnknownLoginQueryRequestPayload`

**Play**
- `AdvancementUpdateS2CPacket`
- `BlockBreakingProgressS2CPacket`
- `BlockEntityUpdateS2CPacket`
- `BlockEventS2CPacket`
- `BlockUpdateS2CPacket`
- `BossBarS2CPacket`
  - `Action`
  - `AddAction`
  - `Consumer`
  - `Type`
  - `UpdateNameAction`
  - `UpdateProgressAction`
  - `UpdatePropertiesAction`
  - `UpdateStyleAction`
- `BundleDelimiterS2CPacket`
- `BundleS2CPacket`
- `ChatMessageS2CPacket`
- `ChatSuggestionsS2CPacket`
  - `Action`
- `ChunkBiomeDataS2CPacket`
  - `Serialized`
- `ChunkDataS2CPacket`
  - `BlockEntityData`
  - `BlockEntityVisitor`
- `ChunkDeltaUpdateS2CPacket`
- `ChunkLoadDistanceS2CPacket`
- `ChunkRenderDistanceCenterS2CPacket`
- `ChunkSentS2CPacket`
- `ClearTitleS2CPacket`
- `CloseScreenS2CPacket`
- `CommandSuggestionsS2CPacket`
  - `Suggestion`
- `CommandTreeS2CPacket`
  - `ArgumentNode`
  - `CommandNodeData`
  - `CommandTree`
  - `LiteralNode`
  - `SuggestableNode`
- `CommonPlayerSpawnInfo`
- `CooldownUpdateS2CPacket`
- `CraftFailedResponseS2CPacket`
- `DamageTiltS2CPacket`
- `DeathMessageS2CPacket`
- `DebugSampleS2CPacket`
- `DifficultyS2CPacket`
- `EndCombatS2CPacket`
- `EnterCombatS2CPacket`
- `EnterReconfigurationS2CPacket`
- `EntitiesDestroyS2CPacket`
- `EntityAnimationS2CPacket`
- `EntityAttachS2CPacket`
- `EntityAttributesS2CPacket`
  - `Entry`
- `EntityDamageS2CPacket`
- `EntityEquipmentUpdateS2CPacket`
- `EntityPassengersSetS2CPacket`
- `EntityPositionS2CPacket`
- `EntityPositionSyncS2CPacket`
- `EntityS2CPacket`
  - `MoveRelative`
  - `Rotate`
  - `RotateAndMoveRelative`
- `EntitySetHeadYawS2CPacket`
- `EntitySpawnS2CPacket`
- `EntityStatusEffectS2CPacket`
- `EntityStatusS2CPacket`
- `EntityTrackerUpdateS2CPacket`
- `EntityVelocityUpdateS2CPacket`
- `ExperienceBarUpdateS2CPacket`
- `ExperienceOrbSpawnS2CPacket`
- `ExplosionS2CPacket`
- `GameJoinS2CPacket`
- `GameMessageS2CPacket`
- `GameStateChangeS2CPacket`
  - `Reason`
- `HealthUpdateS2CPacket`
- `InventoryS2CPacket`
- `ItemPickupAnimationS2CPacket`
- `LightUpdateS2CPacket`
- `LookAtS2CPacket`
- `MapUpdateS2CPacket`
- `MoveMinecartAlongTrackS2CPacket`
- `NbtQueryResponseS2CPacket`
- `OpenHorseScreenS2CPacket`
- `OpenScreenS2CPacket`
- `OpenWrittenBookS2CPacket`
- `OverlayMessageS2CPacket`
- `ParticleS2CPacket`
- `PlayerAbilitiesS2CPacket`
- `PlayerActionResponseS2CPacket`
- `PlayerListHeaderS2CPacket`
- `PlayerListS2CPacket`
  - `Action`
  - `Entry`
  - `Serialized`
- `PlayerPositionLookS2CPacket`
- `PlayerRemoveS2CPacket`
- `PlayerRespawnS2CPacket`
- `PlayerRotationS2CPacket`
- `PlayerSpawnPositionS2CPacket`
- `PlaySoundFromEntityS2CPacket`
- `PlaySoundS2CPacket`
- `ProfilelessChatMessageS2CPacket`
- `ProjectilePowerS2CPacket`
- `RecipeBookAddS2CPacket`
  - `Entry`
- `RecipeBookRemoveS2CPacket`
- `RecipeBookSettingsS2CPacket`
- `RemoveEntityStatusEffectS2CPacket`
- `RemoveMessageS2CPacket`
- `ScoreboardDisplayS2CPacket`
- `ScoreboardObjectiveUpdateS2CPacket`
- `ScoreboardScoreResetS2CPacket`
- `ScoreboardScoreUpdateS2CPacket`
- `ScreenHandlerPropertyUpdateS2CPacket`
- `ScreenHandlerSlotUpdateS2CPacket`
- `SelectAdvancementTabS2CPacket`
- `ServerMetadataS2CPacket`
- `SetCameraEntityS2CPacket`
- `SetCursorItemS2CPacket`
- `SetPlayerInventoryS2CPacket`
- `SetTradeOffersS2CPacket`
- `SignEditorOpenS2CPacket`
- `SimulationDistanceS2CPacket`
- `StartChunkSendS2CPacket`
- `StatisticsS2CPacket`
- `StopSoundS2CPacket`
- `SubtitleS2CPacket`
- `SynchronizeRecipesS2CPacket`
- `TeamS2CPacket`
  - `Operation`
  - `SerializableTeam`
- `TickStepS2CPacket`
- `TitleFadeS2CPacket`
- `TitleS2CPacket`
- `UnloadChunkS2CPacket`
- `UpdateSelectedSlotS2CPacket`
- `UpdateTickRateS2CPacket`
- `VehicleMoveS2CPacket`
- `WorldBorderCenterChangedS2CPacket`
- `WorldBorderInitializeS2CPacket`
- `WorldBorderInterpolateSizeS2CPacket`
- `WorldBorderSizeChangedS2CPacket`
- `WorldBorderWarningBlocksChangedS2CPacket`
- `WorldBorderWarningTimeChangedS2CPacket`
- `WorldEventS2CPacket`
- `WorldTimeUpdateS2CPacket`

**Query**
- `PingResultS2CPacket`
- `QueryResponseS2CPacket`

### **C2S (Client → Server)**

**Common**

- `ClientOptionsC2SPacket`
- `CommonPongC2SPacket`
- `CookieResponseC2SPacket`
- `CustomPayloadC2SPacket`
- `KeepAliveC2SPacket`
- `ResourcePackStatusC2SPacket`
  - `Status`
- `SyncedClientOptions`

**Config**
- `ReadyC2SPacket`
- `SelectKnownPacksC2SPacket`

**Handshake**
- `HandshakeC2SPacket`
  - `ConnectionIntent`

**Login**
- `EnterConfigurationC2SPacket`
- `LoginHelloC2SPacket`
- `LoginKeyC2SPacket`
- `LoginQueryResponseC2SPacket`
  - `LoginQueryResponsePayload`
  - `UnknownLoginQueryResponsePayload`

**Play**
- `AcknowledgeChunksC2SPacket`
- `AcknowledgeReconfigurationC2SPacket`
- `AdvancementTabC2SPacket`
  - `Action`
- `BoatPaddleStateC2SPacket`
- `BookUpdateC2SPacket`
- `BundleItemSelectedC2SPacket`
- `ButtonClickC2SPacket`
- `ChatCommandSignedC2SPacket`
- `ChatMessageC2SPacket`
- `ClickSlotC2SPacket`
- `ClientCommandC2SPacket`
  - `Mode`
- `ClientStatusC2SPacket`
  - `Mode`
- `ClientTickEndC2SPacket`
- `CloseHandledScreenC2SPacket`
- `CommandExecutionC2SPacket`
- `CraftRequestC2SPacket`
- `CreativeInventoryActionC2SPacket`
- `DebugSampleSubscriptionC2SPacket`
- `HandSwingC2SPacket`
- `JigsawGeneratingC2SPacket`
- `MessageAcknowledgmentC2SPacket`
- `PickItemFromBlockC2SPacket`
- `PickItemFromEntityC2SPacket`
- `PlayerActionC2SPacket`
  - `Action`
- `PlayerInputC2SPacket`
- `PlayerInteractBlockC2SPacket`
- `PlayerInteractEntityC2SPacket`
  - `Handler`
  - `InteractAtHandler`
  - `InteractHandler`
  - `InteractType`
  - `InteractTypeHandler`
- `PlayerInteractItemC2SPacket`
- `PlayerLoadedC2SPacket`
- `PlayerMoveC2SPacket`
  - `Full`
  - `LookAndOnGround`
  - `OnGroundOnly`
  - `PositionAndOnGround`
- `PlayerSessionC2SPacket`
- `QueryBlockNbtC2SPacket`
- `QueryEntityNbtC2SPacket`
- `RecipeBookDataC2SPacket`
- `RecipeCategoryOptionsC2SPacket`
- `RenameItemC2SPacket`
- `RequestCommandCompletionsC2SPacket`
- `SelectMerchantTradeC2SPacket`
- `SlotChangedStateC2SPacket`
- `SpectatorTeleportC2SPacket`
- `TeleportConfirmC2SPacket`
- `UpdateBeaconC2SPacket`
- `UpdateCommandBlockC2SPacket`
- `UpdateCommandBlockMinecartC2SPacket`
- `UpdateDifficultyC2SPacket`
- `UpdateDifficultyLockC2SPacket`
- `UpdateJigsawC2SPacket`
- `UpdatePlayerAbilitiesC2SPacket`
- `UpdateSelectedSlotC2SPacket`
- `UpdateSignC2SPacket`
- `UpdateStructureBlockC2SPacket`
- `VehicleMoveC2SPacket`

**Query**
- `QueryPingC2SPacket`
- `QueryRequestC2SPacket`
