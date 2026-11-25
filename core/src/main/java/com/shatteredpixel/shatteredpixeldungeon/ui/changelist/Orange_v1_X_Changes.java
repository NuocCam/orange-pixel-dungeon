/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.ui.changelist;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.ChangesScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

import java.util.ArrayList;

public class Orange_v1_X_Changes {

	private static final int ORANGE = 0xFF9933;

	public static void addAllChanges( ArrayList<ChangeInfo> changeInfos ){
		add_v1_1_Changes(changeInfos);
		add_v1_0_Changes(changeInfos);
	}

	private static ChangeInfo section( ArrayList<ChangeInfo> changeInfos, String key, int color ){
		ChangeInfo changes = new ChangeInfo(Messages.get(ChangesScene.class, key), false, null);
		changes.hardlight(color);
		changeInfos.add(changes);
		return changes;
	}

	public static void add_v1_1_Changes( ArrayList<ChangeInfo> changeInfos ) {

		ChangeInfo changes = new ChangeInfo("v1.1", true, "");
		changes.hardlight(ORANGE);
		changeInfos.add(changes);

		changes = new ChangeInfo("v1.1.1", false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton( new ChangeButton( ChangeIcons.V061_BUGFIX, Messages.get(ChangesScene.class, "bugfixes"),
				"Fixed the following bugs:\n" +
				"**-** The Medic Kit description showing raw formatting codes and logging an error\n" +
				"**-** The Spyglass being unidentified after the Cracked Spyglass repairs itself\n" +
				"**-** The Talisman of Foresight showing a missing text message when a hidden item is close by"));

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.ARTIFACT_MEDIC_KIT), "Medic Kit",
				"**-** The Medic Kit now shows its charges with the maximum, like 2/5\n" +
				"**-** Healing to full health now uses only as many charges as it takes, taking the Vial of Blood's bonus into account, and at +6 anything the healing can't use still becomes a shield\n" +
				"**-** Healing from the kit is now spread out by the Vial of Blood like other healing, including the instant heal at +10"));

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.RETURN_BEACON), "Beacon of Returning",
				"The Beacon of Returning spell has been removed from alchemy. Lloyd's Beacon can still be used to set a return point."));

		changes = section(changeInfos, "new", Window.TITLE_COLOR);

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.ARTIFACT_MEDIC_KIT), "Orange Versions & Changelog",
				"Orange Pixel Dungeon now has its own version number, starting at v1.0.\n" +
				"\n" +
				"This screen lists everything Orange changes compared to the original Shattered Pixel Dungeon v4.0.2, split into New, Changes, Buffs and Nerfs for each Orange version. The original game's changelogs are still available with the other buttons below."));

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.SPYGLASS_REPAIRED), "Spyglass",
				"A new trinket that cannot be found in the dungeon. It is what a Cracked Spyglass becomes once its lens is repaired, see the Cracked Spyglass entry under Changes."));

		changes = section(changeInfos, "changes", CharSprite.WARNING);

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.MOSSY_CLUMP), "Mossy Clump",
				"The Mossy Clump has been reworked. It no longer turns floors into grass or water themed floors, and instead:\n" +
				"\n" +
				"**-** Always fills each floor with patches of grass and random plants, half helpful and half harmful. 4 plants at +0, and 2 more per level.\n" +
				"**-** Whenever a plant is trampled by anyone, a new random plant regrows somewhere else on the floor, which can be helpful or harmful.\n" +
				"**-** Every helpful plant trampled on a floor makes regrowing plants there 5% less likely to be helpful, until they are all harmful. This only affects that floor.\n" +
				"\n" +
				"Upgrade cost is unchanged."));

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.SUNDIAL), "Dimensional Sundial",
				"The Dimensional Sundial no longer changes how quickly enemies spawn. Instead it changes enemies based on the real-life time of day:\n" +
				"\n" +
				"**-** At night (8pm-8am): enemies have 5-15% less health, but deal 0-10% more damage\n" +
				"**-** During the day (8am-8pm): enemies deal 5-15% less damage, but have 0-10% more health\n" +
				"\n" +
				"The effect scales with the trinket's level. Health is set when an enemy spawns, and bosses are not affected by the health change."));

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.SPYGLASS), "Cracked Spyglass",
				"The Cracked Spyglass now keeps track of how many of its hidden items you have picked up. After 10, the lens is repaired and the trinket becomes a **Spyglass**, which keeps its level and still creates extra items on each floor, but the items are no longer hidden."));

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.ARTIFACT_TALISMAN), "Talisman of Foresight",
				"The talisman now also warns you when an item hidden by the Cracked Spyglass is close by, just like it does for hidden traps."));

		changes.addButton( new ChangeButton( Icons.get(Icons.INFO), "About Screen",
				"The about screen now credits the developer of Orange Pixel Dungeon, with a link to their GitHub page. Shattered Pixel Dungeon and everyone else involved is still credited below."));

		changes = section(changeInfos, "buffs", CharSprite.POSITIVE);

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.WAND_LIVING_EARTH), "Earthen Guardian",
				"The guardian created by the Wand of Living Earth is sturdier:\n" +
				"\n" +
				"**-** Armor increased by 1, plus 1 more every 2 wand levels\n" +
				"**-** Takes 20% less damage from magical sources"));

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.PARCHMENT_SCRAP), "Parchment Scrap",
				"Weapon enchantments and armor glyphs you own are now 5-15% stronger while you carry the Parchment Scrap, depending on its level."));

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.ARTIFACT_ROSE3), "Dried Rose",
				"**-** The ghost now deals magical damage, which ignores the armor of enemies\n" +
				"**-** The ghost can now wear cursed equipment, and you can take it back from them at any time"));

		changes = section(changeInfos, "nerfs", CharSprite.NEGATIVE);

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.PARCHMENT_SCRAP), "Parchment Scrap",
				"Enchantments and glyphs now appear a little less often than before:\n" +
				"\n" +
				"**-** +0: 1.5x as common, down from 2x\n" +
				"**-** +1: 3x as common, down from 4x\n" +
				"**-** +2: 5x as common, down from 7x\n" +
				"**-** +3: 8x as common, down from 10x"));

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.WAND_LIVING_EARTH), "Earthen Guardian",
				"The guardian no longer has any evasion, so it can't dodge attacks. It used to have half of your evasion."));

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.ARTIFACT_ROSE3), "Dried Rose",
				"The ghost is a spirit, so it can no longer be:\n" +
				"\n" +
				"**-** Healed by the Wand of Transfusion, it shields them instead\n" +
				"**-** Healed by potions or bombs thrown at it, including honeyed healing and the regrowth bomb\n" +
				"\n" +
				"Other effects that help allies still work."));
	}

	public static void add_v1_0_Changes( ArrayList<ChangeInfo> changeInfos ) {

		ChangeInfo changes = new ChangeInfo("v1.0", true, "");
		changes.hardlight(ORANGE);
		changeInfos.add(changes);

		changes = section(changeInfos, "new", Window.TITLE_COLOR);

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.ARTIFACT_MEDIC_KIT), "Welcome to Orange!",
				"Shattered Pixel Dungeon v4.0.2 is now Orange Pixel Dungeon! This first release has a new title screen, and adds lots of new talents, artifacts and other changes, many of them adapted from Re-ARranged Pixel Dungeon.\n" +
				"\n" +
				"The original changelogs are kept as part of the game's history."));

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.ARTIFACT_MEDIC_KIT), "Medic Kit",
				"The new **Medic Kit** artifact holds charges that recharge over time. It holds 1 charge at +0, up to 10 charges at +9, and each charge restores 10% of max health (12.5% from +5, 15% at +10). Using it takes a turn.\n" +
				"\n" +
				"**-** Level 3: healing is applied much faster\n" +
				"**-** Level 6: excess healing becomes a shield, and you can choose to heal to full or use every charge\n" +
				"**-** Level 10: healing is instant and takes no time\n" +
				"**-** Gains a level from each Potion of Healing, a full waterskin or a blessed ankh saving you\n" +
				"**-** With the Pharmacophobia challenge, the kit can only create shields\n" +
				"**-** A cursed kit cannot be used, and while it is equipped any healing item (potions of healing, dew, honeyed healing) poisons you for half of the heal amount instead of healing you"));

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.ARTIFACT_CAPE), "Artifacts Return",
				"The **Cape of Thorns** and **Lloyd's Beacon** can now be found as random artifact loot."));

		changes.addButton( new ChangeButton( new TalentIcon(Talent.ATK_SPEED_ENHANCE), "Universal Tier 3 Talents",
				"Choosing a subclass now also unlocks 4 talents shared by all subclasses:\n" +
				"**-** Attack Speed Enhancement: +5% attack speed per level (max +4)\n" +
				"**-** Accuracy Enhancement: +5% accuracy per level (max +4)\n" +
				"**-** Evasion Enhancement: +5% evasion per level (max +4)\n" +
				"**-** Better Choice: gives a Stone of Enchantment, Scroll of Enchantment and Scroll of Upgrade"));

		changes.addButton( new ChangeButton( new TalentIcon(Talent.MAX_HEALTH), "New Tier 1 & 2 Talents",
				"Every hero gets a new talent in tier 1 and tier 2:\n" +
				"**Warrior:** Physical Enhancement Training (max health) and Counter Stance (parry)\n" +
				"**Mage:** Charge Preservation and Electromagnetic Induction (faster wand charging)\n" +
				"**Rogue:** Emergency Escape and Swift Stride (movement speed)\n" +
				"**Huntress:** Water Affinity and Hearty Eater\n" +
				"**Duelist:** Skilled Hands and Power Accumulation\n" +
				"**Cleric:** Warding Light and Divine Blast (a new spell)"));

		changes.addButton( new ChangeButton( new TalentIcon(Talent.LETHAL_RAGE), "New Subclass Talents",
				"Every subclass gets 3 new tier 3 talents:\n" +
				"**Berserker:** Lethal Rage, Rage Burst, Endurance in Fury\n" +
				"**Gladiator:** Light Weapons, Offensive Defense, Repeated Finishers\n" +
				"**Battlemage:** Combat Magic (Magic Combo finishers), Spell Rush, Magic Circle\n" +
				"**Warlock:** Dark Magic Madness, Enhanced Mark, Mark of Weakness\n" +
				"**Assassin:** Energy Retrieval, Perfect Assassin, Prudent Preparation\n" +
				"**Freerunner:** Quick Preparation, Overcoming Adversity, Momentary Focus\n" +
				"**Sniper:** Kick, Sharpshooting, Target Acquisition\n" +
				"**Warden:** Living Grass, Attraction, Healing Dew\n" +
				"**Champion:** Faster Charging, Quick Follow-up, Dual Swords Mastery\n" +
				"**Monk:** Energy Restoration, Energy Barrier, Harmony\n" +
				"**Priest:** Divine Ray, Holy Bomb, Resurrection (new spells)\n" +
				"**Paladin:** Holy Mantle, Power of Life, Aggression Induction (new spells)"));

		changes = section(changeInfos, "changes", CharSprite.WARNING);

		changes.addButton( new ChangeButton( Icons.TALENT.get(), "Interface",
				"**-** The tier 3 talent screen now uses two rows when a hero has many talents\n" +
				"**-** The title screen artwork has been redrawn in orange, and the game is now named Orange Pixel Dungeon"));

		changes = section(changeInfos, "buffs", CharSprite.POSITIVE);

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.ANKH), "Blessed Ankh",
				"**-** Invulnerability after being revived is 1 turn longer\n" +
				"**-** Revival health is unchanged, but the hero now also gains a shield equal to 50% of their max health\n" +
				"**-** A Medic Kit in your inventory gains a level when a blessed ankh saves you"));

		changes.addButton( new ChangeButton( ChangeIcons.V081_MISC, "Sad Ghost",
				"The reward for the Sad Ghost's quest is now guaranteed to be at least +1. It is +1 80% of the time, +2 15% of the time and +3 5% of the time."));
	}
}
