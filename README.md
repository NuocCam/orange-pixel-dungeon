# Orange Pixel Dungeon

Orange Pixel Dungeon is a fork of [Shattered Pixel Dungeon](https://github.com/00-Evan/shattered-pixel-dungeon) v4.0.2. It's the normal game with some new artifacts, talents and spells, a few reworked trinkets and some balance changes.

It has its own app id (`nuoccam.orangepixeldungeon`), so it installs next to regular Shattered Pixel Dungeon and the saves are separate.

## Download

Builds are on the [releases page](https://github.com/NuocCam/orange-pixel-dungeon/releases/latest). Each release has an Android .apk, a desktop .jar and an iOS .ipa.

- Android: open the .apk on your phone and allow installs from that app when it asks.
- Desktop: needs Java 11 or newer, then `java -jar OrangePixelDungeon-<version>-Desktop.jar`.
- iOS: the .ipa is unsigned, so you have to sideload it with Sideloadly or AltStore. I haven't tried it on a real device yet.

## What's different

The in-game list is under Changes on the title screen. The short version:

- New Medic Kit artifact, and Cape of Thorns and Lloyd's Beacon drop again.
- Universal tier 3 talents, a new tier 1 and tier 2 talent for every hero, and 3 new tier 3 talents for every subclass.
- New cleric spells: Divine Blast, Divine Ray, Holy Bomb, Resurrection, Holy Mantle, Power of Life and Aggression Induction.
- Mossy Clump now fills floors with grass and plants that grow back when trampled.
- Dimensional Sundial changes enemy health and damage depending on the time of day.
- Cracked Spyglass repairs itself after 10 hidden items and turns into a Spyglass. Talisman of Foresight warns you about hidden items.
- Blessed Ankh gives a longer invulnerability and a shield. The Sad Ghost reward is always at least +1.
- Parchment Scrap makes enchantments and glyphs stronger but a bit rarer.
- Dried Rose's ghost deals magic damage and can wear cursed gear. Potions and bombs no longer heal it, and Wand of Transfusion shields it instead.
- Earthen Guardian has more armor and takes less magic damage, but can't dodge.

## Building

Needs JDK 11 or newer (17 for the iOS build). Gradle is included.

```
./gradlew desktop:debug
./gradlew desktop:release
./gradlew android:assembleDebug
```

The first runs the game, the second builds a jar into `desktop/build/libs/`, the third makes a debug .apk. The setup guides from upstream are in `docs/`: [Android](docs/compiling-for-android.md), [desktop](docs/compiling-for-desktop.md), [iOS](docs/compiling-for-ios.md).

## Credits

- [Shattered Pixel Dungeon](https://shatteredpixel.com/shatteredpd/) by Evan Debenham. It's a great game, go support it on [Patreon](https://www.patreon.com/ShatteredPixel) or buy it on [Steam](https://store.steampowered.com/app/1769170/Shattered_Pixel_Dungeon/), [Google Play](https://play.google.com/store/apps/details?id=com.shatteredpixel.shatteredpixeldungeon) or the [App Store](https://apps.apple.com/app/shattered-pixel-dungeon/id1563121109).
- [Pixel Dungeon](https://github.com/00-Evan/pixel-dungeon-gradle) by Watabou.
- A lot of the new talents and spells come from [Re-ARranged Pixel Dungeon](https://github.com/Hoto-Mocha/Re-ARranged-Pixel-Dungeon) by Hoto-Mocha.
- The debug scroll is [scrollofdebug](https://github.com/zrp200/scrollofdebug) by zrp200.
- Translations are from Shattered's [Transifex project](https://explore.transifex.com/shattered-pixel/shattered-pixel-dungeon/). The text Orange adds is English only.

This isn't an official Shattered Pixel Dungeon release, so please report Orange bugs here and not to its developer.

## License

GPLv3, same as Shattered Pixel Dungeon. See [LICENSE.txt](LICENSE.txt).
