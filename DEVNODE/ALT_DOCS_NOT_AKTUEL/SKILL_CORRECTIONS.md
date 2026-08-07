# SKILL_CORRECTIONS

Sammlung von Abweichungen/Fehlern, die beim Arbeiten mit dem Skill
`minecraft-fabric-development` aufgefallen sind. Jeder Eintrag nennt das
Problem, warum es wichtig ist, und was korrigiert/ergänzt werden muss.
Nicht abschließend geklärte Punkte sind als offene Fragen markiert.

Projekt, in dem die Befunde gemacht wurden:
`/home/timon/devhub/Fabric-Package-Logger 1.21.4` (Stonecutter,
1.21–1.21.11 Yarn + 26.1/26.2 un-/Mojang, laut `build.gradle`).

---

## 1. Stonecutter: `//? if`-Blöcke INNERHALB einer `/* ... */`-auskommentierten Verzweigung brechen (Guide-Lücke)

**Wo:** `guides/stonecutter_guide.md`, Abschnitte C.1, C.3, C.5, C.7.
**Was falsch/unvollständig ist:** Der Guide suggeriert, inaktive
Verzweigungen werden nur "auskommentiert" und der Zustand bleibt
umkehrbar (C.1). Er zeigt Branching (C.5) nur auf EINER Ebene. Er warnt
NICHT explizit davor, einen `//? if X { ... //?} else { ... }`-Block in
eine Verzweigung zu legen, die selbst bereits mit `/* ... */`
auskommentiert ist. Java-Kommentare verschachteln NICHT: das innere `*/`
(aus `*///?}`) schließt den äußeren `/*`-Kommentar vorzeitig.

**Beobachtetes Versagen:**
- `screen/widget/ColorPickerWidget.java` und `screen/widget/ColorSelectorWidget.java`
- Quell-Truth = 26.1 (der `>=26.1`-Zweig ist live), der Yarn-else-Zweig
  ist komplett in `/* ... */` gepackt (`/*public class ColorPickerWidget
  extends ClickableWidget { ... *///?}`).
- Im auskommentierten Yarn-Zweig liegen INNERHALB weitere
  `//? if >=1.21.9 { ... //?} else { /* ... */ }`-Blöcke (z.B.
  `drawStrokedRectangle` vs `drawBorder`, `Click`-Signatur vs alte Signatur).
- Beim 26.1-Build hat Stein-Cutter diese inneren Marker VERARBEITET: das
  innere `*///?}` emittierte ein `*/`, das den äußeren `/*`-Kommentar des
  Yarn-Zweigs schloss. Ab dort wurde der Rest der Datei zu "lebendem"
  Code außerhalb jeder Klasse. → unvollständige Java-Syntax.
- Symptome im Build:
  - `class, interface, enum oder record erwartet` (ab ~Zeile 497),
  - `Anweisungen werden außerhalb von Methoden und Initializern nicht erwartet`,
  - `Kompakte Quelldatei darf keine Packagedeklaration aufweisen` (Zeile 1).

Bestätigt durch das Stonecutter-Projekt selbst (Codeberg): Issue #19
"Nested conditions sometimes produce artifacts", Issue #48 erwähnt
"commented-out nested directives". Java-`/* */`-Kommentare verschachteln
nicht, und Stonecutter verarbeitet Marker innerhalb von Kommentar-Zweigen
stellenweise weiter.

**Konkrete Regel, die in den Guide muss:** In INACTIVE-Bereichen
(`//? if X { ... } else { /* ... */ }`) dürfen KEINE weiteren
`//? if/elif/else` stehen. Eine zusätzliche Versionsachse gehört auf die
TOP-Ebene als eine vollständige `if/elif/else`-Kette — nie verschachteln.

**Fix (hier angewendet):** Die `>=1.21.9`-Unterscheidung aus der
`/* */`-Yarn-Verzweigung herausgezogen → beide Widgets als top-level
`//? if >=26.1 { ... } //?} elif >=1.21.9 { ... } //?} else { ... }`
umgeschrieben, mit drei vollständigen, voneinander unabhängigen
Klassenkörpern (keine Verschachtelung mehr).

---

## 2. Stonecutter verarbeitet `//?`-Marker INNERHALB von `/* */`-Kommentaren (widerspricht C.1)

**Wo:** `guides/stonecutter_guide.md`, C.1 ("Inactive branches are commented
out, never deleted").
**Was falsch ist:** C.1 impliziert, Marker in ausgeschlossenen Bereichen
seien passiv. Empirisch werden sie verarbeitet (Befund 1).
**Korrektur nötig:** In C.1/C.7 klarstellen: Kommentar-Zweige werden als
`/* */` ausgegeben, aber ihr Inhalt läuft vorher noch durch den
Präprozessor.

---

## 3. 26.x-Naming: Guide ist KORREKT, kein "Widerspruch"

**Wo:** Skill-Beschreibung + `build.gradle` B.6.2.
**Verifiziert (nicht falsch!):** Die 26.1-deobf-Jar enthält
`net/minecraft/client/Minecraft.class` (Mojang), KEIN `MinecraftClient.class`
(Yarn). 26.1 ist un-/Mojang-gemappt, kein mappings-`Dependency`, Loom-Plugin
`net.fabricmc.fabric-loom` (nicht `-remap`) — genau wie der Guide sagt.
**Warum `DualListSelectorWidget.java` (nur Yarn, kein `>=26.1`-Zweig) keinen
Fehler hat:** Die Datei hat keinen `>=26.1`-Zweig und ist für 26.1 KOMPLETT
inaktiv — reines `/* */`-Kommentarblock, wird für 26.1 nicht kompiliert
(= funktionsgleich leer). Nicht weil Yarn auf 26.1 läuft. Kein Guide-Fehler.
**Optionale Ergänzung (Empfehlung):** Ein Satz im Guide, der genau diese
Verwirrung klärt: "Eine pure Yarn-Datei OHNE `>=26.1`-Zweig ist für die
un-/Mojang-Nodes komplett inaktiv (leerer Kommentarblock) und wird für sie
nicht kompiliert."

---

## 4. "Kompakte Quelldatei darf keine Packagedeklaration ..." ist NUR Folge von Befund 1

**Was beobachtet wurde:** JEP-330-Source-File-Mode-Symptom in
`ColorPickerWidget.java`/`ColorSelectorWidget.java` Zeile 1.
**Kausalität:** Durch den kaputten Klassenrahmen sieht die Datei wie ein
Einzelquellen-Programm aus. Kein eigener Bug.
**Fix:** Nur Troubleshooting-Hinweis in D.4, der "Kompakte Quelldatei darf
keine Packagedeklaration aufweisen" als typisches Symptom verschachtelter
Conditional-Strukturen markiert. `kein` eigenständiger Fix nötig.