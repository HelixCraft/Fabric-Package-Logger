# Package Logger

Ein Minecraft Fabric Mod zum Loggen von Netzwerk-Paketen zwischen Client und Server.

## Funktionsweise

Der Package Logger intercepted **alle** Netzwerk-Pakete über `ClientConnection`:

### Eingehende Pakete (S2C - Server to Client)

- Werden über `ClientConnection.handlePacket()` abgefangen
- Pakete deren Klassenname `.s2c.` enthält
- Beispiele: Inventar-Updates, Entity-Positionen, Chat-Nachrichten

### Ausgehende Pakete (C2S - Client to Server)

- Werden über `ClientConnection.send()` abgefangen
- Pakete deren Klassenname `.c2s.` enthält
- Beispiele: Spieler-Bewegungen, Block-Interaktionen, Chat-Eingaben

## Log-Modi

### Modus 1: Chat

Pakete werden direkt im Minecraft-Chat angezeigt mit Farbcodierung:

- **Grau**: Zeitstempel `[HH:mm:ss]`
- **Grün**: Eingehende Pakete `[Incoming]`
- **Rot**: Ausgehende Pakete `[Outgoing]`
- **Gelb**: Paketname
- **Weiß**: Paketdaten

### Modus 2: File

Pakete werden in eine Datei geschrieben:

- **Speicherort**: `MINECRAFT/config/packet-logger/packages-YYYY-MM-DD.txt`
- Neue Datei pro Tag
- Format: `[HH:mm:ss] [Incoming/Outgoing] PacketName {data}`

## Paket-Auswahl

Im Config-Screen (Taste `R`) können spezifische Pakete ausgewählt werden:

- **Linke Liste**: Verfügbare Pakete
- **Rechte Liste**: Ausgewählte Pakete (werden geloggt)
- **Suchfeld**: Filtert beide Listen
- Klick auf `+` fügt hinzu, Klick auf `-` entfernt

## Paketdaten

Die geloggten Daten enthalten **alle Felder** des Pakets via Reflection:

- Alle nicht-statischen Felder werden ausgelesen
- NBT-Daten bei Inventar-Paketen
- Entity-Positionen, IDs, Velocities
- Item-Stacks mit allen Eigenschaften

## Beispiel-Ausgabe

### Chat:

```
[18:43:59] [Incoming] InventoryS2CPacket {syncId=1, revision=0, contents=[...]}
[18:44:01] [Outgoing] ClickSlotC2SPacket {syncId=1, slot=5, button=0}
```

### File (config/packet-logger/packages-2026-01-04.txt):

```
=== Package Logger - 2026-01-04 ===
Format: [TIME] [DIRECTION] PacketName {data}
=========================================

[18:43:59] [Incoming] InventoryS2CPacket {syncId=1, revision=0, contents=[...]}
[18:44:01] [Outgoing] ClickSlotC2SPacket {syncId=1, slot=5, button=0}
```

## Konfiguration

Gespeichert unter: `MINECRAFT/config/packet-logger-config.json`

```json
{
  "logPackages": true,
  "logMode": "CHAT",
  "selectedS2CPackages": [
    "InventoryS2CPacket",
    "ScreenHandlerSlotUpdateS2CPacket"
  ],
  "selectedC2SPackages": ["ClickSlotC2SPacket"]
}
```

## Keybinding

- **R**: Öffnet den Config-Screen

## Technische Details

### Mixin

- `ClientConnectionMixin`: Intercepted `send()` für C2S und `handlePacket()` für S2C

### Klassen

- `PacketLogger`: Logging-Service mit Chat/File Output
- `ModConfig`: Konfigurationsverwaltung
- `SimpleConfigScreen`: GUI für Einstellungen
- `DualListSelectorWidget`: Widget für Paket-Auswahl
