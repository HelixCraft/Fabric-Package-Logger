# Package Logger

A deep packet logging mod for Minecraft Fabric that captures all network traffic (S2C and C2S) with full NBT/Component data.

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
.minecraft/config/package-logger/packets_2026-01-04_15-30-45_servername.log
```

A new log file is created when:

- Joining a world/server
- Re-enabling logging after it was disabled

### Example Output

```
[12:34:56.789] [S2C] InventoryS2CPacket {syncId:2,revision:1,id:"minecraft:generic_9x3",components:{"minecraft:container":[{item:{id:"minecraft:diamond_sword",count:1,components:{"minecraft:enchantments":{levels:{"minecraft:sharpness":5}}}},slot:0}]}}
```

### Configuration

Config saved at: `.minecraft/config/package-logger-config.json`

```json
{
  "logPackages": true,
  "logMode": "FILE",
  "deepLogging": true,
  "selectedS2CPackages": ["InventoryS2CPacket"],
  "selectedC2SPackages": ["ClickSlotC2SPacket"]
}
```

## Installation

### Requirements

- Minecraft 1.21.4
- Fabric Loader 0.16.0+
- Fabric API

### Download

Download the latest release from the [Releases](https://github.com/HelixCraft/Fabric-Package-Logger/releases) page and place it in your `mods` folder.

## For Developers

### Building

```bash
./gradlew build
```

Output: `build/libs/package-logger-1.0.0.jar`

### Project Structure

```
src/client/java/dev/redstone/packagelogger/
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
