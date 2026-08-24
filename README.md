# MultiCraft Helper

A client-side **Fabric** mod for **Hypixel SkyBlock** that lets you track
**multiple craft recipe trees on screen at once** — a reimplementation of
SkyOcean's single-item Craft Helper, extended so you can work toward several items
without swapping the active one.

Built against **Minecraft 26.1.2 (Java 25)**.

See [`DESIGN.md`](DESIGN.md) for the full design.

## Commands
```
/mch add <item> [amount]   add (or re-amount) a tracked craft
/mch remove <item>         stop tracking one
/mch clear                 remove all
/mch list                  list active targets
/mch move <item> up|down   reorder
/mch collapse <item>       collapse/expand a target's tree
/mch hud                   toggle the HUD
```
`<item>` is a SkyBlock internal id, e.g. `ENCHANTED_IRON_BLOCK`.

## How it's built
- **Pure-JVM core** (`data/`, `craft/`, `config/`, part of `inventory/`, `ui/`):
  recipe parsing, tree resolution, progress, multi-target management. No Minecraft
  dependency — unit-tested under `src/test`.
- **Minecraft integration layer** (files marked `MINECRAFT INTEGRATION LAYER`):
  commands, HUD drawing, inventory scanning.

Recipe data uses the open **NEU item schema** (the same upstream data SkyOcean
consumes via meowdding-repo), not SkyOcean's code. A tiny snapshot is bundled for
offline use; a full snapshot is merged from a cache dir at runtime.

## Build

```bash
export JAVA_HOME=/path/to/jdk-25
./gradlew build       # compiles, runs tests, produces build/libs/multicraft-helper-0.1.0.jar
./gradlew test        # unit tests only
./gradlew runClient   # launch a dev client
```

Verified: `./gradlew build` succeeds on JDK 25 with Gradle 9.7.1 + Loom 1.15.5,
all 6 unit tests pass, and a mod jar is produced.

### How 26.1.2 is targeted (no mappings needed)
Minecraft **26.1+ ships deobfuscated** — Mojang distributes the client with real
class names, so there are no obfuscation mappings and none are needed (that's why
the release JSON has no `client_mappings` and Fabric publishes no Yarn for 26.x).
The build uses [`dev.kikugie.loom-back-compat`](https://codeberg.org/KikuGie/loom-back-compat)
with `loomx.unobfuscated=true`, which applies plain Fabric Loom with **no
remapping**: `loomx.applyMojangMappings()` is a no-op and mod deps are added via
`modImplementation` (aliased to plain configs). The resulting jar references the
real 26.1 names directly — so it loads on the deobfuscated 26.1.2 runtime. (A jar
built for an older obfuscated version like 1.21.11 would instead be remapped to
intermediary names such as `class_7157`, which don't exist on 26.1.2 — that
mismatch is exactly what crashes such a jar on launch.)

## License
MIT — see [`LICENSE`](LICENSE). Recipe data is sourced from the NEU/meowdding-repo
ecosystem; confirm their attribution terms before redistributing a full snapshot.
