# Flushfire 0.2.1 alpha — official build source

This package is **source code, not a precompiled working JAR**. The previous
0.2.0 test JAR used handwritten temporary stubs and failed at runtime with
`NoSuchFieldError: Blocks.FIRE` because the stub declared `FireBlock` while
Minecraft 1.21.1 declares `Blocks.FIRE` as `Block`. It also used an incorrect
`BuiltInRegistries.BLOCK` field type, which could cause another crash.

The Java source does not require a special cast to fix these errors: an official
NeoForge Gradle compilation emits the correct binary references automatically.
This project intentionally **does not** include those fake stubs.

## Build on GitHub (no Gradle installation needed)

1. Extract this ZIP on your computer.
2. Create a new **empty** GitHub repository (do not initialize with README).
3. Upload **all contents** of the extracted folder, including `.github/workflows/build.yml`,
   into the repository root and commit them to the `main` branch.
4. On GitHub open **Actions** > **Build Flushfire with official NeoForge** > **Run workflow**.
   A push to main also starts the build automatically.
5. Wait for the action to finish. On a successful run, download the artifact
   `Flushfire-0.2.1-alpha-NEOFORGE-BUILD`, extract it, and use the `.jar` inside.
6. Delete *all previous* Flushfire JARs from your `mods` folder before adding the result.
7. Test in an isolated instance and a disposable world first.

The workflow uses Java 21, Gradle 8.10.2, NeoGradle 7.1.38 and NeoForge 21.1.266.
If the action fails to compile, the build log will show the real source/API problem
that needs fixing. **A CI build can still fail**; this ZIP is not a verified release.

## Build locally

With Java 21 and Gradle 8.10.2 installed:

```shell
gradle clean build --no-daemon
```

The JAR should be in `build/libs/`.

## Behavior currently implemented (not yet game-verified)

- Model and selection-outline offsets based on support collision shapes.
- Attempts to allow ordinary fire to survive on non-full geometry.
- Shift + flint-and-steel/fire-charge for flammable exposed faces.
- Heuristic cleanup for finished Burnt blocks.

Important limits: The fire can only occupy an empty block cell; it cannot replace
a door or fence in the same block cell. Fire damage is standard for entities *inside
that fire cell*, but the visually shifted portion below it is **not guaranteed** to
deal damage. Burnt lifecycle integration remains incomplete, and neither Sodium
compatibility nor modded-block support is fully tested. Do not distribute as a
production-ready public-modpack release yet.

All implementation is original; no Tightfire source or assets are included.
