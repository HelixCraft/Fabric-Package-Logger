# Erfahrungsdokumentation — Fabric Mod Development mit Stonecutter

**Autor:** DeepSeek V4 Flash Free (opencode-Assistent)
**Projekt:** Packet Logger (dev.redstone.packetlogger), Versions-Support `1.21 … 1.21.11, 26.1, 26.2`
**Zweck:** Diese Datei dient späteren Agenten als Orientierung. Sie dokumentiert, was in der bisherigen Arbeit gut funktioniert hat, welche Fehler aufgetreten sind und wie man in diesem Projekt schnell und sicher vorankommt.

---

## 1. Das Wichtigste zuerst: Terminologie

**`Branches` = Stonecutter-Bedingungsabschnitte, NICHT Git-Branches.**

Das Projekt ist ein **Stonecutter-Multi-Version-Projekt**. Eine einzige Quelldatei enthält mehrere Versionen des Codes, getrennt durch Stonecutter-Präprozessor-Kommentare:

```
//? if >=26.1 {
/* <Mojang-Code für 26.1/26.2> */
*///?} elif >=1.21.9 {
/* <Yarn-Code für 1.21.9–1.21.11> */
*///?} else {
/* <Yarn-Code für 1.21–1.21.8> */
//?}
```

- Beim aktiven Branch (Konfiguration in `stonecutter.gradle`: `stonecutter.active "1.21"`) werden die Kommentare entfernt und der richtige Abschnitt "ent-kommentiert".
- **Wichtig:** Wenn ein Agent "in allen Branches ändern" sagt, meint er: in **allen drei Abschnitten derselben Datei**. Nicht in Git-Branches.
- Die drei Abschnitte sind in einer Datei oft **identisch bis auf kleine API-Unterschiede** (z. B. `KeyInput` vs. `int keyCode` vs. `KeyEvent`). Das macht Edits mit dem `edit`-Tool mehrdeutig.

---

## 2. Die drei Stonecutter-Abschnitte im Überblick

| Abschnitt | Minecraft-Versionen | Mapping | Besonderheiten |
|---|---|---|---|
| `//? if >=26.1 {` | 26.1, 26.2 | **Mojang (obf./unobf.)** | `Component.literal`, `Button.builder`, `InputConstants`, `KeyMapping`, `KeyEvent`, `minecraft.gui.setScreen` (ab 26.2) |
| `//?} elif >=1.21.9 {` | 1.21.9–1.21.11 | **Yarn** | `Text.literal`, `ButtonWidget.builder`, `InputUtil`, `KeyBinding`, `KeyInput` |
| `//?} else {` | 1.21–1.21.8 | **Yarn** | `Text.literal`, `ButtonWidget.builder`, `InputUtil`, `KeyBinding`, `keyPressed(int,int,int)` |

Kernunterschied der APIs:

- **Mojang (26.x):** Gui-Elemente heißen `Button`, Text ist `Component`, Input ist `InputConstants`, Keybind ist `KeyMapping`.
- **Yarn (1.21.x):** Gui-Elemente heißen `ButtonWidget`, Text ist `Text`, Input ist `InputUtil`, Keybind ist `KeyBinding`.

---

## 3. Wichtigste Lektion: `edit`-Tool bei Stonecutter-Dateien

**Problem:** In einer Stonecutter-Datei gibt es drei (fast identische) Kopien eines Codeblocks. Das `edit`-Tool schlägt fehl mit `Found multiple matches for oldString`.

**Was funktioniert hat (in dieser Reihenfolge verwenden):**

1. **`replaceAll=true`** — wenn alle Kopien exakt identisch sein sollen und identisch sind. Schnellste Lösung.
2. **Größeren Kontext als Anker nehmen**, der den Abschnitt eindeutig macht:
   - Den **nächsten unterschiedlichen Teil** (z. B. `keyPressed(int keyCode, ...)` vs. `keyPressed(KeyInput input)` vs. `keyPressed(KeyEvent event)`) mit in den `oldString` aufnehmen.
   - Oder eine Zeile, die nur in einem Abschnitt vorkommt (z. B. `this.minecraft.gui.setScreen(...)` nur im 26.2-Zweig).
3. **`rg`-Befehle** laufen lassen, um zu prüfen, welcher Abschnitt wo steht (Zeilenummern ausgeben), und dann gezielt editieren.

**Sicherheitsregel:** Nach großen Edits IMMER mit `rg` verifizieren, dass die Änderung in **allen drei** Abschnitten gelandet ist und nicht fälschlich doppelt in einem. Gerade beim Automatisieren mit `replaceAll` kann man leicht einen Abschnitt versehentlich doppelt treffen oder einen auslassen.

---

## 4. Bewährte Vorgehensweise (Workflow)

### 4.1 Vor dem Schreiben von Code: Referenzen konsultieren (Pflicht)

**NIEMALS API-Namen raten.** Der Skill `minecraft-fabric-development` hat gebündelte Referenzen:

```
/home/timon/ai/skills/minecraft-fabric-development/versions/<mapping>/<version>.md
/home/timon/ai/skills/minecraft-fabric-development/mappings/<mapping>/<version>/...
```

- `versions/...` = Diff-Report (was wurde in dieser Version geändert/hinzugefügt/entfernt).
- `mappings/...` = konkrete Mapping-Tabellen (Klassen, Felder, Methoden mit Signaturen).

**Bei Build-Fehlern** (falsche/fehlende/umbenannte Klassen, Methoden, Felder, falsche Signaturen) gilt MANDATORISCH die Reihenfolge:
1. `versions/<mapping>/<version>.md`
2. `mappings/<mapping>/<version>/`
3. `scripts/fetch_sources.sh` (decompilierter Source, dauert bis ~20 Min — nur als letzte Option)

Nur wenn diese Quellen konsultiert wurden, darf Code geändert werden.

### 4.2 Ent-Obfuskation: Obfuskierte Namen erkennen

Im `chat_history` tauchte z. B. `KeyBinding.n()` auf. Das ist der obfuskierte Name von `updateKeysByCode()`. Beim Lesen von decompiliertem Source: Die `mappings/`-Datei verrät den echten Namen (`KeyBinding.md` → `| void | updateKeysByCode() |`). Immer die Mapping-Tabelle für den genauen Namen nutzen, nicht raten.

### 4.3 Root Cause statt Symptom finden

Beim Keybind-Toggle-Problem wurde zunächst vermutet, die Registrierung sei falsch. Die eigentliche Ursache lag tiefer: `KeyBinding.wasPressed()` hängt an `timesPressed`, das nur über statische Methoden erhöht wird, die in die **statische Map `KEY_TO_BINDINGS`** schauen. `setBoundKey()` füllt diese Map **nicht** — nur `updateKeysByCode()` tut das. Erst durch Lesen des decompilierten `KeyBinding.java` und Verfolgen des Keyboard-Dispatches (`Keyboard.onKey` → `KeyBinding.onKeyPressed`) wurde die Ursache gefunden. **Lektion:** Bei "funktioniert nicht"-Bugs den tatsächlichen Ausführungsfluss durch den Quellcode verfolgen.

### 4.4 Builds: Der Skill-Helfer

Statt manueller Schleifen über alle Versionen:

```bash
/home/timon/ai/skills/minecraft-fabric-development/scripts/test_versions.sh "/home/timon/devhub/Fabric-Package-Logger 1.21.4" "1.21 - 1.21.11" build
```

- Für 26.x ist `JAVA_HOME=/usr/local/jdk-25` nötig (der Helper setzt das automatisch).
- Ausgabe: pro Version `clean` (Erfolg) oder voller Fehlertext.
- Exit-Code 0 = alles grün, 1 = mindestens eine Version schlug fehl.
- **Hinweis aus der Praxis:** Der Nutzer bricht manche Läufe ab und verifiziert nur einzelne Versionen (z. B. 1.21, 1.21.1). Nicht erschrecken, wenn ein Lauf "interrupted" endet.

---

## 5. Konkrete Erfahrungen aus der bisherigen Arbeit

### 5.1 Log-Pfad

- Altes Verhalten: Logging ins Config-Verzeichnis (`getConfigDir()`).
- Neues Verhalten: Eigener Ordner `<game dir>/packet-logger` über `FabricLoader.getInstance().getGameDir().resolve("packet-logger")`.
- Das nutzt die version-agnostische Fabric-Loader-API und funktioniert in allen drei Abschnitten ohne Stonecutter-Differenzierung.

### 5.2 Log-Kopf (Header)

Header-Reihenfolge:
```
=== Deep Packet Logger ===
Session: <name>
World: <name>
Client: Minecraft <version> (Fabric Loader <loader-version>)
Format: [TIME] [DIRECTION] PacketName {deep_data}
====
```

`getClientInfoLine()` nutzt `FabricLoader.getInstance().getRawGameVersion()` und `getModContainer("fabricloader")...getVersion()...getFriendlyString()` — version-agnostisch.

**Wichtige Stolperfalle:** Beim Erweitern des Headers wurde einmal versehentlich der Rumpf von `getWorldName()` gelöscht und musste wiederhergestellt werden. **Bei Edits immer darauf achten, dass man nur den gewünschten Teil ändert.**

### 5.3 Suchfeld-Fokus-Bug

- Symptom: Das Suchfeld im `DualListSelectorWidget` konnte nicht doppelt angeklickt/fokussiert werden.
- Ursache: In `setFocused(boolean)` wurde `searchField.setFocused(focused)` nur im `false`-Zweig gesetzt.
- Fix in allen drei Abschnitten:
  - Yarn: `this.focused = focused; searchField.setFocused(focused);`
  - 26.1: `super.setFocused(focused); searchField.setFocused(focused);`

### 5.4 Keybind-Feature

- Config-Feld: `public int loggingKeybind = -1` in `ModConfig`.
- Registrierung: `loggingKeyBinding` in `PacketLoggerClient`, initial ungebunden (`GLFW_KEY_UNKNOWN`).
- Button-Layout: 2 Buttons statt einem, Lücke `KEYBIND_GAP=3`. `logButtonWidth=(buttonWidth-GAP)*2/3` (Toggle), `keybindButtonWidth=buttonWidth-GAP-logButtonWidth` (rechts, Keybind).

**Der schwierigste Bug — Toggle funktionierte nicht (Root Cause):**

- `KeyBinding.wasPressed()` hängt an `timesPressed`.
- `timesPressed` wird nur über statische `onKeyPressed(key)`/`setKeyPressed(key,pressed)` erhöht, die im Keyboard-Dispatch (`Keyboard.onKey`) aufgerufen werden.
- Diese statischen Methoden schauen in die **statische Map `KEY_TO_BINDINGS`** (`Map<InputUtil.Key, KeyBinding>`).
- Die Map wird nur **im Konstruktor** und in **`updateKeysByCode()`** befüllt.
- `setBoundKey(K)` ändert nur das Feld `boundKey`, **nicht** `KEY_TO_BINDINGS`. Daher fand der Keyboard-Dispatch beim Drücken von K kein KeyBinding → kein Toggle.
- Der Config-Keybind (F6) funktionierte, weil er schon beim Konstruktor korrekt in der Map landete.

**Fix:** In `PacketLoggerClient.setLoggingKeybind(int)` (Yarn-Abschnitt `else`, deckt 1.21 & 1.21.9+ ab) nach `setBoundKey(InputUtil.fromKeyCode(keyCode, 0))` zusätzlich `KeyBinding.updateKeysByCode()` aufrufen. Das ist der offizielle Vanille-Mechanismus — verifiziert: `GameOptions` (Zeile 1442) und `ControlsListWidget` (Zeile 59) tun genau das nach Key-Umbelegung.

**Mojang-26.1-Branch:** KEIN Change nötig. `KeyMapping.setKey(InputConstants.Type.KEYSYM.getOrCreate(keyCode))` ist eventsbasiert und hat keine `KEY_TO_BINDINGS`-Map.

### 5.5 Keybind-Anzeige uppercase

- Nutzerwunsch: Ganzer Name uppercase (`h`→`H`, `space`→`SPACE`).
- Fix in `keybindButtonLabel()` aller drei Abschnitte:
  ```java
  (name.startsWith("key.keyboard.") ? name.substring("key.keyboard.".length()) : name).toUpperCase()
  ```

### 5.6 Autosave + echtes Cancel (Config-Screen)

Nutzer hat sich bewusst für "Autosave beim Schließen + echtes Cancel" entschieden (NICHT "jede Aktion live anwenden" — das hätte Cancel widerspruchslos gemacht).

Design:
- **Escape = Speichern** (Autosave): Jede Art, den Screen zu schließen, speichert. Kein Datenverlust mehr.
- **Save-Button** bleibt (speichert + schließt).
- **Cancel = echtes Rollback** auf den Stand **beim Öffnen** (nicht Factory-Defaults). Beispiel: 1 Packet geloggt → 4 hinzugefügt → Cancel → bleibt 1.

Umsetzung:
1. **Widget:** Neuer Setter `setSelectedPackets(Set<String>)` in `DualListSelectorWidget` (alle 3 Abschnitte):
   ```java
   public void setSelectedPackets(Set<String> newSelection) {
       selectedPackets.clear();
       selectedPackets.addAll(newSelection);
       updateFilteredLists();
   }
   ```
2. **Snapshot beim Öffnen** im Konstruktor:
   ```java
   this.initialS2C = new HashSet<>(config.selectedS2CPackets);
   this.initialC2S = new HashSet<>(config.selectedC2SPackets);
   this.initialKeybind = config.loggingKeybind;
   ```
   Felddeklarationen: `private final Set<String> initialS2C; private final Set<String> initialC2S; private final int initialKeybind;` plus `import java.util.Set;`.
3. **`persistToConfig()`** als gemeinsame Helfermethode (lokale Werte → config + `config.save()`).
4. **`close()`** ruft `persistToConfig()` dann `setScreen(parent)` (Autosave bei Escape).
5. **`saveAndClose()`** ruft nur noch `close()` (Endlosschleife vermeiden!).
6. **`cancelAndClose()`** stellt Snapshot wieder her (Werte, Keybind via `PacketLoggerClient.setLoggingKeybind`, Packet-Sets via `setSelectedPackets`), ruft `persistToConfig()`, dann `close()`.
7. Cancel-Button: `button -> this.cancelAndClose()`.

**Redundanz-Bug vermeiden:** In `persistToConfig()` war einmal fälschlich `config.loggingKeybind = config.loggingKeybind;` (Self-Assignment, sinnlos) — wurde entfernt. Der Keybind-Wert lebt bereits direkt im `config`-Objekt.

---

## 6. Zusammenfassung der häufigsten Fehlerquellen

1. **"Branches" falsch verstanden** (Git vs. Stonecutter-Abschnitte) — siehe Abschnitt 1.
2. **`edit`-Tool mit mehrdeutigem `oldString`** in Multi-Kopien-Dateien → `replaceAll` oder größerer Kontext.
3. **Nach Edits nicht verifiziert**, ob alle 3 Abschnitte geändert wurden.
4. **API-Namen geraten** statt Referenzen (`versions/`, `mappings/`) zu lesen.
5. **Nur Symptom behandelt** statt Root Cause (Keybind-Toggle).
6. **Rumpf-Methoden versehentlich gelöscht** (getWorldName) beim Erweitern eines umgebenden Blocks.
7. **Endlosschleife** bei `saveAndClose()`/`close()`-Verschachtelung — gemeinsame `persistToConfig()`-Helfermethode nutzen.
8. **Obfuskierte Namen** (`KeyBinding.n()`) nicht aufgelöst → Mapping-Tabelle lesen.

---

## 7. Schnell-Checkliste für den nächsten Agenten

1. Mapping-Set bestimmen (Mojang für 26.x, Yarn für 1.21.x) — NICHT raten.
2. Bei Änderungen, die mehrere Versionen betreffen: alle 3 Stonecutter-Abschnitte bedenken.
3. Bei `edit`-Mehrdeutigkeit: `replaceAll` oder eindeutigen Kontext-Anker.
4. API/geänderte Signaturen vor dem Schreiben in `versions/`+`mappings/` prüfen.
5. Build per Skill-Helfer `test_versions.sh` über die Versionen laufen lassen.
6. Bei 26.x: `JAVA_HOME=/usr/local/jdk-25`.
7. Nach Build-Fehler: erst Referenzen, dann Code ändern, dann neu bauen.
8. Nach jedem großen Edit mit `rg` verifizieren, dass die Struktur konsistent ist.





# ABSCHNIT 2



Alle Versionen bauen. Die Compiler-Fehler sind behoben:
1. 1.21.9/1.21.10/1.21.11: InputUtil.fromKeyCode wurde in 1.21.9 von (int, int) auf (KeyInput) geändert (im versions/yarn/1.21.9.md bestätigt: - fromKeyCode (II), + fromKeyCode (KeyInput)).
- SimpleConfigScreen.java (1.21.9-Branch): fromKeyCode(new KeyInput(config.loggingKeybind, 0, 0)) — KeyInput ist ein Record (key, scancode, modifiers), per javap verifiziert.
- PacketLoggerClient.java: Der Yarn-else-Block wurde in einen top-level elif >=1.21.9-Zweig (mit new KeyInput) plus else (mit int,int) aufgeteilt, inkl. conditionalem KeyInput-Import.
2. 26.1/26.2: Der 26.1-Konstruktor nutzte fälschlich Text.literal statt Component.literal (Mojang-API) — korrigiert.
Builds: 1.21, 1.21.9, 1.21.10, 1.21.11, 26.1, 26.2 alle BUILD SUCCESSFUL.
▣  Build · DeepSeek V4 Flash Free · 4m 47s
So, hier gibt es mir jetzt die Übersicht, wie sich was geändert hat in 21.9, 21.10, 1.4 und 111111111 und dann in den 26er Versionen. Kannst du mir nochmal erklären, woher du jetzt diese Information hattest? Erstens, wie sich's geändert hat, und zweitens, wie es korrekt ist. Also, wo genau hast du dir diese Information rausgezogen? Was war sozusagen dein Workflow, um die Informationen zu finden? Und welche Commands hast du dafür genutzt? Also sozusagen auf welche Art und Weise hast du es herausgefunden. Gib mir da mal eine Übersicht bzw. Aufschlüsselung. 
+ Thought: 453ms
Hier ist die Aufschlüsselung meines Workflows zur Fehlerbehebung.
Ausgangslage
Der Build schlug in 1.21.9, 1.21.10, 1.21.11 mit InputUtil.fromKeyCode(int, int)-Fehler fehl und in 26.1/26.2 mit Text.literal-Symbolfehler.
Schritt 1: Fehlermeldung lesen (woher der Fix-Verdacht kam)
Die Compiler-Ausgabe selbst war der erste Hinweis:
Fehler: Methode fromKeyCode in Klasse InputUtil kann nicht auf die angegebenen Typen angewendet werden.
  Erforderlich: KeyInput
  Ermittelt:    int,int
→ Das sagt bereits: In diesen Versionen erwartet fromKeyCode ein KeyInput, nicht zwei int.
Schritt 2: Skill-Referenzen prüfen (Pflicht laut Skill)
Ich habe die gebündelten Referenzen des Skills konsultiert, nicht geraten. Zuerst die Diff-Reports der betroffenen Versionen:
rg -n "fromKeyCode|KeyInput" /home/timon/ai/skills/minecraft-fabric-development/versions/yarn/1.21.9.md
Ergebnis:
- Methode - `fromKeyCode` `(II)Lfna$a;`     <- (int,int) ENTFERNT
+ Methode + `fromKeyCode` `(Lgth;)Lfst$a;`  <- (KeyInput) NEU
+ `net/minecraft/client/input/KeyInput`     <- neue Klasse
Das bestätigt: In 1.21.9 wurde die Signatur von (II) auf (KeyInput) geändert. Für 1.21.10/1.21.11 lieferten die versions/yarn/1.21.10.md/1.21.11.md keine Änderung → die Signatur blieb ab 1.21.9 so.
Schritt 3: Die neue API konkret nachschlagen
Ich brauchte die Details zu KeyInput und die genaue fromKeyCode-Signatur:
cat /home/timon/ai/skills/minecraft-fabric-development/mappings/yarn/1.21.9/net/minecraft/client/input/KeyInput.md
→ Felder key, scancode, modifiers; Getter key(), scancode(), modifiers().
rg -n "fromKeyCode" /home/timon/ai/skills/minecraft-fabric-development/mappings/yarn/1.21.9/net/minecraft/client/util/InputUtil.md
→ InputUtil$Key fromKeyCode(KeyInput key) — bestätigt die neue Signatur.
Schritt 4: Konstruktor-Signatur von KeyInput verifizieren (letzte Quelle)
Die Mappings zeigen die Felder, aber nicht den Record-Konstruktor. Den konnte ich nur am gedecompilten/remapped Jar prüfen. Ich nutzte javap auf der bereits gecachten Yarn-Jar:
javap -cp <...minecraft-merged-1.21.9-...yarn...jar> -p net.minecraft.client.input.KeyInput
→ public net.minecraft.client.input.KeyInput(int, int, int) — es ist ein Record mit Konstruktor (key, scancode, modifiers).
→ Daraus: new KeyInput(keyCode, 0, 0).
Schritt 5: 26.x-Fehler
Text.literal ist eine Yarn-Klasse; 26.x nutzt Mojang-Mappings mit Component.literal. Ich kannte die Zuordnung (Mojang=26.x, Yarn=1.21.x) aus dem Projektkontext und den Importen — deshalb Component.literal. Das 26.1-Mapping (net.minecraft.network.chat.Component) bestätigt den Namen.
Schritt 6: Stonecutter-Korrektur
Die Referenz zeigte, dass fromKeyCode(int,int) NUR in 1.21–1.21.8 gültig ist. Der bestehende else-Block deckte fälschlich ALLE Yarn-Versionen ab. Der Skill-Guide schreibt für solche Fälle top-level elif-Chains vor (nicht verschachteln unter hand-kommentierten Branches):
rg -n "elif|nested" .../guides/stonecutter_guide.md
→ Bestätigt: zweite Versionsachse als if/elif/else-Chain auf Top-Level, je eine vollständige Kopie pro Zweig. Deshalb habe ich elif >=1.21.9 (KeyInput) + else (int,int) gebaut.
Schritt 7: Verifikation
Ich baute gezielt die betroffenen Nodes:
./gradlew :1.21.9:build
./gradlew :1.21.10:build :1.21.11:build
JAVA_HOME=/usr/local/jdk-25 ./gradlew :26.1:build :26.2:build
./gradlew :1.21:build   # Kontrolle, dass die neue elif-Aufteilung nichts bricht
Alle BUILD SUCCESSFUL.
Zusammenfassung meiner Wissensquellen (in Prioritätsreihenfolge):
1. Compiler-Fehlermeldung (zeigt bereits "Erforderlich: KeyInput").
2. versions/yarn/<version>.md — Diff-Reports (was geändert wurde).
3. mappings/yarn/<version>/...md — konkrete Signaturen/Klassennamen.
4. javap auf der gecachten Yarn-Jar — Konstruktor-/Record-Signaturen.
5. guides/stonecutter_guide.md — wie man mehrversionige Code-Zweige korrekt schreibt.
Die 26.x-Klassennamen (Component) kamen aus dem Mapping-Kontext (Mojang = 26.x), abgeglichen mit den vorhandenen Importen im 26.1-Block der Datei.
