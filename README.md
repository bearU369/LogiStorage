<picture>
    <img src="docs/logo-banner.png">
</picture>

LogiStorage is an addon mod for bridging automation tools between **ComputerCraft** and **Applied Energistics 2**, allowing power users build the most sophisticated and advanced automation setup at their fingertips.

Only compatible with **Forge 1.20.1**, this mod is built using NeoForge's *LegacyForge* to be compatible with *Forge*.

For guides and documentations regarding the mod, click [here](docs/guide.md) to learn more.

## Dependencies
- [CC:Tweaked](https://github.com/cc-tweaked/CC-Tweaked)
- [Applied Energistics 2](https://github.com/AppliedEnergistics/Applied-Energistics-2)

## How to Build

**NOTE**: You must have **Git**, **Gradle**, and **JDK 21** installed before cloning this project.
Make sure your *PATH* environment included the directories of the required binaries before doing the build process.

- Open your terminal.
- Clone the project via `git clone https://git.home.arpa/bear_369/LogiStorage.git`
- Change directory to the project via `cd ./LogiStorage`
- Run `gradle build` or `./gradlew build` to build the project.
- Run `gradle runData` or `./gradlew runData` to generate data files for item recipes, loot tables, and other data.
- Run `gradle runClient` or `./gradlew runClient` to test the mod, running Minecraft bundled with the mod and other mods within.

## License
This project is licensed under multiple terms:
- **Source Code**: Distributed under [LGPL-2.1-or-later](LICENSE) license.
- **Assets** (art, music, sound): Distributed under [CC-BY-SA-4.0](LICENSE-ASSETS) license.

See the [LICENSE](LICENSE) and [LICENSE-ASSETS](LICENSE-ASSETS) files for more information.