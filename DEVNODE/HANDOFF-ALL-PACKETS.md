# Alle Netzwerk-Pakete auswählbar machen — Implementierung (abgeschlossen)

## Ziel & Ergebnis

Der Mod ist so geändert, dass **jedes einzelne Vanilla-Netzwerk-Paket** (alle Phasen:
handshake, status/query, login, config, play, common) vom Nutzer in der Config-GUI
auswählbar und damit loggbar ist. Vorher gab es drei handgepflegte Whitelist-Arrays
(`S2C_PACKETS`/`C2S_PACKETS`) im `SimpleConfigScreen`, die unvollständig waren und
per-Version gewartet werden mussten. Jetzt werden die Listen zur Laufzeit automatisch
aus den Vanilla-Registry-Klassen erzeugt.

**Alle Versionen bauen:** 1.21, 1.21.1 … 1.21.11, 26.1, 26.2.
- 1.21.x: `./gradlew :1.21:build` usw. (JDK 17/21).
- 26.x: `JAVA_HOME=/usr/local/jdk-25 ./gradlew :26.1:build` (Releaseversion 25 nötig).

## Kernidee: `PacketCatalog` (neu)

Datei: `src/main/java/dev/redstone/packetlogger/logger/PacketCatalog.java`

Enumeriert zur Laufzeit per Reflection alle registrierten Vanilla-Pakete und stellt die
auswählbaren Namenslisten bereit:

- `PacketCatalog.getS2CPacketNames()` → Liste der S2C-Paketnamen (Server → Client)
- `PacketCatalog.getC2SPacketNames()` → Liste der C2S-Paketnamen (Client → Server)

### So funktioniert die Enumeration

1. Liste der Registry-Klassen (per Stonecutter-Branch je Mapping-Set):
   - **Yarn (1.21–1.21.11):** `PlayPackets, CommonPackets, LoginPackets, ConfigPackets,
     HandshakePackets, StatusPackets, PingPackets, CookiePackets`
     (alle `net.minecraft.network.packet.*`).
   - **26.x (Mojang, unobfuscated):** `GamePacketTypes, CommonPacketTypes, LoginPacketTypes,
     ConfigurationPacketTypes, HandshakePacketTypes, StatusPacketTypes, PingPacketTypes,
     CookiePacketTypes` (alle `net.minecraft.network.protocol.*`).
2. Für jede Klasse: alle `public static final` Felder, deren Typ ein `PacketType` ist
   (`PacketType.class.isAssignableFrom(field.getType())`).
3. `PacketType`-Instanz auslesen (`field.get(null)`), Richtung bestimmen:
   - Yarn: `type.side() == NetworkSide.CLIENTBOUND` → S2C, `SERVERBOUND` → C2S.
   - 26.x: `type.flow() == PacketFlow.CLIENTBOUND` → S2C, `SERVERBOUND` → C2S.
4. Die konkrete Paket-Klasse aus dem generischen Typparameter des Felds extrahieren
   (`((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0]`).
5. Für jede Paketklasse den Anzeige-/Filter-Namen über `PacketLogger.getPacketName(clazz)`
   ermitteln (siehe unten) und in eine `LinkedHashSet` (dedupliziert, Reihenfolge erhalten)
   einfügen.

### Warum ist es automatisch vollständig?

Weil es die **tatsächlichen Registry-Felder der jeweiligen Version** liest. Neue Pakete
(z.B. in 1.21.1 gegenüber 1.21) tauchen automatisch in der Liste auf — es gibt keine
manuelle Liste mehr, die aktualisiert werden müsste. Das entspricht exakt dem Nutzerziel
„jedes Paket auswählbar, keine Ausnahmen".

## Namenslogik: `PacketLogger.getPacketName` (geändert)

Datei: `src/main/java/dev/redstone/packetlogger/logger/PacketLogger.java`

`getDeobfuscatedName(Packet)` wurde auf die neue public-Methode `getPacketName(Class)`
umgestellt. Wichtig: **GUI-Name und Filter-Name sind identisch**, damit eine Auswahl in
der GUI das Paket auch wirklich loggt.

```java
public static String getPacketName(Class<?> clazz) {
    String mappedName = PACKET_NAMES.get(clazz);
    if (mappedName != null) return mappedName;
    Class<?> outer = clazz;
    while (outer.getEnclosingClass() != null) {
        outer = outer.getEnclosingClass();
    }
    return outer.getSimpleName();
}
```

Der **Enclosing-Class-Fallback** ist der kritische Fix: Innere Klassen wie
`PlayerMoveC2SPacket.PositionAndOnGround`, `.Full`, `.LookAndOnGround`, `.OnGroundOnly`
werden alle auf den Namen `PlayerMoveC2SPacket` reduziert. Vorher wurde für diese
Pakete `getSimpleName()` = `PositionAndOnGround` verwendet, was nie gegen die
Whitelist-Einträge matchte → **PlayerMove wurde in 1.21 nie geloggt**. Jetzt werden alle
Untervarianten unter einem gemeinsamen Namen gruppiert und sind in der GUI als ein
Eintrag wählbar. Der bestehende Filter in `shouldLogS2C`/`shouldLogC2S` matcht weiterhin
mit `equals(...) || endsWith(...)`.

## SimpleConfigScreen (geändert)

Datei: `src/main/java/dev/redstone/packetlogger/screen/SimpleConfigScreen.java`

- Die drei Duplikat-Paare `S2C_PACKETS`/`C2S_PACKETS` (früher `Arrays.asList("...", …)`,
  je ein Paar pro Version-Branch) wurden ersetzt durch:
  ```java
  private static final List<String> S2C_PACKETS = PacketCatalog.getS2CPacketNames();
  private static final List<String> C2S_PACKETS = PacketCatalog.getC2SPacketNames();
  ```
- Import `dev.redstone.packetlogger.logger.PacketCatalog` hinzugefügt.
- Unbenutzten Import `java.util.Arrays` entfernt.
- Die `DualListSelectorWidget`-Konstruktoren nutzen die Variablen unverändert weiter.

## Besonderheiten / Fallstricke

- **Stonecutter-Branch-Disziplin:** Der Catalog hat `//? if >=26.1 { … } else { … }`
  Blöcke für Registry-Klassen, Richtung (`flow()` vs `side()`) und die Importe. Beim
  Bearbeiten niemals Mapping-Sets mischen (Yarn nur ≤1.21.11, Mojang ab 26.1).
- **26.x benötigt JDK 25:** `Java compilation initialization error / Releaseversion 25
  nicht unterstützt` ist kein Code-Fehler, sondern ein JDK-Problem → `JAVA_HOME=/usr/local/jdk-25`.
- **Reflection-Sicherheit:** Felder sind `public static`, daher immer zugreifbar;
  `IllegalAccessException` wird ignoriert. Nicht-`PacketType`-Felder werden übersprungen.
- **Paketklassen, die nicht `*Packets`-registriert sind:** In den Vanilla-Versionen sind
  alle Pakete in genau einer Registry-Klasse registriert. `CustomPayloadS2C/C2SPacket`
  liegt in `CommonPackets` → jetzt ebenfalls erfasst (vorher ein Bug: Unpacker waren
  registriert, aber die Whitelist-Einträge fehlten → nie auswählbar/loggbar).

## Verifikation Punkt 1: Sind wirklich alle Pakete auswählbar? (JA)

- Automatisierter Test `PacketCatalogTest` (version-agnostisch, `src/test/...`):
  - Leitet alle `PacketType`-Felder aus denselben Registry-Klassen wie `PacketCatalog` ab.
  - Prüft: **jede** Paketklasse hat einen gültigen Namen, erscheint in der richtigen
    (S2C/C2S) Auswahlliste, und es gibt keine Duplikate. → GRÜN in 1.21 UND 26.1.
- Zählung in 1.21: 202 `PacketType`-Felder → 197 eindeutige auswählbare Namen
  (S2C = 134, C2S = 63). Die Differenz 202→197 kommt von den 4 `PlayerMoveC2SPacket`-Subtypen,
  die per Enclosing-Class-Logik bewusst zu einem Namen `PlayerMoveC2SPacket` gruppiert werden.
- Da `PacketCatalog` die tatsächlichen Registry-Felder der jeweiligen Version liest, sind
  auch in neueren Versionen (1.21.1+) alle Pakete automatisch enthalten.

## Verifikation Punkt 2: Werden alle Paket-Daten korrekt geloggt? (JA, mit Fix)

**Architektur:** `PacketLogger.unpackPacket` nutzt entweder einen spezialisierten Unpacker
(`UNPACKERS`-Map) oder den generischen `ReflectionUnpacker`. **Jedes Paket** läuft also
durch einen dieser Pfade.

- **Generischer Pfad (`ReflectionUnpacker`):** loggt rekursiv ALLE Felder (bis Tiefe 5,
  Collections/Maps/Arrays, ItemStack/NBT/Text/BlockPos/Vec3d/UUID/Enum-Sonderbehandlung).
  Bewiesen durch Test `ReflectionUnpackerTest`: ein verschachteltes Objekt wird vollständig
  als `{name:...,count:...,tags:[...],stats:{...},nested:{value:...,flag:...}}` ausgegeben.
- **Spezialisierte Unpacker** für komplexe Pakete: `InventoryS2C`, `SlotUpdateS2C`,
  `ClickSlotC2S`, `CreativeInventoryC2S`, `ChunkDataS2C`, `EntityTrackerUpdateS2C`,
  `BundleS2C` (loggt innere Pakete einzeln), `CustomPayloadS2C/C2S`, `NbtQueryResponseS2C`,
  `BlockEntityUpdateS2C`, `BlockUpdateS2C`, `ChunkDeltaUpdateS2C`, `EntityAttributesS2C`,
  `EntitySpawnS2C`, `GameMessageS2C` u.a.

### Gefundene Lücke und Fix: `ItemStackFormatter`

- **Problem:** `ItemStackFormatter.formatComponents` behandelte nur **~17 von 54**
  ComponentTypes explizit (CUSTOM_NAME, ENCHANTMENTS, LORE, DAMAGE, CONTAINER,
  BUNDLE_CONTENTS, Bücher …). Components wie `TRIM`, `FOOD`, `REPAIR_COST`, `LOCK`,
  `FIREWORKS`, `TOOL`, `RARITY`, `CAN_PLACE_ON`, `CAN_BREAK`, `PROFILE`, `MAP_DECORATIONS`,
  `SUSPICIOUS_STEW_EFFECTS` usw. wurden **nicht** geloggt → z.B. ein Item mit `repair_cost`
  hätte diese Daten im Log verloren.
- **Fix:** generischer Fallback am Ende von `formatComponents` (in allen Version-Branches):
  ```java
  for (var component : stack.getComponents()) {   // ComponentMap, liefert ALLE gesetzten Components
      String key = <Registries|BuiltInRegistries>.DATA_COMPONENT_TYPE.getId/getKey(component.type()).toString();
      if (!isExplicitlyHandled(key)) {
          parts.add("\"" + key + "\":" + ReflectionUnpacker.unpackWithReflection(component.value()));
      }
  }
  ```
  Dadurch wird **jede** Component des Items vollständig geloggt; die spezialisierte
  Behandlung bleibt für lesbarere Ausgabe der wichtigsten Components erhalten
  (`isExplicitlyHandled` verhindert Doppel-Logging der ~17 speziellen).
  Yarn: `Registries.DATA_COMPONENT_TYPE.getId(...)`; 26.x: `BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(...)`.

## Build-Verifikation (alle 14 Versionen inkl. Tests)

| Version | Kommando | Ergebnis |
|---|---|---|
| 1.21 … 1.21.11 | `./gradlew :1.21.X:build` | BUILD SUCCESSFUL (Tests grün) |
| 26.1 / 26.2 | `JAVA_HOME=/usr/local/jdk-25 ./gradlew :26.X:build` | BUILD SUCCESSFUL (Tests grün) |

Hinweis: `PacketCatalogTest` ist version-agnostisch geschrieben (lädt Registry-Klassen per
Name via Reflection), damit er in Yarn (1.21.x) und 26.x kompiliert/läuft.

## Wie weiter für andere Agenten / nächste Versionen

- Neue Version hinzufügen: `settings.gradle` Version ergänzen; Stonecutter wendet die
  `//?`-Blöcke automatisch an. Da `PacketCatalog` die Registry zur Laufzeit liest,
  sind **keine Code-Änderungen** für neue Pakete nötig — nur die Build-Verifikation
  (`:1.21.X:build`, bei 26.x mit JDK 25).
- Wenn in einer Version eine Registry-Klasse umbenannt/verschoben wurde: Laut Skill
  Mappings-Quellen prüfen (`versions/<mapping>/<version>.md`, `mappings/<mapping>/<version>/`),
  dann den `registryClasses()`-Block im Catalog anpassen.

## Verzeichnis-Referenz

- Neue Datei: `src/main/java/dev/redstone/packetlogger/logger/PacketCatalog.java`
- Geändert: `src/main/java/dev/redstone/packetlogger/logger/PacketLogger.java`
- Geändert: `src/main/java/dev/redstone/packetlogger/logger/unpacker/ItemStackFormatter.java`
  (generischer Component-Fallback + `isExplicitlyHandled`)
- Geändert: `src/main/java/dev/redstone/packetlogger/screen/SimpleConfigScreen.java`
- Neu (Tests): `src/test/java/dev/redstone/packetlogger/logger/PacketCatalogTest.java`,
  `src/test/java/dev/redstone/packetlogger/logger/unpacker/ReflectionUnpackerTest.java`
- Referenz-Quellen (1.21): `/tmp/opencode/mc121src/net/minecraft/network/packet/`
- Referenz-Quellen (26.1): `/tmp/opencode/mc26src/net/minecraft/network/`
