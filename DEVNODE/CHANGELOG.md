# Changelog

## 1.1.2

### Packet Catalog Rework
- Complete rework of the packet catalog across S2C and C2S.
- Many previously missing packets are now logged.
- Packets that were not fully unpacked now include their complete data (NBT/Component) and are correctly deserialized.

### Bugfixes
- Fixed the search text input in the config screen: it no longer gets blocked by double-clicking, so the field can be focused/refocused reliably.

### Config Screen
- Added autosave when closing the config screen; a Cancel button reverts all changes to the state from when the screen was opened.

### Keybind
- Added a configurable keybind to toggle packet logging on/off.

### Log Storage
- Log files are now stored in a dedicated `packet-logger` folder inside the game directory instead of the config directory.

### Log File Header
- Each log file header now includes additional metadata about the client (Minecraft and Fabric Loader version) and the active session/world.
