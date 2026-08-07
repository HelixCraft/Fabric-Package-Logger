# Stonecutter Migration Guide

This project has been migrated to use [Stonecutter](https://github.com/kmc-sh/stonecutter) for supporting Minecraft versions 1.21 through 1.21.10.

## Project Structure

The project root now serves as a controller for multiple versioned subprojects.
- `settings.gradle`: Defines the supported versions.
- `build.gradle`: The central build script applied to all versions.
- `gradle.properties`: Contains version definitions (e.g., `minecraft_version_1.21`).
- `src/main/java`: Shared source code for all versions.

## Building

To build all versions at once:
```bash
./gradlew buildAll
```
Artifacts will be placed in `build/all_versions`.

To build a specific version:
```bash
./gradlew :1.21.4:build
```

## Running

Run configurations are generated for each version.
- **Client**: `./gradlew :1.21.4:runClient`
- **Server**: `./gradlew :1.21.4:runServer`

## Code Development & Preprocessor

The source code is shared. Use Stonecutter's comment-based preprocessor to handle version differences.

### Syntax

**Conditional Blocks:**
```java
//? if >1.21.1 {
import net.minecraft.some.NewClass;
//? } else {
/*?
import net.minecraft.some.OldClass;
?*/
//? }
```

**Single Line:**
```java
//? if <1.21.2 {
System.out.println("Old version");
//? }
```

**Expression Replacement:**
```java
int x = /*? if >1.21.4 { */ 100 /*? } else { */ 50 /*? } */;
```

### Tips
- Use `/*?` to comment out code that won't compile in the active development version (usually the latest).
- Stonecutter will swap the commented/active blocks for each target version during the build.
- You can switch the active version in `settings.gradle` (change `active = ...`) or rely on IDE plugins if available.

### Example Scenario
If `RenderingAPI` changed in 1.21.5:
```java
//? if >=1.21.5 {
RenderingAPI.drawNew(stack);
//? } else {
/*?
RenderingAPI.drawOld(stack);
?*/
//? }
```

## Adding New Versions

1. Open `settings.gradle` and add the new version to the list.
2. Open `gradle.properties` and add `minecraft_version_X`, `yarn_mappings_X`, `fabric_version_X`.
3. Run `./gradlew projects` to refresh.
