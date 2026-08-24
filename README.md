# MultiCraft Helper

A client-side **Fabric** mod for **Hypixel SkyBlock** that lets you track
**multiple craft recipe trees on screen at once** — a reimplementation of
SkyOcean's single-item Craft Helper, extended so you can work toward several items
without swapping the active one.

Built and tested against **Minecraft 1.21.11 (Java 21)**.

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
export JAVA_HOME=/path/to/jdk-21
./gradlew build       # compiles, runs tests, produces build/libs/multicraft-helper-0.1.0.jar
./gradlew test        # unit tests only
./gradlew runClient   # launch a dev client
```

Verified: `./gradlew build` succeeds on JDK 21 with Gradle 9.7.1 + Loom 1.15.5,
all 6 unit tests pass, and a remapped mod jar is produced.

### Why not 26.1.2?
The original target was MC **26.1.2**, but that version has **no public
obfuscation mappings**: Mojang's release JSON ships only `client`/`server` jars
(no `client_mappings`), and Fabric publishes no Yarn for the 26.x line. A stock
Loom workspace therefore can't map it. SkyOcean targets 26.x via **Stonecutter**
plus a bespoke mapping setup from the kikugie/teamresourceful mavens. Supporting
26.x here is a future Stonecutter target; the mod is developed on 1.21.11 (which
has official Mojang mappings and a published Fabric API) in the meantime.

## License
MIT — see [`LICENSE`](LICENSE). Recipe data is sourced from the NEU/meowdding-repo
ecosystem; confirm their attribution terms before redistributing a full snapshot.
