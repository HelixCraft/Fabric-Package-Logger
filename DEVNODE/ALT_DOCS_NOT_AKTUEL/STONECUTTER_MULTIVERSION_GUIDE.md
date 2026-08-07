# Stonecutter Multi-Version Setup — Vollständige Referenz

**Projekt:** Crystal per Second HUD (Fabric-Mod)
**Ziel:** 13 Minecraft-Versionen (`1.21` bis `26.1`) aus **einem** Quellcode-Baum bauen.
**Mappings-Hinweis:** Dieses Projekt nutzt Yarn-Mappings (1.21.x) und Mojang-Mappings (26.1).
Die **Stonecutter-Konfiguration ist von der Mapping-Wahl vollständig unabhängig** —
sie ist mit Yarn- *und* Mojang-Mappings identisch (Details in Abschnitt 6).
**Dieses Dokument ist eine exakte, 1:1-Wiedergabe der tatsächlichen Projektkonfiguration** —
wenn du ein normales Single-Version-Projekt nach diesem Leitfaden umbaust, erhältst du exakt
diese funktionierende Einrichtung.

---

## 1. Überblick & Architektur

Stonecutter (`dev.kikugie.stonecutter`, Version **0.9.6**) verwaltet ein **Multi-Project-Gradle-Setup**:

- **Tree (der Baum)** = das Root-Projekt. Es hält den **geteilten Quellcode** in `src/`
  (Java + Ressourcen) und die gemeinsame Build-Konfiguration.
- **Node (der Knoten)** = eine Minecraft-Version, registriert als Gradle-**Subproject**
  unter `versions/<version>/`. Jeder Node erhält beim Bauen seinen **eigenen, verarbeiteten
  Quellcode** (generiert aus `src/`) und sein eigenes Jar.
- **Aktive Version** = die Version, deren Zustand aktuell in `src/` liegt. Wird die aktive
  Version gewechselt, **schreibt Stonecutter die Preprocessor-Kommentare in `src/` neu**
  (Blöcke werden ein-/auskommentiert).

Jeder Node hat zwei Eigenschaften:
1. **Projektname** — der Ordnername unter `versions/` (hier identisch mit der Version, z. B. `1.21.4`)
2. **Zielversion** — der String, gegen den die Bedingungen im Code geprüft werden
   (`sc.current.version`), hier ebenfalls `1.21.4`.

Die zentrale Idee: Du schreibst Code **einmal** in `src/` und markierst versionabhängige
Stellen mit speziellen Kommentaren (`//? if <version> { ... //?}`). Stonecutter erzeugt daraus
pro Version einen korrekt aufgelösten Quellcode.

```
Projekt-Root (Tree)
├── src/                          ← GETeilter Quellcode (wird editiert)
│   ├── main/java/...             ← Java mit //?-Kommentaren
│   └── main/resources/...        ← geteilt, inkl. fabric.mod.json mit Platzhaltern
├── build.gradle                  ← Zentrales Build-Skript (läuft PRO Node einmal)
├── settings.gradle               ← Plugin + Tree/Node-Registrierung
├── stonecutter.gradle            ← Controller: aktive Version
├── gradle.properties             ← Gemeinsame Properties
└── versions/
    ├── 1.21/gradle.properties    ← Pro-Node Dependency-Versionen
    ├── 1.21.4/gradle.properties
    └── ... (13 Nodes)
```

---

## 2. Projektstruktur (Baum) — Datei für Datei

### 2.1 Root-Verzeichnis (im Git)

| Pfad | Zweck |
| --- | --- |
| `settings.gradle` | Stonecutter-Plugin + Node-Liste + Controller-Konfig |
| `stonecutter.gradle` | Controller-Skript: legt die **aktive Version** fest (auto-generiert) |
| `build.gradle` | Zentrales Build-Skript, wird pro Node ausgeführt |
| `gradle.properties` | Gemeinsame Eigenschaften (Mod-Version, Loom, Loader, `hard_mode`) |
| `gradle/wrapper/gradle-wrapper.properties` | Gradle-Version (9.5.1) |
| `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar` | Gradle Wrapper |
| `src/main/java/` | Geteilter Java-Quellcode mit `//?`-Preprocessor-Markierungen |
| `src/main/resources/` | Geteilte Ressourcen (`fabric.mod.json`, Mixin-Config, Lang, Icon) |
| `versions/<version>/gradle.properties` | Pro-Node Dependency-Versionen (Mappings, Fabric API, ModMenu) |
| `.gitignore` | Ignoriert `build/`, `run/`, `bin/`, `.gradle/`, Cache usw. |
| `.github/workflows/build.yml` | CI: baut mit Java 25 alle 13 Nodes |
| `LICENSE`, `README.md`, `SKILL_CORRECTIONS.md` | Doku/Meta |

### 2.2 Verzeichnisse, die NICHT im Git sind (generiert)

| Pfad | Zweck |
| --- | --- |
| `versions/<v>/build/generated/stonecutter/main/java` | **Verarbeiteter** Java-Quellcode dieses Nodes (von `stonecutterGenerate` erzeugt) |
| `versions/<v>/build/generated/stonecutter/main/resources` | Verarbeitete Ressourcen (hier praktisch unverändert) |
| `versions/<v>/build/libs/` | Die fertigen Jars dieses Nodes |
| `versions/<v>/run/` | Run-Verzeichnis dieses Nodes (Standard; dieses Projekt teilt `run/` aber, s. u.) |
| `run/` | **Geteiltes** Run-Verzeichnis aller Nodes (in `loom {}` konfiguriert) |
| `bin/`, `.gradle/`, `.vscode/`, `.qodo/` | Diverse lokale Artefakte |

---

## 3. Die Konfigurationsdateien im Detail

### 3.1 `settings.gradle` — Plugin laden & Nodes registrieren

```groovy
pluginManagement {
    repositories {
        maven {
            name = 'Fabric'
            url = 'https://maven.fabricmc.net/'
        }
        maven {
            name = 'KikuGie Snapshots'
            url = 'https://maven.kikugie.dev/snapshots'
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id 'dev.kikugie.stonecutter' version '0.9.6'
}

stonecutter {
    kotlinController = false
    centralScript = 'build.gradle'

    create(rootProject) {
        versions(
            '1.21', '1.21.1', '1.21.2', '1.21.3',
            '1.21.4', '1.21.5', '1.21.6', '1.21.7',
            '1.21.8', '1.21.9', '1.21.10', '1.21.11',
            '26.1'
        )
        vcsVersion = '1.21.4'
    }
}

rootProject.name = 'crystal-per-second-hud'
```

**Die entscheidenden Punkte:**

- **Plugin-Version:** `dev.kikugie.stonecutter` **`0.9.6`** (auch nach 2026 regelmäßig neu
  released — bei einem Neubau gegen das aktuelle Release abgleichen).
- **Repositories (pluginManagement):** Für die Plugin-Auflösung sind **vier** Repos nötig:
  - `https://maven.fabricmc.net/` (Fabric)
  - `https://maven.kikugie.dev/snapshots` (KikuGie Snapshots — **Stonecutter wird hieraus geladen**)
  - `mavenCentral()`
  - `gradlePluginPortal()`
- **`kotlinController = false`:** Zwingend, weil der Controller (`stonecutter.gradle`) und das
  zentrale Skript (`build.gradle`) in **Groovy DSL** geschrieben sind. Standard wäre Kotlin.
- **`centralScript = 'build.gradle'`:** Sagt Stonecutter, dass das gemeinsame Build-Skript
  `build.gradle` heißt (statt `build.gradle.kts`).
- **`create(rootProject) { versions(...) }`:** Registriert alle **13 Nodes**. Wichtig:
  `versions(...)` nimmt nur eine **explizite Liste** — keine Range-Angabe wie `'1.21-26.1'`.
  Jede Version muss einzeln stehen.
- **`vcsVersion = '1.21.4'`:** Der „Reset-Punkt“. Vor jedem Git-Commit sollte `src/` auf den
  Zustand dieser Version zurückgesetzt werden, damit nicht lauter Preprocessor-Diff-Noise
  committet wird (Task „Reset active project“).
- **`rootProject.name`:** Muss als Letztes stehen und darf den Mod-Namen tragen.

> Soll ein Node einen anderen Ordnernamen als die Version tragen (z. B. `1.21.11-fabric`),
> nutzt man stattdessen `version('<ordnername>', '<zielversion>')` einzeln pro Eintrag.

### 3.2 `stonecutter.gradle` — der Controller (aktive Version)

```groovy
plugins {
    id "dev.kikugie.stonecutter"
}
stonecutter.active "1.21.4"
```

- Diese Datei wird von Stonecutter **automatisch erzeugt und gepflegt** (nach dem ersten Sync).
- `stonecutter.active "1.21.4"` legt die **aktuell aktive Version** fest.
- **Nicht von Hand editieren.** Die Version wird über den Gradle-Task
  „Set active project to …“ gewechselt, der diese Zeile sauber neu schreibt und zugleich alle
  Preprocessor-Kommentare in `src/` für die neue Version verarbeitet.
- Achtung: In der Kotlin-Variante (Standard) würde hier `/* [SC] DO NOT EDIT */` stehen —
  in dieser Groovy-Konfiguration nicht.

### 3.3 `gradle.properties` — gemeinsame Properties (Root)

```properties
# Shared properties for all versions
org.gradle.jvmargs=-Xmx1G
org.gradle.parallel=true
org.gradle.configuration-cache=false

# Mod Properties
mod_version=1.0.0
maven_group=com.helixcraft.cpsh

# Fabric Properties
loader_version=0.19.3
loom_version=1.17-SNAPSHOT
dev.kikugie.stonecutter.hard_mode=true
```

| Property | Wert | Bedeutung |
| --- | --- | --- |
| `mod_version` | `1.0.0` | Mod-Version (geht in Jarname und `fabric.mod.json`) |
| `maven_group` | `com.helixcraft.cpsh` | Maven-Gruppe |
| `loader_version` | `0.19.3` | Fabric Loader (für alle Nodes gleich) |
| `loom_version` | `1.17-SNAPSHOT` | Fabric Loom — im `buildscript`-Classpath (s. u.) |
| `dev.kikugie.stonecutter.hard_mode` | `true` | Stonecutter-Hard-Mode: versucht, auch scheinbar „kaputte“ Preprocessor-Zustände zu verarbeiten. **Diese Property muss hier auf Root-Ebene stehen**, damit sie vor dem Laden des Controllers verfügbar ist. |

### 3.4 `versions/<version>/gradle.properties` — Pro-Node Dependency-Versionen

Hier stehen die **versionabhängigen Abhängigkeiten**. Welche das sind, hängt von deiner
**Mappings-Wahl** ab — nicht von Stonecutter. Dieses Projekt verwendet Yarn für die
1.21.x-Nodes und für 26.1 (unobfuskiert) gar keine Mappings:

Beispiel `versions/1.21.4/gradle.properties` (Yarn-Node):

```properties
deps.yarn_mappings = 1.21.4+build.8
deps.fabric_api = 0.119.4+1.21.4

deps.modmenu=13.0.4
```

Beispiel `versions/26.1/gradle.properties` (**ohne** Mappings — 26.1 ist nicht mehr obfuskiert,
es gibt kein Yarn):

```properties
deps.fabric_api = 0.145.1+26.1

deps.modmenu=18.0.0-alpha.8
```

- Jeder Node braucht **seine eigene** solche Datei. Stonecutter liest zwei Ebenen:
  1. `./gradle.properties` (Root) — gemeinsame Werte
  2. `./versions/<projekt>/gradle.properties` — node-spezifische Werte
- Zugriff im Build-Skript mit `property('<name>')` (wirft, wenn nicht vorhanden) bzw.
  `findProperty('<name>')` (liefert `null`). In Groovy-DSL auf `String` casten.
- Der **Property-Name ist frei wählbar** — `deps.yarn_mappings` ist nur die Konvention dieses
  Projekts. Benutzt du z. B. Mojang-Mappings für eine obfuskierte Version (1.21.x mit Mojmap),
  trägst du dort den Mojmap-Koordinaten-String in eine eigene Property ein (z. B.
  `deps.mojmap`) und referenzierst sie in der `mappings`-Dependency in 3.5.
- Für 26.1 kann die Mappings-Property **fehlen**, weil der Build-Zweig für `>=26.1` keine
  `mappings`-Dependency deklariert (siehe 3.5).
- Diese Dateien **gehören ins Git** (sind die Quelle für Dependency-Auflösung).

### 3.5 `build.gradle` — das zentrale Build-Skript

Dieses Skript läuft **einmal pro Node** (jeder Node ist eine eigene Gradle-Build-Instanz mit
seinen eigenen Properties). Es ist der komplexeste Teil.

```groovy
buildscript {
    repositories {
        maven {
            name = 'Fabric'
            url = 'https://maven.fabricmc.net/'
        }
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath "net.fabricmc:fabric-loom:${property('loom_version')}"
    }
}

plugins {
    id 'maven-publish'
}

if (sc.current.parsed >= "26.1") {
    apply plugin: 'net.fabricmc.fabric-loom'
} else {
    apply plugin: 'net.fabricmc.fabric-loom-remap'
}

loom {
    runs {
        client {
            runDir = "${rootProject.projectDir}/run"
            programArg "--username=dev"
        }
        server {
            runDir = "${rootProject.projectDir}/run"
        }
    }
}

version = "${property('mod_version')}+mc${sc.current.version}"
group = property('maven_group') as String
base.archivesName = 'crystal-per-second-hud'

repositories {
    maven { name = "Terraformers"; url = "https://maven.terraformersmc.com/" }
}

sourceSets {
    main {
        java {
            setSrcDirs(["${layout.buildDirectory.get()}/generated/stonecutter/main/java"])
        }
        resources {
            setSrcDirs(["${rootProject.projectDir}/src/main/resources"])
        }
    }
}

tasks.named('compileJava') {
    dependsOn tasks.named('stonecutterGenerate')
}



def loaderDep = property('loader_version') as String
def apiDep = property('deps.fabric_api') as String

dependencies {
    minecraft "com.mojang:minecraft:${sc.current.version}"

    if (sc.current.parsed >= "26.1") {
        implementation "net.fabricmc:fabric-loader:${loaderDep}"
        implementation "net.fabricmc.fabric-api:fabric-api:${apiDep}"
    } else {
        mappings "net.fabricmc:yarn:${property('deps.yarn_mappings')}:v2"
        modImplementation "net.fabricmc:fabric-loader:${loaderDep}"
        modImplementation "net.fabricmc.fabric-api:fabric-api:${apiDep}"
    }

    if (sc.current.parsed >= "26.1") {
        compileOnly "com.terraformersmc:modmenu:${property('deps.modmenu')}"
    } else {
        modCompileOnly "com.terraformersmc:modmenu:${property('deps.modmenu')}"
    }
}

def javaVersion = sc.current.parsed >= "26.1" ? 25 : 21

processResources {
    def ver = project.version
    def mcVer = sc.current.version
    def javaReq = sc.current.parsed >= "26.1" ? ">=25" : ">=21"
    inputs.property "version", ver
    inputs.property "minecraft_version", mcVer
    inputs.property "java_version", javaReq
    filesMatching("fabric.mod.json") {
        expand "version": ver, "minecraft_version": mcVer, "java_version": javaReq
    }
}

tasks.withType(JavaCompile).configureEach {
    it.options.release = javaVersion
}

java {
    withSourcesJar()
}

tasks.matching { it.name == 'sourcesJar' }.configureEach {
    dependsOn tasks.named('stonecutterGenerate')
}

jar {
    inputs.property "projectName", project.name
    from("LICENSE") {
        rename { "${it}_${project.name}" }
    }
}

publishing {
    publications {
        create("mavenJava", MavenPublication) {
            from components.java
        }
    }
}
```

**Warum die einzelnen Teile nötig sind:**

1. **`buildscript { }` + `classpath "net.fabricmc:fabric-loom:..."`** — zwingend, weil Loom
   **bedingt** per `apply plugin:` angewendet wird (siehe Punkt 2). Ein `plugins {}`-Block kann
   **keine** Groovy-Bedingung auf die Plugin-ID anwenden; `apply plugin:` wiederum findet das
   Plugin nur, wenn es auf dem Buildscript-Classpath liegt. Beide Plugin-IDs
   (`net.fabricmc.fabric-loom` **und** `net.fabricmc.fabric-loom-remap`) liegen im **selben**
   Loom-Jar. Repositories hier: `maven.fabricmc.net`, `mavenCentral`, `gradlePluginPortal`.
   **Hinweis:** Die Repos hier sind nur für den *Plugin-Classpath*. Die *projektweiten*
   Repositories (z. B. Terraformers für ModMenu) stehen separat in `repositories { }` weiter
   unten — nicht in `publishing.repositories` (dort würde es für die Dependency-Auflösung
   ignoriert).

2. **Bedingte Loom-Anwendung — Mapping-abhängig, nicht Stonecutter-abhängig:**
   ```groovy
   if (sc.current.parsed >= "26.1") {
       apply plugin: 'net.fabricmc.fabric-loom'
   } else {
       apply plugin: 'net.fabricmc.fabric-loom-remap'
   }
   ```
   - **Obfuskierte Versionen** (hier 1.21.0 – 1.21.11, egal ob Yarn- oder Mojang-Mappings):
     `net.fabricmc.fabric-loom-remap` (Loom remappt die obfuskierte Jar auf deine Mappings)
   - **Unobfuskierte Versionen** (hier 26.1+, Klassen kommen bereits mit Mojang-Namen):
     `net.fabricmc.fabric-loom` (kein Remap nötig)
   Die Grenze `26.1` ist eine **Eigenschaft dieses Projekts** — bei einem reinen Yarn- *oder*
   reinen Mojang-Projekt entfällt diese Fallunterscheidung im Build-Skript komplett.
   Loom 1.17 kündigte `modImplementation`/`modCompileOnly` für die neuen Versionen — deshalb
   die entsprechenden Zweige in den `dependencies`.

3. **`sc` ist die Stonecutter-Extension**, die vom Plugin im zentralen Skript automatisch
   als Alias bereitgestellt wird:
   - `sc.current.version: String` — die Versions-Ziel-String des aktuellen Nodes (z. B. `1.21.4`)
   - `sc.current.parsed: ParsedVersion` — Vergleichsobjekt, erlaubt
     `sc.current.parsed >= "26.1"` (semantischer Versionsvergleich)
   - `sc.current.project: String` — der Ordnername des Nodes

4. **Geteiltes Run-Verzeichnis** (`loom { runs { client/server { runDir = .../run } } }`):
   Ohne das hätte jeder Node ein eigenes `versions/<v>/run/` und Videoeinstellungen/Keybinds
   würden bei jedem Versionswechsel zurücksetzen. Alle Nodes teilen sich `<root>/run/`.
   **Caveat:** Auch Saves werden geteilt — Welten, die von einer neueren Version gespeichert
   wurden, können in älteren korrupt erscheinen (allgemeine Minecraft-Limitierung).

5. **Jar-Namensschema:** `version = "<mod_version>+mc<mc_version>"` → z. B.
   `crystal-per-second-hud-1.0.0+mc1.21.4.jar`. Das `mc`-Präfix unterscheidet die Jars der
   Nodes eindeutig.

6. **`sourceSets`** — der zentrale Schaltpunkt:
   ```groovy
   sourceSets {
       main {
           java { setSrcDirs(["${layout.buildDirectory.get()}/generated/stonecutter/main/java"]) }
           resources { setSrcDirs(["${rootProject.projectDir}/src/main/resources"]) }
       }
   }
   ```
   - Java wird aus dem **Stonecutter-generierten** Verzeichnis kompiliert
     (`versions/<v>/build/generated/stonecutter/main/java`), nicht aus `src/`.
   - Ressourcen kommen direkt aus dem **geteilten** `src/main/resources` des Roots.
   - Normalerweise verdrahtet Stonecutter die Source-Sets selbst; diese explizite
     Umleitung ist nötig, weil Loom die Source-Sets neu registriert, nachdem Stonecutter
     seine Verarbeitung eingerichtet hat.

7. **`compileJava` hängt von `stonecutterGenerate` ab** — stellt sicher, dass der
   verarbeitete Quellcode erzeugt wird, bevor kompiliert wird. Genauso für `sourcesJar`.

8. **Dependencies pro Zweig:**
   - `minecraft "com.mojang:minecraft:${sc.current.version}"` — immer
   - Obfuskiert (hier `<26.1`): `mappings <deine-mappings>` + `modImplementation` für
     Loader/API. Bei Yarn: `mappings "net.fabricmc:yarn:${property('deps.yarn_mappings')}:v2"`;
     bei Mojang-Mappings für eine obfuskierte Version stattdessen z. B. einen Mojmap-Loader
     als `mappings`-Dependency.
   - Unobfuskiert (hier `>=26.1`): kein `mappings`, stattdessen `implementation`
   - ModMenu: obfuskiert → `modCompileOnly`; unobfuskiert → `compileOnly` (Loom 1.17 hat
     `modCompileOnly` für 26.1 abgeschafft)

9. **Java-Version:** `release = 25` für 26.1, sonst `21`. Dazu passt im `processResources`
   die `java_version`-Anforderung (`>=25` bzw. `>=21`) für `fabric.mod.json`.

10. **`processResources` + `expand`:** Füllt die Platzhalter in
    `src/main/resources/fabric.mod.json` pro Node ein: `version`, `minecraft_version`
    (`sc.current.version`), `java_version`. Nur `fabric.mod.json` wird matchen via
    `filesMatching(...)`.

11. **`jar { from("LICENSE") ... }`** — bindet die Lizenzdatei (umbenannt mit Node-Namen)
    ins Jar ein. **Achtung:** `LICENSE` muss im Root liegen und der Pfad bezieht sich auf den
    Node — daher `inputs.property "projectName", project.name` als Invalidation.

### 3.6 `gradle/wrapper/gradle-wrapper.properties` — Gradle-Version

```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-9.5.1-bin.zip
networkTimeout=10000
retries=0
retryBackOffMs=500
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

- **Gradle 9.5.1** (nötig für Loom 1.17 und den 26.1-Zweig).
- `gradle-wrapper.jar` und die `gradlew`-Skripte sind Standard (Apache-Lizenz-Banner).

### 3.7 `.gitignore`

```gitignore
# gradle
.gradle/
build/
out/
classes/

# eclipse
*.launch

# idea
.idea/
*.iml
*.ipr
*.iws

# vscode
.settings/
.vscode/
bin/
.classpath
.project

# macos
*.DS_Store

# fabric
run/

# java
hs_err_*.log
replay_*.log
*.hprof
*.jfr
*.zip
.stonecutter-report.md
```

Wichtig: `build/`, `run/`, `bin/` global ignoriert — dadurch sind alle generierten
Node-Artefakte (`versions/<v>/build/`, `versions/<v>/run/`, `versions/<v>/bin/`) automatisch
ausgeschlossen, während die `versions/<v>/gradle.properties` committet bleiben.

### 3.8 `.github/workflows/build.yml` — CI

```yaml
name: build
on: [pull_request, push]

jobs:
  build:
    runs-on: ubuntu-24.04
    steps:
      - name: checkout repository
        uses: actions/checkout@v6
      - name: validate gradle wrapper
        uses: gradle/actions/wrapper-validation@v6
      - name: setup jdk
        uses: actions/setup-java@v5
        with:
          java-version: '25'
          distribution: 'microsoft'
      - name: make gradle wrapper executable
        run: chmod +x ./gradlew
      - name: build
        run: ./gradlew build
      - name: capture build artifacts
        uses: actions/upload-artifact@v7
        with:
          name: Artifacts
          path: build/libs/
```

- **Java 25** wird benötigt, weil der 26.1-Node mit `--release 25` kompiliert (ein JDK 21
  kann das nicht). Mit Java 25 baut man auch alle 1.21-Nodes sauber (`--release 21`).
- `./gradlew build` vom Root aus baut **alle 13 Nodes** (Gradle führt den Task im Root und in
  allen Subprojekten aus).

---

## 4. Versionstabelle — alle 13 Nodes (Ist-Zustand dieses Projekts)

Mappings-Spalte: Dieses Projekt nutzt **Yarn** (1.21.x) und **keine Mappings** (26.1,
unobfuskiert). In einem Projekt mit anderen Mappings stünde hier einfach der entsprechende
Koordinations-String.

| Node | Mappings (`deps.yarn_mappings` hier) | `deps.fabric_api` | `deps.modmenu` |
| --- | --- | --- | --- |
| `1.21` | `yarn 1.21+build.9` | `0.102.0+1.21` | `11.0.4` |
| `1.21.1` | `yarn 1.21.1+build.3` | `0.116.12+1.21.1` | `11.0.4` |
| `1.21.2` | `yarn 1.21.2+build.1` | `0.106.1+1.21.2` | `12.0.1` |
| `1.21.3` | `yarn 1.21.3+build.2` | `0.114.1+1.21.3` | `12.0.1` |
| `1.21.4` | `yarn 1.21.4+build.8` | `0.119.4+1.21.4` | `13.0.4` |
| `1.21.5` | `yarn 1.21.5+build.1` | `0.128.2+1.21.5` | `14.0.2` |
| `1.21.6` | `yarn 1.21.6+build.1` | `0.128.2+1.21.6` | `15.0.2` |
| `1.21.7` | `yarn 1.21.7+build.8` | `0.129.0+1.21.7` | `15.0.2` |
| `1.21.8` | `yarn 1.21.8+build.1` | `0.136.1+1.21.8` | `15.0.2` |
| `1.21.9` | `yarn 1.21.9+build.1` | `0.134.1+1.21.9` | `16.0.1` |
| `1.21.10` | `yarn 1.21.10+build.3` | `0.138.4+1.21.10` | `16.0.1` |
| `1.21.11` | `yarn 1.21.11+build.6` | `0.141.4+1.21.11` | `17.0.0` |
| `26.1` | *(keine — unobfuskiert)* | `0.145.1+26.1` | `18.0.0-alpha.8` |

**Merkregel bei neuen Versionen:** Mappings-Build-Nummern (z. B. Yarn) und
Fabric-API-Versionen niemals raten — exakt für die Zielversion recherchieren
(Fabric-Maven-Metadaten für Yarn, GitHub-Releases von FabricMC/fabric-api für die API, wobei
alte Minor-Versionen weit hinten liegen können, z. B. 1.21.2 auf Seite 4 der GitHub-API).

---

## 5. Stonecutter-Syntax im Quellcode

### 5.1 Grundprinzip

Bedingungen leben **in Code-Kommentaren**. Damit „sieht“ der Compiler/IDE immer gültigen
Code oder gültige Kommentare, während Stonecutter beim Verarbeiten umschaltet, welcher Zweig
aktiv (echter Code) und welcher inaktiv (Kommentar) ist. Inaktive Zweige werden **nicht
gelöscht**, sondern mit `/*`/`*/` auskommentiert — dadurch bleibt der Code reversibel.

### 5.2 Kommentar-Formate pro Dateityp

| Dateityp | Öffnen | Schließen |
| --- | --- | --- |
| Java, Scala, JSON5 | `//?` und `/*` `*/` | `//?}` bzw. `*///?}` |
| Kotlin | `//?`, verschachtelte `/* */` | dito |
| YAML, Properties, Access Widener/Transformer | `#?` | `#?}` |

### 5.3 Closed Scope (mehrzeilig, `{ }`) — der wichtigste Baustein

```java
    //? if >=26.1 {
    import net.minecraft.client.Minecraft;
    import net.minecraft.client.gui.GuiGraphics;
    //?} else {
    /*import net.minecraft.client.MinecraftClient;
    import net.minecraft.client.gui.DrawContext;
    *///?}
```

- `//? if >=26.1 {` öffnet einen geschlossenen Block (das `{` gehört zur Markierung).
- Steht die Bedingung **nicht** an, setzt Stonecutter `/*` an den Anfang des Blockinhalts und
  `*/` direkt vor `//?}`. Steht sie an, entfernt es diese Marker wieder.
- **Echtes Beispiel aus `CrystalHUD.java`** (Zustand bei aktiver Version **1.21.4**):

  ```java
      //? if >=26.1 {
      /*import net.minecraft.client.Minecraft;
      import net.minecraft.client.gui.GuiGraphics;
      *///?} else {
      import net.minecraft.client.MinecraftClient;
      import net.minecraft.client.gui.DrawContext;
      //?}
  ```

  Der `>=26.1`-Zweig ist hier inaktiv und eingerückt auskommentiert; der `else`-Zweig ist
  aktiver Code.

### 5.4 Line Scope (einzeilig, ohne `{ }`)

Betrifft nur die **nächste nicht-leere Zeile** — für kleine Unterschiede wie ein einzelnes
Argument:

```java
    //? if <26.1 {
    import net.minecraft.client.render.RenderTickCounter;
    //?}
```

Hier wird bei 26.1+ der Import entfernt (auskommentiert).

### 5.5 Bedingungs-Ketten: `if` / `elif` / `else`

```java
    //? if >=26.1 {
    public void render(GuiGraphics drawContext, float tickCounter) {
    //?} elif >=1.21 {
    /*public void render(DrawContext drawContext, RenderTickCounter tickCounter) {
    *///?} else {
    /*public void render(DrawContext drawContext, float tickCounter) {
    *///?}
```

- **`//?} elif <bedingung> {`** setzt eine Kette fort, **`//?} else {`** schließt sie ab.
- **Regel:** Nur der **letzte** Zweig einer Kette darf ohne eigene `{ }` offen bleiben.
  Alles davor muss mit `//?}` bzw. `//?} else {` geschlossen sein. Falsch:
  ```java
  //? if <1.21
  ...            // ← darf nur als LETZTER Zweig ohne braces stehen
  //? else ...
  ```
  Richtig:
  ```java
  //? if <1.21 {
  ...
  //?} else
  ...
  ```
- `elif`, `else if` und `else` sind in Chains erlaubt (das Projekt nutzt `elif`).

### 5.6 Vergleichsoperatoren

| Operator | Beispiel | Bedeutung |
| --- | --- | --- |
| `>=` | `if >=26.1` | Version ist 26.1 oder neuer |
| `>` | `if >1.21.11` | neuer als 1.21.11 |
| `<` | `if <26.1` | älter als 26.1 |
| `<=` | `if <=1.21.11` | 1.21.11 oder älter |
| `==` | `if ==1.21.4` | exakt 1.21.4 |

Verglichen wird **semantisch** (vollqualifizierte Versionen). Wichtig: `>=1.21` matcht auch
`26.1` (26.1 > 1.21). Deshalb müssen in `elif`-Ketten **die spezifischeren/neueren
Bedingungen zuerst** stehen. Genau das macht der Code:

```java
//? if >=26.1 {      // 1. Prüfung: 26.1+ (sonst würde 26.1 in den >=1.21-Zweig fallen)
//?} elif >=1.21.6 {
//?} else {
```

### 5.7 Regeln & Fallstricke (aus diesem Projekt gelernt)

1. **Ganze Konstrukte in einem Zweig lassen.** Ein `{ }`-Block sollte ganze Methoden,
   Konstruktoren, Felder oder Importgruppen umfassen. Niemals eine einzelne Anweisung
   „halbiert“ über `//?`-Marker teilen (z. B. Signatur conditional, Body geteilt) — das ist
   unzuverlässig.
2. **Constructor-Signatur:** Wenn der Super-Konstruktor versionabhängige Klassen nutzt
   (hier `Component.literal` vs. `Text.literal`), muss der **gesamte Konstruktor** (inkl.
   `super(...)` und Body) in einem Zweig stehen. Die **Klassendeklaration** bleibt außerhalb.
   ```java
   //? if >=26.1 {
   public HUDEditorScreen(CrystalHUD hud, CrystalCounter counter) {
       super(Component.literal("Crystal per Second HUD Editor"));
       ...
   }
   //?} else {
   /*protected HUDEditorScreen(CrystalHUD hud, CrystalCounter counter) {
       super(Text.literal("Crystal per Second HUD Editor"));
       ...
   }
   *///?}
   ```
3. **`@Override`-Annotation:** Inline-Conditionals *innerhalb* einer Zeile
   (z. B. `public /*? if <26.1 {*/@Override/*?}*/ ...`) funktionieren nicht sauber.
   Lösung: `@Override` einfach immer setzen — die Mehrdeutigkeit zwischen
   `mouseClicked`/`mouseDragged`/`mouseReleased` wird über die **Methodensignaturen** im
   Conditional gelöst, nicht über die Annotation.
4. **Vergleichs-Reihenfolge:** breite Bedingungen zuerst abgrenzen (siehe 5.6).
5. **Nicht das erste/last `{` falsch setzen:** Nach `//? if X {` folgt ab der **nächsten
   Zeile** der Blockinhalt; das schließende `//?}` steht allein auf einer eigenen Zeile.
   Inaktiver Inhalt sieht dann so aus: `/*...*/` direkt vor `//?}` bzw. `//?} else {`.
6. **IDE-Sicht:** Im Editor sieht man immer nur den Zustand der **aktuell aktiven** Version.
   Nach einem Versionswechsel (Task) wird `src/` umgeschrieben. Vor dem Committen auf
   `vcsVersion` zurücksetzen, sonst verschmutzt der Diff mit Preprocessor-Markierungen.

### 5.8 Verwendete Bedinungsklassen im Projekt (Zusammenfassung)

- **Mapping-Wechsel (Mojang vs. Yarn — in diesem Projekt bei 26.1):** `if >=26.1` /
  `elif >=1.21.x` / `else`
- **1.21.9-Unterscheidung (KeyBinding-API):** `if >=26.1` / `elif >=1.21.9` / `else`
- **1.21.6-Unterscheidung (MatrixStack-API):** `if >=26.1` / `elif >=1.21.6` / `else`
- **1.21-Unterscheidung (RenderTickCounter / event-API):** `if >=26.1` / `elif >=1.21` / `else`

Betroffene Dateien:
`CrystalHUD.java`, `CrystalPerSecondHUD.java`, `HUDConfig.java`, `HUDKeyBinding.java`,
`HUDEditorScreen.java`, `mixin/InGameHudMixin.java`, `mixin/CrystalDestructionMixin.java`.

---

## 6. Mappings-Unterscheidung: Yarn ↔ Mojang (unabhängig von Stonecutter)

**Wichtig vorweg:** Welche Mappings du benutzt (Yarn oder Mojang), ändert **nichts** an der
Stonecutter-Konfiguration — `settings.gradle`, `stonecutter.gradle`, Tasks und Preprocessor
bleiben identisch. Die Mapping-Wahl wirkt sich nur aus auf (a) den **Code** (Klassennamen)
und (b) das **Build-Skript** (Loom-Plugin, `mappings`-Dependency, Dependency-Configs).
Stonecutter selbst ist mapping-agnostisch.

Dieses Projekt nutzt ab Version **26.1** die Un-Obfuskierung von Minecraft aus: Die Klassen
kommen direkt mit Mojang-Namen (kein Yarn mehr nötig). Deshalb steckt die Yarn→Mojang-Grenze
im Build und im Code hinter `>=26.1`-Bedingungen:

| Aspekt | Obfuskiert (hier 1.21.x, Yarn) | Unobfuskiert (26.1+, Mojang) |
| --- | --- | --- |
| Loom-Plugin | `net.fabricmc.fabric-loom-remap` | `net.fabricmc.fabric-loom` |
| `mappings`-Dependency | Yarn hier; beliebige andere Mappings möglich | **keine** (Klassen bereits benannt) |
| Loader/API-Config | `modImplementation` | `implementation` |
| ModMenu | `modCompileOnly` | `compileOnly` |
| Java-Version (`--release`) | 21 | 25 |
| `fabric.mod.json` `java` | `>=21` | `>=25` |

Entsprechende Yarn→Mojang-Umbenennungen, die im Code hinter `if >=26.1` stecken:

| Yarn (≤1.21.11) | Mojang (26.1+) |
| --- | --- |
| `net.minecraft.client.MinecraftClient` | `net.minecraft.client.Minecraft` |
| `net.minecraft.client.gui.DrawContext` | `net.minecraft.client.gui.GuiGraphics` |
| `net.minecraft.client.gui.hud.InGameHud` | `net.minecraft.client.gui.Gui` |
| `net.minecraft.client.render.RenderTickCounter` | `net.minecraft.client.DeltaTracker` |
| `net.minecraft.client.font.TextRenderer` | `net.minecraft.client.gui.Font` |
| `net.minecraft.text.Text` | `net.minecraft.network.chat.Component` |
| `net.minecraft.client.gui.screen.Screen` | `net.minecraft.client.gui.screens.Screen` |
| `net.minecraft.client.option.KeyBinding` | `net.minecraft.client.KeyMapping` |
| `net.minecraft.client.util.InputUtil` | `net.minecraft.client.InputConstants` |
| `net.minecraft.entity.decoration.EndCrystalEntity` | `net.minecraft.world.entity.boss.enderdragon.EndCrystal` |
| `net.minecraft.entity.player.PlayerEntity` | `net.minecraft.world.entity.player.Player` |
| `net.minecraft.util.Identifier` | `net.minecraft.util.Identifier` (unverändert) |

Methoden/Fields: `drawText→drawString`, `getMatrices→pose`, `push→pushPose`, `pop→popPose`,
`textRenderer.getWidth→font.width`, `fontHeight→lineHeight`, `currentScreen→screen`,
`world→level`, `hudHidden→hideGui`, `isOnThread→isSameThread`,
`getScaledWidth→getGuiScaledWidth`. (Teilweise aus externen Quellen recherchiert — beim
Portieren auf eine konkrete 26.1-API immer gegen die tatsächliche 26.1-Signatur verifizieren,
z. B. per `./gradlew :26.1:genSources`.)

---

## 7. Aufgaben (Tasks) & Arbeitsablauf

### 7.1 Alle Stonecutter-Tasks anzeigen

```bash
./gradlew tasks --group stonecutter
```

### 7.2 Aktive Version wechseln

```bash
./gradlew "Set active project to 1.21.4"
```

- Es gibt **einen solchen Task pro Version** (z. B. `Set active project to 26.1`,
  `Set active project to 1.21`). Die Namen enthalten Leerzeichen — deshalb in Anführungszeichen.
- Der Task schreibt `stonecutter.gradle` neu (`stonecutter.active "..."`) und verarbeitet
  zugleich alle Preprocessor-Kommentare in `src/` für die neue Version.
- Intern heißen die Tasks `stonecutterSwitchTo<version>` (vom IDE-Sync/Model genutzt).

### 7.3 Vor einem Git-Commit: auf VCS-Version zurücksetzen

```bash
./gradlew "Reset active project"
```

Setzt `src/` auf den Zustand von `vcsVersion` (`1.21.4`) zurück und verhindert
Preprocessor-Diff-Noise im VCS.

### 7.4 Kommentare im falschen Zustand reparieren

```bash
./gradlew "Refresh active project"
```

Führt den Comment-Processor erneut auf der aktiven Version aus (nützlich, wenn ein
Versionswechsel abgebrochen wurde).

### 7.5 Bauen

```bash
./gradlew build              # baut ALLE 13 Nodes (Root-Task propagiert in Subprojekte)
./gradlew :1.21.4:build      # nur ein Node
./gradlew :26.1:build
```

Fertige Jars liegen in `versions/<v>/build/libs/`:

```
crystal-per-second-hud-1.0.0+mc1.21.4.jar
crystal-per-second-hud-1.0.0+mc1.21.4-sources.jar
```

(Alte, ohne `mc` benannte Jars im `build/libs` stammen von einer früheren Naming-Konfiguration.)

### 7.6 Client starten (ein Node)

```bash
./gradlew :1.21.4:runClient
./gradlew :26.1:runClient
```

Alle Nodes teilen sich `<root>/run/` (gemeinsame `options.txt`), `programArg "--username=dev"`.

### 7.7 Minecraft-Quellen für einen Node dekompilieren (Nachschlagen von APIs)

```bash
./gradlew :1.21.4:genSources
```

legt die `-sources.jar` im Loom-Cache ab; einzelne Klassen extrahieren z. B. mit
`unzip -p ...-sources.jar net/minecraft/client/gui/screen/Screen.java`.

### 7.8 Interne Stonecutter-Tasks (nicht von Hand aufrufen)

`stonecutterGenerate`, `stonecutterPrepare`, `stonecutterMerge` (je Node) sowie
`stonecutterSaveModels`, `stonecutterSaveTreeModel`, `stonecutterSaveBranchModel`.
`compileJava` und `sourcesJar` sind über `dependsOn` auf `stonecutterGenerate` gebunden.

---

## 8. Funktionsweise beim Bauen (Generated-Sources-Pipeline)

1. Du editierst **nur** `src/main/java/` (mit `//?`-Markierungen) und
   `src/main/resources/`.
2. Beim Bauen eines Nodes führt Gradle zuerst `stonecutterGenerate` aus. Es liest den
   geteilten `src/`-Baum und schreibt pro Node den aufgelösten Code nach
   `versions/<v>/build/generated/stonecutter/main/java/`.
3. `sourceSets.main.java` zeigt auf dieses generierte Verzeichnis → Loom kompiliert den
   aufgelösten Code.
4. Ressourcen kommen direkt aus `src/main/resources`; `processResources` ersetzt die
   Platzhalter in `fabric.mod.json` (`version`, `minecraft_version`, `java_version`).
5. Ergebnis: pro Node ein eigenes, korrekt aufgelöstes Jar mit passendem
   `fabric.mod.json` und passendem Dateinamen.

**Merke:** Es gibt zwei Quellcode-Zustände —
- den **geteilten** `src/` (was du editierst, abhängig von der aktiven Version) und
- die **generierten** Sources in `versions/<v>/build/generated/` (deterministisch aus `src/`
  erzeugt).

Wechselst du die Version, wird `src/` umgeschrieben; generiert wird beim nächsten Build neu.

---

## 9. Schritt-für-Schritt: Normales Projekt → Stonecutter-Projekt

Diese Reihenfolge reproduziert die Einrichtung exakt:

1. **Gradle-Wrapper 9.5.1** bereitstellen (`gradle/wrapper/gradle-wrapper.properties`,
   `gradle-wrapper.jar`, `gradlew`, `gradlew.bat`) — Inhalt aus 3.6.

2. **`settings.gradle` anlegen** — exakt wie in 3.1: `pluginManagement` (4 Repos),
   `plugins { id 'dev.kikugie.stonecutter' version '0.9.6' }`,
   `stonecutter { kotlinController = false; centralScript = 'build.gradle';
   create(rootProject) { versions(...); vcsVersion = '...' } }`, `rootProject.name = ...`.

3. **Erstes Sync/`./gradlew`** starten — Stonecutter erzeugt `stonecutter.gradle`
   (Inhalt wie 3.2) und legt die Node-Verzeichnisse unter `versions/` an.

4. **`gradle.properties` (Root)** anlegen — exakt wie 3.3. Wichtig: `loom_version`,
   `loader_version`, `mod_version`, `maven_group` und
   `dev.kikugie.stonecutter.hard_mode=true`.

5. **`build.gradle` (zentral)** anlegen — exakt wie 3.5. Alle `sc.current.parsed`-Zweige,
   `buildscript`-Classpath, `sourceSets`, `compileJava`-Abhängigkeit, `processResources`,
   Jar-Naming übernehmen.

6. **`versions/<version>/gradle.properties`** für **jeden** registrierten Node anlegen
   (Tabelle aus Abschnitt 4). Die Mappings-Property trägst du je nach deiner Mapping-Wahl ein
   (hier Yarn; 26.1 ohne Mappings, da unobfuskiert).

7. **Quellcode nach `src/main/java` bzw. `src/main/resources` verschieben** und die
   versionabhängigen Stellen mit `//? if/elif/else`-Kommentaren markieren (Abschnitt 5).
   Immer **ganze Konstrukte** in Zweige packen, `@Override` einfach setzen, neueste
   Bedingungen in Ketten zuerst.

8. **`fabric.mod.json`** mit Platzhaltern versehen:
   ```json
   {
       "version": "${version}",
       "depends": {
           "minecraft": "~${minecraft_version}",
           "java": "${java_version}"
       }
   }
   ```
   (Wird von `processResources` pro Node befüllt.)

9. **`.gitignore`** übernehmen (3.7) und `LICENSE` ins Root legen (wird ins Jar gebunden).

10. **(Optional) CI** `build.yml` anlegen (3.8) — mit Java 25.

11. **Testen:**
    ```bash
    ./gradlew "Set active project to 1.21.4"   # aktive Version setzen
    ./gradlew build                            # alle Nodes bauen
    ./gradlew :1.21.4:runClient                # Client-Test
    ./gradlew "Reset active project"           # vor dem Commit
    ```

**Nicht vergessen:**
- Das zentrale `build.gradle` läuft **pro Node**, nicht einmal — relative Pfade beziehen sich
  auf den jeweiligen Node (deshalb `${rootProject.projectDir}/run` und
  `${rootProject.projectDir}/src/main/resources`).
- Projektweite Repositories (Terraformers etc.) gehören in `repositories { }`, **nicht** in
  `publishing.repositories { }`.
- Loom muss bedingt angewendet werden können → `buildscript { classpath ... }` ist nötig,
  ein reines `plugins { }` reicht hier nicht.

---

## 10. Quellen & weiterführende Referenzen

- Stonecutter-Wiki: `https://stonecutter.kikugie.dev/wiki`
- Plugin-Source (aktuellste Version): `https://github.com/stonecutter-versioning/stonecutter`
- Offizielles Fabric+Stonecutter-Template:
  `https://github.com/stonecutter-versioning/stonecutter-template-fabric`
- Lokale Projekt-Artefakte: `crystal-per-second-hud-template-1.21.4.zip` (Ausgangs-Template)
  und `template-mod-template-1.21.4-mojang.zip` (Mojang-Template) im Projekt-Root.

*Dokument erstellt aus dem Ist-Zustand des Projekts (Stonecutter 0.9.6, Gradle 9.5.1,
Loom 1.17-SNAPSHOT, 13 Versionen, `vcsVersion`/aktiv = 1.21.4).*
