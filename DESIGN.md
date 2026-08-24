# MultiCraft Helper — Design

A client-side Fabric mod for Hypixel SkyBlock that reimplements SkyOcean's
single-item **Craft Helper** and lets you keep **multiple craft recipe trees on
screen at once**, so you can work toward several items without swapping the active
one. Built and tested against **Minecraft 1.21.11 (Java 21)**; see §2 for why not
26.1.2.

## 1. Goal & scope
Reimplement the single-item Craft Helper exactly — item + amount → recursive
recipe tree with per-material have/need progress — then render **N of them
simultaneously**, each fully independent.

**In scope:** recipe-tree resolution; live owned-quantity tracking across
inventory / ender chest / storage / sacks; a multi-target HUD; commands to add /
remove / reorder / clear targets.

**Out of scope:** cross-target material budgeting (each target counts owned
materials on its own, exactly like running the single-item helper twice); price
integration; **any automation of crafting or input** (Hypixel rules — the mod is
display-only).

## 2. Target & stack
- **Minecraft 1.21.11**, **Java 21**, **Fabric**, client-only. (Build verified:
  Gradle 9.7.1 + Loom 1.15.5, `./gradlew build` green, 6/6 tests, jar produced.)
- **Kotlin 2.4.0** + Fabric Language Kotlin, matching the SkyBlock-mod ecosystem.
- **Official Mojang mappings** (available for 1.21.x), Fabric Loader 0.18.3,
  Fabric API 0.141.6+1.21.11, Loom 1.15-SNAPSHOT.

> **Why not 26.1.2?** The year-based release has no public obfuscation mappings —
> Mojang's release JSON ships only `client`/`server` jars (no `client_mappings`),
> and Fabric has no Yarn for 26.x — so stock Loom can't map it. SkyOcean targets
> 26.x via Stonecutter + a bespoke mapping setup (kikugie/teamresourceful mavens).
> Supporting 26.x is a future Stonecutter target; we develop on 1.21.11 meanwhile.

## 3. Data source (decision)
SkyOcean does **not** own its recipe data; it consumes the open **NEU
(NotEnoughUpdates) item schema** via the separate MIT-licensed `meowdding-repo`.
So rather than depend on SkyOcean (runtime coupling, unstable internal APIs, its
restrictive license), we consume that same upstream data directly:

- Parse the **NEU item schema**: per-item JSON with a `recipes` array; each
  crafting recipe is a 3×3 grid keyed `A1..C3` = `"INTERNALNAME:qty"`, plus a
  `count` output size.
- Ship a small **bundled snapshot** (`resources/data/sample_recipes.json`) so the
  mod works offline; a full snapshot (NEU / meowdding-repo, MIT) is merged from a
  cache dir at runtime (next data milestone).

## 4. Architecture
Pure-JVM core (no Minecraft import; unit-tested) vs. Minecraft integration layer.

```
data/     RepoItem, Recipe, RecipeTree, ItemRepository, RecipeResolver   [pure]
inventory/QuantityIndex, OwnedProvider                                    [pure]
          ContainerScanner, SackTracker                                   [mc]
craft/    CraftTarget, CraftTargetManager, ProgressCalculator             [pure]
ui/       TargetWidget (line model)                                       [pure]
          CraftHud (drawing)                                              [mc]
command/  CraftCommands (/mch ...)                                        [mc]
config/   Config                                                          [pure]
MultiCraft (runtime holder) / MultiCraftClient (entrypoint)               [mc]
```

The `[mc]` files are the only ones whose API names depend on 26.1.2 mappings;
everything they call into is verified pure code.

## 5. Recipe engine
- **RecipeResolver** expands `(item, amount)` into a `RecipeNode` tree: picks the
  primary crafting recipe, `craftsNeeded = ceil(amount / outputCount)`, multiplies
  ingredient quantities down each level. Memoised by `(name, amount)`, cycle-guarded
  by expansion path, depth-capped.
- **ProgressCalculator** layers owned quantities on the tree: a node is *satisfied*
  when you own enough of it directly **or** enough of every child; `percent` is
  leaf-quantity-weighted. Each target is scored independently.

## 6. Multi-item display (the delta)
- **CraftTargetManager** holds an *ordered, persisted list* of targets instead of
  one active recipe. `add` accumulates (doesn't replace); plus `remove`, `clear`,
  `move`, `setAmount`, `toggleCollapsed`. Persists inputs only and re-resolves on
  load, so repo updates are picked up automatically.
- **TargetWidget** builds one target's lines (header + tree). **CraftHud** stacks
  every target's lines in one anchored, corner-aligned panel. No cross-target logic.

## 7. Item finding (owned quantities)
- **QuantityIndex**: `internalName -> count`, broken down by `Source`, thread-safe,
  per-source wholesale replacement to avoid double-counting moved stacks.
- **ContainerScanner** reads open screens; the SkyBlock id comes from the
  `minecraft:custom_data` component (`ExtraAttributes.id`). **SackTracker** parses
  `Stored: n/cap` lore. Sack/storage data is cached with a timestamp; staleness is
  surfaced in the HUD.

## 8. Commands
`/mch add <item> [amount]` · `remove <item>` · `clear` · `list` ·
`move <item> up|down` · `collapse <item>` · `hud` (toggle).

## 9. Milestones
0. **Scaffold** — builds on 1.21.11, tests pass, jar produced. *(done)*
1. **Data** — full NEU/meowdding snapshot download + merge into the cache.
2. **Counts** — ContainerScanner for inventory + ender chest wired to QuantityIndex.
3. **HUD** — CraftHud drawing verified in-game for one target.
4. **Multi** — several targets stacked; commands + persistence (core done).
5. **Sacks/storage** + config screen.

## 10. Open questions / risks
- 26.x support requires a Stonecutter + community-mapping setup (see §2); deferred.
- The `[mc]` layer compiles against 1.21.11 but hasn't been exercised **in-game**
  yet (HUD draw, screen scanning, sack parsing) — that's milestones 2–5.
- meowdding-repo/NEU attribution terms when bundling a full snapshot.
- Sack-data freshness — no reliable push source; how aggressively to warn on stale.
