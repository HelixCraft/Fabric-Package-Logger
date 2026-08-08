# HANDOFF: Alle Pakete auswählbar machen (Complete 1.21 → dann porten)

> Dieses Dokument ist der vollständige Zustand der Arbeit. Nächstes Vorgehen: 1.21 fertig
> implementieren, Build grün, dann als anderes Agenten-Portierungsbasis nutzen.

## 1. Ziel (Nutzerwunsch, unverändert wichtig)

Jedes einzelne Netzwerk-Paket (KEINE Ausnahmen — alle Phasen: handshake, status/query,
login, config, play, common) muss vom Nutzer in der Config-GUI auswählbar (und damit
loggbar) sein. Maßstab: jedes Paket, das im Minecraft-Prozess in/ausgeht, muss im Logger
wiederfindbar sein. Nutzer-Vorgaben:

1. Erst nur Version **1.21** vollständig umsetzen (Build muss laufen).
2. Erst danach **1.21.1** porten; dort neue Pakete der Version ergänzen (Stonecutter-Syntax).
3. Andere Versionen (1.21.2–26.2) macht ein **anderer Agent** anhand dieses Doku + eines
   separaten `.md`-Dokuments (noch zu erstellen, NACH erfolgreichem 1.21-Build).
4. Skill `minecraft-fabric-development` ernst nehmen: Mappings-Quellen wirklich verwenden
   (`mappings/yarn/<version>/net/minecraft/network/packet/`), Stonecutter-Guide, Mappings-Guide.

## 2. Architektur des Mods (kurz)

- `SimpleConfigScreen.java` (~1000 Zeilen, 3 Version-Branches): 3 duplizierte Blöcke
  `S2C_PACKETS`/`C2S_PACKETS` (Zeilen **48/161**, **372/485**, **694/807**), je Branch.
  Arrays sind HARDCODED-Whitelists → das ist das Kernproblem (unvollständig, wartungsintensiv).
  Selector-Aufbau: `new DualListSelectorWidget(x,y,w,h,"S2C Packets...", S2C_PACKETS,
  new HashSet<>(config.selectedS2CPackets), selection -> {})` → `addDrawableChild`/`addRenderableWidget`.
- `DualListSelectorWidget.java`: nimmt `List<String> packets`, `Set<String> initialSelection`.
  Gibt `getSelectedPackets()` → `Set<String>` zurück. Konstruktor-Signaturen existieren je
  Version (Line 75 für 26.x, 420 für Yarn-Branches — prüfen beim Einbau).
- `PacketLogger.java` (~517 Zeilen): statische Filterung pro Paket:
  - `getDeobfuscatedName(packet)` (Line 383): `PACKET_NAMES.get(clazz) ?? clazz.getSimpleName()`.
  - `shouldLogS2C/shouldLogC2S` (Line 393/401): leere Selection → false (nichts loggen);
    sonst `simpleName.equals(selected) || simpleName.endsWith(selected)`.
  - `PACKET_NAMES` (Map<Class, String>, Line 54): per `registerUnpackers()` befüllt,
    version-spezifisch (`>=26.1` Mojang-Klassen → Yarn-Namen; `else` = Yarn-Klassen 1.21–1.21.11).
  - `registerPacket(clazz, name, unpacker)` und `registerPacketName(clazz, name)`.
  - `onWorldJoin/onWorldLeave` für File-Log-Sessions.
- `ModConfig.java`: `selectedS2CPackets`/`selectedC2SPackets` (List<String>), persistiert als
  JSON `packet-logger-config.json`. Names = einfache Yarn-Klassennamen (z.B. `ChatMessageS2CPacket`).
- `ClientConnectionMixin.java`: channelRead0/send — fängt ALLE Pakete aller Phasen.

## 3. Verifizierte Fakten aus der dekompilierten 1.21-Quelle

Quelle liegt jetzt lokal unter **`/tmp/opencode/mc121src`** (genSources fertig, Pfad
`.../net/minecraft/network/**`). WICHTIG: `/tmp` kann weggewischt werden → bei Bedarf
regenerieren mit `./gradlew :1.21:genSourcesWithVineflower` (Projekt-Root) und Jar in
Loom-Cache suchen (`.gradle/loom-cache/minecraftMaven/...`).

### Paket-Typen-Registrierung (1.21, Yarn) — das ist die Quelle der Wahrheit:

Klassen in `net.minecraft.network.packet.*`:

| Klasse | # `public static final PacketType`-Felder |
|---|---|
| `PlayPackets` | **163** (inkl. 4x `PlayerMoveC2SPacket`-Subtypen!) |
| `CommonPackets` | 16 |
| `ConfigPackets` | 7 |
| `CookiePackets` | 2 |
| `HandshakePackets` | 1 |
| `LoginPackets` | 9 |
| `PingPackets` | 2 |
| `StatusPackets` | 2 |
| **Summe** | **~202 PaketTypen** |

- `PacketType` ist ein **record**: `record PacketType<T extends Packet<?>>(NetworkSide side, Identifier id)`.
  Zugriff: `side()` → `NetworkSide` (SERVERBOUND=zum Server=C2S, CLIENTBOUND=zum Client=S2C),
  `id()` → Identifier, `toString()` = `side.getName() + "/" + id`.
- Feld-Beispiele `PlayPackets.java`: `public static final PacketType<BundleS2CPacket> BUNDLE = s2c("bundle");`
- Fabrik-Methoden (privat) am Ende von PlayPackets:
  `s2c(String)` → `PacketType<T extends Packet<ClientPlayPacketListener>>` (CLIENTBOUND),
  `c2s(String)` → `PacketType<T extends Packet<ServerPlayPacketListener>>` (SERVERBOUND).
- **Subtypen/Innere Klassen** existieren (wichtig für Namen!):
  `PlayerMoveC2SPacket.PositionAndOnGround`→MOVE_PLAYER_POS, `.Full`→MOVE_PLAYER_POS_ROT,
  `.LookAndOnGround`→MOVE_PLAYER_ROT, `.OnGroundOnly`→MOVE_PLAYER_STATUS_ONLY.
  `clazz.getSimpleName()` davon wäre nur `PositionAndOnGround` etc. → passt NICHT zur
  Whitelist-Eintragung `PlayerMoveC2SPacket`. → **aktueller Bug: PlayerMove wird in 1.21 nie geloggt.**
- Verzeichnisbaum `net/minecraft/network/packet/c2s/{common,config,handshake,login,play,query}/`
  und `net/minecraft/network/packet/s2c/{common,...}/` zeigt die Paketklassen.
  `query/` = QueryPingC2SPacket, QueryRequestC2SPacket (Status/Ping Phase).
- Alle Paketklassen erben `net.minecraft.network.packet.Packet<T>`; Listener-Typ
  (z.B. `ClientPlayPacketListener`) entscheidet via `side`/Fabrik die Richtung.

### Vergleich gegen Mappings (Nutzer-Anforderung „prüfen, ob alle drin sind"):

`tree mappings/yarn/1.21/net/minecraft/network/packet/` (Skill-Verzeichnis) enthält dieselben
8 Registry-Klassen (Common/Config/Cookie/Handshake/Login/Ping/Play/Status) für 1.21–1.21.11.
Also: **dynamische Enumeration über diese 8 Klassen deckt 1.21 vollständig ab.**

## 4. Lösungsansatz (gewählt): Dynamische Laufzeit-Enumeration

Statt 3 handgepflegte Whitelist-Arrays: zur Laufzeit ALLE registrierten `PacketType`-Felder
der 8 Registry-Klassen per Reflection sammeln → daraus die GUI-Listen und die Filter-Namen.

### Konkret (1.21/Yarn-Branch):

1. **Neue Utility** (z.B. `dev.redstone.packetlogger.logger.PacketCatalog`):
   - statisch die 8 Registry-Klassen auflisten (je Version-Branch unterschiedlich!):
     - Yarn (1.21–1.21.11): `CommonPackets, ConfigPackets, CookiePackets, HandshakePackets,
       LoginPackets, PingPackets, PlayPackets, StatusPackets` (alle `net.minecraft.network.packet.*`).
     - Mojang (26.x): `GamePacketTypes, CommonPacketTypes, LoginPacketTypes,
       ConfigurationPacketTypes, HandshakePacketTypes, StatusPacketTypes, PingPacketTypes,
       CookiePacketTypes` (alle `net.minecraft.network.protocol.*`, siehe /tmp/opencode/mc26src;
       PacketType ist record `(PacketFlow flow, Identifier id)` → **Methode heißt `flow()`, nicht `side()`!**)
   - `Field`-Iteration: `for (Field f : registryClass.getFields())` + Filter
     `PacketType.class.isAssignableFrom(f.getType())` → `PacketType<?> pt = (PacketType) f.get(null)`.
     Feld-Namen (`BUNDLE`) vs. generischer Typ: den generischen Typparameter entnehmen
     (`((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0]` → `Class<?>` Paketklasse).
     Fallback: aus der Instanz `pt` die Klasse via `id()` geht nicht direkt — daher Generics lesen.
   - **Namen bilden**: `name = PACKET_NAMES.getOrDefault(packetClass, enclosingSimpleName)`,
     wobei `enclosingSimpleName` bei inneren Klassen (`getEnclosingClass() != null`) der
     `getSimpleName()` der umschließenden Klasse ist (→ `PlayerMoveC2SPacket`). So gruppieren
     sich die 4 Move-Subtypen unter `PlayerMoveC2SPacket` und `shouldLogC2S` (`endsWith`) matcht.
     → **Konsistenz zentral**: GUI-Name == Name, den `getDeobfuscatedName` liefert. Dann ist
     automatisch jedes Paket auswählbar UND loggbar.
   - Richtung: `pt.side() == NetworkSide.CLIENTBOUND` → S2C-Liste; `SERVERBOUND` → C2S-Liste.
     (26.x: `pt.flow() == PacketFlow.CLIENTBOUND` → S2C.)
   - Ausgabe: `List<String> getS2CPacketNames()`, `getC2SPacketNames()` (sortiert, dedupliziert).

2. **SimpleConfigScreen**: in allen 3 Branch-Blöcken die statischen Arrays ersetzen durch
   `PacketCatalog.getS2CPacketNames()` / `getC2SPacketNames()` als Argumente des Selectors.
   (Arrays können bleiben, aber Inhalt = dynamisch; oder die Arrays komplett streichen und
   direkt aufrufen. Sauberer: Konstante `private static final List<String> S2C_PACKETS =
   PacketCatalog.getS2CPacketNames();` in JEDEM Branch.) Config-Loading (initialSelection)
   unverändert → alte Config-Werte bleiben gültig.

3. **PacketLogger**: `getDeobfuscatedName` auf die neue Namenslogik umstellen
   (enclosing-class-Fallback). `PACKET_NAMES` kann bleiben (Mojang→Yarn in 26.x,
   hier identische Namen in 1.21). Dadurch matchen alle GUI-Namen die Filterlogik.
   Die `endsWith`-Logik in `shouldLogS2C/C2S` deckt auch die 4 PlayerMove-Subtypen ab,
   sobald der Name `PlayerMoveC2SPacket` ist.

4. **26.x-Besonderheit** (nur fürs spätere Portierungs-`.md`): Paketklassen sind Mojang-named
   (`ClientboundPlayerChatPacket`), müssen via `PACKET_NAMES` auf Yarn-Namen gemappt werden.
   Die dynamische Enumeration muss in 26.x die `*PacketTypes`-Klassen + `flow()` nutzen und
   Namen ebenfalls durch `PACKET_NAMES.getOrDefault(...)` ziehen → GUI zeigt Yarn-Namen,
   Filter matcht. Manche Yarn-Namen existieren dort nicht als SimpleName → PACKET_NAMES
   ist dort die Pflicht-Quelle (bereits massiv befüllt, ~140 Zeilen).

## 5. Sicherzustellen / Fallstricke

- **Vollständigkeit**: Nach Umsetzung in 1.21 Liste mit `PlayPackets` etc. abgleichen
  (163+16+7+2+1+9+2+2=202 Einträge, dedupliziert ~ alle Paketklassen in
  `c2s/play`, `s2c/play`, `c2s/common`, `c2s/config`, `c2s/login`, `c2s/query`, `c2s/handshake`, …).
- **Keine leere Auswahl verwechseln**: leere Selection = nichts loggen (aktuelles Verhalten,
  nicht ändern, sonst Log-Flut). Nutzer wählt explizit.
- **Geschachtelte Klassen**: `getEnclosingClass`-Fallback ist der kritische Fix, damit
  `PlayerMoveC2SPacket` etc. unter einem Namen gruppiert werden.
- **Reflection-Sicherheit**: Registry-Klassen sind `public`, Felder `public static final` →
  `getFields()` + `field.get(null)` reicht; bei Exception `UnsupportedOperationException`
  o.ä. graceful skip + System.err-Hinweis.
- **Cache/Mem**: Enumeration einmalig in `static { }`-Block cachen (kein Wiederholen pro Frame).
- **Version-Branch-Disziplin (Stonecutter)**: ALLE Versionstypen kommen in `//?`-Blöcke
  (imports siehe SimpleConfigScreen Kopf). Beim Bearbeiten NIE Branch-Ebenen vermischen.
- **Build-Test nur 1.21**: `./gradlew :1.21:build` (oder `test_versions.sh . 1.21 build`).
  Aktiver Projekt-Zweig ist bereits 1.21 (via `stonecutterSwitchTo1.21`, BUILD SUCCESSFUL).
  Vorsicht: `stonecutter.gradle` aktive Version ggf. erneut prüfen.
- **Loom-Cache für Quellen**: `~/.gradle/caches/fabric-loom/minecraftMaven` hat nur
  merged-deobf 26.1/26.2, keine Quellen → genSources notwendig (bereits gelaufen).

## 6. Verifizierte 26.x-API (für Portierungs-Doku)

Aus `/tmp/opencode/mc26src` (extrahiertes 26.1-Quellen-Jar):
- `PacketType` = record `(PacketFlow flow, Identifier id)` — **kein `side()`**, Zugriff `flow()`.
- Registry-Klassen mit statischen Feldern: `GamePacketTypes` (61 SERVERBOUND_* + 127 CLIENTBOUND_*),
  `CommonPacketTypes`, `LoginPacketTypes`, `ConfigurationPacketTypes`, `StatusPacketTypes`,
  `HandshakePacketTypes` (nur `CLIENT_INTENTION = createServerbound("intention")`),
  `CookiePacketTypes`, `PingPacketTypes`.
- `ConnectionProtocol` enum: HANDSHAKING/PLAY/STATUS/LOGIN/CONFIGURATION (kein QUERY —
  Status/Ping leben in `StatusPacketTypes`/`PingPacketTypes`).
- `GameProtocols`: `SERVERBOUND_TEMPLATE`, `createServerboundProtocol`, `createClientboundProtocol`, `CodecModifier`.

## 7. Nächste konkrete Schritte (Reihenfolge)

1. `PacketCatalog` erstellen (nur 1.21/Yarn-Branch + leere 26.x-/1.21.9+-Platzhalter in
   `//?`-Blöcken, damit alle Versionen kompilieren).
2. `PacketLogger.getDeobfuscatedName` auf enclosing-Class-Logik umstellen.
3. In `SimpleConfigScreen` die 3×2 statischen Arrays durch dynamische Aufrufe ersetzen.
4. `./gradlew :1.21:build` bis grün.
5. Vollständigkeits-Check: enumerierte Listen == alle Paketklassen (siehe 5.).
6. 1.21.1: `./gradlew :1.21.1:build` — neue Pakete dieser Version via Mappings vergleichen
   (`mappings/yarn/1.21.1/net/minecraft/network/packet/`), nötigenfalls Registry-Klassen/
   Paketklassen anpassen (Stonecutter-Syntax, da die 8 Registry-Klassen namensgleich bleiben,
   dürfte nur das 1.21.1-Jar andere Paketklassen enthalten).
7. Danach separates Portierungs-`.md` für die anderen Agenten schreiben (26.x-Punkte aus 6.).

## 8. Wichtige Pfade

- Projekt: `/home/timon/devhub/Fabric-Package-Logger 1.21.4`
- 1.21-Quellen: `/tmp/opencode/mc121src/net/minecraft/network/packet/`
- 26.x-Quellen: `/tmp/opencode/mc26src/net/minecraft/network/`
- Mappings: `/home/timon/ai/skills/minecraft-fabric-development/mappings/yarn/<v>/net/minecraft/network/packet/`
- Guides: `/home/timon/ai/skills/minecraft-fabric-development/guides/stonecutter_guide.md`,
  `.../guides/mappings.md`
- GenSources-Log: `/tmp/opencode/fetch121.log`
- Skill-Skript: `/home/timon/ai/skills/minecraft-fabric-development/scripts/fetch_sources.sh . 1.21 /tmp/opencode/mc121src`
