# Packet Logger

![Environment](https://img.shields.io/badge/Environment-Client-purple)
[![Java 21](https://img.shields.io/badge/Language-Java%2021-orange)](https://www.oracle.com/java/technologies/downloads/#java21)
[![Modrinth](https://img.shields.io/badge/Modrinth-Packet--Logger-00AF5C?logo=modrinth)](https://modrinth.com/mod/fabric-packet-logger)

<p align="left">
  <a href="https://modrinth.com/mod/fabric-packet-logger">
    <img src="https://github.com/user-attachments/assets/6bc92930-84f9-4eb1-ae1d-8f79775b87c6" width="200" alt="Download on Modrinth">
  </a>
  <a href="https://modrinth.com/mod/fabric-api">
    <img src="https://cdn.modrinth.com/data/cached_images/cf4bfd9c2896b8f63cd7db479ceccc0578610b21.webp" width="200" alt="Fabric API on Modrinth">
  </a>
</p>
A deep packet logging mod for Minecraft Fabric that captures all network traffic (´S2C´ and ´C2S´) with full NBT/Component data.

## Use Cases

- **Mod Development**: Debug network communication between client and server
- **Container Inspection**: View exact contents of inventories with full NBT/Component data and copying them
- **Reverse Engineering**: Analyze server-side mechanics and packet structures
- **Anti-Cheat Analysis**: Understand what data the server sends and receives
- **Learning**: Understand how Minecraft's network protocol works

<div align="center">
  <img src="https://github.com/user-attachments/assets/e41a36c0-6f4e-4cfb-af4a-8cc0864bb76f" width="700" />
</div>

## Usage

### Keybind

Press **F6** to open the config screen.

You can also assign a keybind in the config screen to toggle packet logging on/off. It is unbound by default.

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
MINECRAFT_FOLDER/packet-logger/packets_2026-01-04_15-30-45_servername.log
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
  "loggingKeybind": -1,
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

Output: `build/libs/packet-logger-1.1.2.jar`

### Project Structure

```
src/main/java/dev/redstone/packetlogger/
├── PacketLoggerClient.java       # Client entrypoint
├── config/
│   └── ModConfig.java            # Configuration
├── logger/
│   ├── PacketLogger.java         # Main logger
│   └── unpacker/                 # Specialized packet unpackers
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

All packets are loaded dynamically from the game's `PacketType` registries and are selectable in the config screen's dual-list selector. Both S2C (Server → Client) and C2S (Client → Server) packets are covered across all protocol phases.

See [PACKETS-1.21.4.md](PACKETS-1.21.4.md) for an example listing of all packets for Minecraft 1.21.4.

## License

This project is licensed under the [GNU General Public License v3.0](LICENSE).
