# SkyOcean Item Tracker HUD

A Minecraft Fabric mod for version 26.1.2 that creates a custom item tracking HUD/overlay by leveraging SkyOcean's powerful inventory and storage search functions and craft helper system.

## Features

- **Item Search Integration**: Use SkyOcean's item search to find items across all storage locations
- **Custom HUD Overlay**: Display tracked items in a customizable on-screen overlay
- **Craft Helper Integration**: Show craft helper information for items you're working towards
- **Real-time Updates**: Monitor your inventory and storage in real-time

## Requirements

- Minecraft 26.1.2
- Fabric Loader 0.16.7+
- Fabric API 0.100.5+26.1.2
- Fabric Language Kotlin 1.12.5+
- SkyOcean 1.17.2+26.1.2

## Building

```bash
./gradlew build
```

## Project Structure

- `src/main/kotlin/com/example/skyoceantracker/` - Main mod code
  - `SkyOceanTrackerMod.kt` - Mod entry point
  - `hud/ItemTrackerHUD.kt` - HUD overlay rendering
  - `integration/SkyOceanIntegration.kt` - SkyOcean API integration layer

## SkyOcean API Reference

Key classes to integrate with:

- `me.owdding.skyocean.features.item.sources.ItemSource` - Access to various item sources
- `me.owdding.skyocean.data.profile.CraftHelperStorage` - Craft helper data management
- `me.owdding.skyocean.features.recipe.crafthelper.CraftHelperManager` - Craft helper state management
- `me.owdding.skyocean.features.recipe.SimpleRecipeApi` - Recipe lookup

## License

MIT
