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

package com.shatteredpixel.shatteredpixeldungeon.journal;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.jungle.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.VaultLaser;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.VaultSentry;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.quest.vault.*;
import com.shatteredpixel.shatteredpixeldungeon.items.Dewdrop;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.Embers;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KingsCrown;
import com.shatteredpixel.shatteredpixeldungeon.items.TengusMask;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.PlateArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ScaleArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.food.MysteryMeat;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Pasty;
import com.shatteredpixel.shatteredpixeldungeon.items.food.PhantomMeat;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.WornKey;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfExperience;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfFrost;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLiquidFlame;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfParalyticGas;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.BearPelt;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.BeastMarrow;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.DireWolfPelt;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.DwarfToken;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.GeckoTail;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.GooBlob;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.MetalShard;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.WolfPelt;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRecharging;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTransmutation;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.FihBlade;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Spear;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.SentryRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GuardianTrap;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Rotberry;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import static com.shatteredpixel.shatteredpixeldungeon.journal.MobInfo.Kind.*;
import static com.shatteredpixel.shatteredpixeldungeon.journal.MobInfo.Rating.*;

//the quick stats shown in the bestiary. The 4 ratings compare a mob to the usual enemies of its
//region, and drop chances are the base ones. This list is writen by hand, so when a mob's numbers
//or drops change, change it here too
public class MobInfo {

	public enum Rating { NONE, LOW, NORMAL, HIGH, VERY_HIGH, FLOOR, WEAPON }

	//for drops which aren't one set item
	public enum Kind {
		SEED(ItemSpriteSheet.SEED_HOLDER),
		STONE(ItemSpriteSheet.STONE_HOLDER),
		POTION(ItemSpriteSheet.POTION_HOLDER),
		SCROLL(ItemSpriteSheet.SCROLL_HOLDER),
		WAND(ItemSpriteSheet.WAND_HOLDER),
		RING(ItemSpriteSheet.RING_HOLDER),
		ARTIFACT(ItemSpriteSheet.ARTIFACT_HOLDER),
		WEAPON_ANY(ItemSpriteSheet.WEAPON_HOLDER),
		TIER2_WEAPON(ItemSpriteSheet.WEAPON_HOLDER),
		ARMOR(ItemSpriteSheet.ARMOR_HOLDER),
		THROWN(ItemSpriteSheet.MISSILE_HOLDER),
		ANY(ItemSpriteSheet.CHEST),
		TREASURE(ItemSpriteSheet.CHEST),
		PRIZE(ItemSpriteSheet.WEAPON_HOLDER),
		ITS_WEAPON(ItemSpriteSheet.WEAPON_HOLDER),
		ITS_ARMOR(ItemSpriteSheet.ARMOR_HOLDER);

		public final int image;

		Kind( int image ){
			this.image = image;
		}
	}

	public static class Drop {
		public final Class<? extends Item> item; //null when it's only a kind of item
		public final Kind kind; //null when it's a set item
		public final float chance;
		public String pieces; //shown instead of the chance, for drops which always happen
		public boolean fades; //gets rarer the more of it you've had
		public String note;

		private Drop( Class<? extends Item> item, Kind kind, float chance ){
			this.item = item;
			this.kind = kind;
			this.chance = chance;
		}

		public String name(){
			return Messages.capitalize( item != null
					? Messages.get(item, "name")
					: Messages.get(MobInfo.class, "any_" + kind.name().toLowerCase(Locale.ENGLISH)) );
		}

		public int image(){
			return item != null ? Reflection.newInstance(item).image : kind.image;
		}

		public String noteText(){
			return Messages.get(MobInfo.class, "note_" + note);
		}

		public String chanceText(){
			String text;
			if (pieces != null){
				text = "x" + pieces;
			} else {
				float percent = Math.round(chance * 1000) / 10f;
				text = (percent == (int) percent ? Integer.toString((int) percent)
						: Float.toString(percent)) + "%";
			}
			return fades ? text + "*" : text;
		}
	}

	//all four are null for things which can't be fought
	public final Rating hp, damage, evasion, defense;
	public String ability; //null if it has no tricks
	public final ArrayList<Drop> drops = new ArrayList<>();

	private MobInfo( Rating hp, Rating damage, Rating evasion, Rating defense ){
		this.hp = hp;
		this.damage = damage;
		this.evasion = evasion;
		this.defense = defense;
	}

	public String abilityText(){
		return Messages.get(MobInfo.class, "ability_" + ability);
	}

	public static String word( Rating rating ){
		return Messages.get(MobInfo.class, rating.name().toLowerCase(Locale.ENGLISH));
	}

	//null if the bestiary has nothing extra to say about it
	public static MobInfo of( Class<?> cls ){
		return ALL.get(cls);
	}

	private MobInfo can( String ability ){
		this.ability = ability;
		return this;
	}

	private MobInfo drop( Class<? extends Item> item, float chance ){
		drops.add(new Drop(item, null, chance));
		return this;
	}

	private MobInfo drop( Kind kind, float chance ){
		drops.add(new Drop(null, kind, chance));
		return this;
	}

	//these three change the last drop added
	private MobInfo rarer(){
		drops.get(drops.size()-1).fades = true;
		return this;
	}

	private MobInfo pieces( String pieces ){
		drops.get(drops.size()-1).pieces = pieces;
		return this;
	}

	private MobInfo note( String note ){
		drops.get(drops.size()-1).note = note;
		return this;
	}

	private static final HashMap<Class<?>, MobInfo> ALL = new HashMap<>();

	private static MobInfo add( Class<?> cls, Rating hp, Rating damage, Rating evasion, Rating defense ){
		MobInfo info = new MobInfo(hp, damage, evasion, defense);
		ALL.put(cls, info);
		return info;
	}

	private static MobInfo hazard( Class<?> cls ){
		return add(cls, null, null, null, null);
	}

	static {
		//**** sewers and jungle ****
		add(Rat.class, LOW, LOW, LOW, LOW);
		add(Snake.class, LOW, LOW, VERY_HIGH, NONE).can("snake")
				.drop(SEED, 0.25f);
		add(Gnoll.class, NORMAL, NORMAL, NORMAL, NORMAL)
				.drop(Gold.class, 0.5f);
		add(Swarm.class, VERY_HIGH, LOW, NORMAL, NONE).can("swarm")
				.drop(PotionOfHealing.class, 1/6f).rarer();
		add(Crab.class, NORMAL, NORMAL, NORMAL, VERY_HIGH).can("crab")
				.drop(MysteryMeat.class, 0.167f);
		add(Slime.class, HIGH, NORMAL, NORMAL, NONE).can("slime")
				.drop(TIER2_WEAPON, 0.2f).rarer();
		add(Skink.class, LOW, LOW, NORMAL, NONE);
		add(Deer.class, NORMAL, LOW, NORMAL, LOW).can("deer")
				.drop(SEED, 0.24f)
				.drop(MysteryMeat.class, 0.06f);
		add(Boar.class, NORMAL, NORMAL, NORMAL, LOW).can("boar")
				.drop(MysteryMeat.class, 0.1f);
		add(Toad.class, VERY_HIGH, LOW, LOW, VERY_HIGH).can("toad")
				.drop(PotionOfHealing.class, 1/6f).rarer();
		add(Wolf.class, NORMAL, HIGH, NORMAL, NORMAL)
				.drop(WolfPelt.class, 2/3f).note("three_a_run");
		add(Bear.class, VERY_HIGH, HIGH, NORMAL, HIGH)
				.drop(BearPelt.class, 0.5f).note("one_a_run")
				.drop(TIER2_WEAPON, 0.2f).rarer();
		add(Mantis.class, NORMAL, HIGH, NORMAL, NONE).can("mantis");
		add(Squirrel.class, LOW, NONE, VERY_HIGH, NONE).can("squirrel")
				.drop(SEED, 0.25f)
				.drop(Gold.class, 0.25f);

		add(Albino.class, NORMAL, LOW, LOW, LOW).can("albino")
				.drop(MysteryMeat.class, 1f);
		add(GnollExile.class, VERY_HIGH, HIGH, NORMAL, HIGH).can("gnollexile")
				.drop(ANY, 1f).pieces("2-3");
		add(HermitCrab.class, VERY_HIGH, NORMAL, NORMAL, VERY_HIGH).can("hermitcrab")
				.drop(ARMOR, 1f)
				.drop(MysteryMeat.class, 0.5f);
		add(CausticSlime.class, HIGH, NORMAL, NORMAL, NONE).can("causticslime")
				.drop(GooBlob.class, 1f)
				.drop(TIER2_WEAPON, 0.2f).rarer();
		add(Gecko.class, NORMAL, NORMAL, VERY_HIGH, NORMAL).can("gecko")
				.drop(GeckoTail.class, 0.67f)
				.drop(PotionOfParalyticGas.class, 1f);
		add(DireWolf.class, HIGH, NORMAL, HIGH, NORMAL).can("direwolf")
				.drop(DireWolfPelt.class, 1f);

		add(FetidRat.class, HIGH, LOW, NORMAL, HIGH).can("fetidrat");
		add(GnollTrickster.class, HIGH, NORMAL, NORMAL, NORMAL).can("gnolltrickster")
				.drop(THROWN, 1f);
		add(GreatCrab.class, VERY_HIGH, NORMAL, NONE, VERY_HIGH).can("greatcrab")
				.drop(MysteryMeat.class, 1f).pieces("2");

		add(Goo.class, VERY_HIGH, HIGH, HIGH, NORMAL).can("goo")
				.drop(WornKey.class, 1f)
				.drop(GooBlob.class, 1f).pieces("2-4");
		add(Amalgam.class, VERY_HIGH, HIGH, HIGH, NORMAL).can("amalgam")
				.drop(WornKey.class, 1f)
				.drop(BeastMarrow.class, 1f).pieces("2-4");

		//**** prison ****
		add(Skeleton.class, NORMAL, NORMAL, NORMAL, NORMAL).can("skeleton")
				.drop(WEAPON_ANY, 0.1667f).rarer();
		add(Thief.class, NORMAL, NORMAL, NORMAL, LOW).can("thief")
				.drop(RING, 0.015f).rarer()
				.drop(ARTIFACT, 0.015f).rarer();
		add(DM100.class, NORMAL, NORMAL, NORMAL, NORMAL).can("dm100")
				.drop(SCROLL, 0.25f);
		add(Guard.class, HIGH, HIGH, NORMAL, HIGH).can("guard")
				.drop(ARMOR, 0.2f).rarer();
		add(Necromancer.class, HIGH, NONE, HIGH, NORMAL).can("necromancer")
				.drop(PotionOfHealing.class, 0.2f).rarer();

		add(Bandit.class, NORMAL, NORMAL, NORMAL, LOW).can("bandit")
				.drop(RING, 0.5f).rarer()
				.drop(ARTIFACT, 0.5f).rarer();
		add(SpectralNecromancer.class, HIGH, NONE, HIGH, NORMAL).can("spectralnecromancer")
				.drop(ScrollOfRemoveCurse.class, 1f)
				.drop(PotionOfHealing.class, 0.2f).rarer();

		add(Elemental.NewbornFireElemental.class, VERY_HIGH, VERY_HIGH, NORMAL, NORMAL).can("newbornelemental")
				.drop(Embers.class, 1f)
				.drop(PotionOfLiquidFlame.class, 0.125f);
		add(RotLasher.class, VERY_HIGH, VERY_HIGH, NONE, HIGH).can("rotlasher")
				.drop(SEED, 0.75f);
		add(RotHeart.class, VERY_HIGH, NONE, NONE, NORMAL).can("rotheart")
				.drop(Rotberry.Seed.class, 1f);

		add(Tengu.class, VERY_HIGH, HIGH, HIGH, NORMAL).can("tengu")
				.drop(TengusMask.class, 1f);

		//**** caves ****
		add(Bat.class, NORMAL, NORMAL, NORMAL, LOW).can("bat")
				.drop(PotionOfHealing.class, 0.1667f).rarer();
		add(Brute.class, NORMAL, NORMAL, NORMAL, HIGH).can("brute")
				.drop(Gold.class, 0.5f);
		add(Shaman.RedShaman.class, NORMAL, LOW, NORMAL, NORMAL).can("redshaman")
				.drop(WAND, 0.03f).rarer();
		add(Shaman.BlueShaman.class, NORMAL, LOW, NORMAL, NORMAL).can("blueshaman")
				.drop(WAND, 0.03f).rarer();
		add(Shaman.PurpleShaman.class, NORMAL, LOW, NORMAL, NORMAL).can("purpleshaman")
				.drop(WAND, 0.03f).rarer();
		add(Spinner.class, NORMAL, NORMAL, NORMAL, NORMAL).can("spinner")
				.drop(MysteryMeat.class, 0.125f);
		add(DM200.class, VERY_HIGH, NORMAL, NORMAL, HIGH).can("dm200")
				.drop(WEAPON_ANY, 0.1f).rarer()
				.drop(ARMOR, 0.1f).rarer();

		add(ArmoredBrute.class, NORMAL, NORMAL, NORMAL, VERY_HIGH).can("armoredbrute")
				.drop(ScaleArmor.class, 0.75f)
				.drop(PlateArmor.class, 0.25f);
		add(DM201.class, VERY_HIGH, HIGH, NORMAL, HIGH).can("dm201")
				.drop(MetalShard.class, 1f)
				.drop(WEAPON_ANY, 0.1f).rarer()
				.drop(ARMOR, 0.1f).rarer();

		add(CrystalWisp.class, NORMAL, LOW, NORMAL, NORMAL).can("crystalwisp");
		add(CrystalGuardian.class, VERY_HIGH, NORMAL, NORMAL, HIGH).can("crystalguardian");
		add(CrystalSpire.class, VERY_HIGH, NONE, NONE, NONE).can("crystalspire");
		add(GnollGuard.class, NORMAL, LOW, NORMAL, NORMAL).can("gnollguard")
				.drop(Spear.class, 0.1f);
		add(GnollSapper.class, NORMAL, LOW, NORMAL, NORMAL).can("gnollsapper");
		add(GnollGeomancer.class, VERY_HIGH, LOW, NONE, NORMAL).can("gnollgeomancer");

		add(Pylon.class, NORMAL, NORMAL, NONE, NONE).can("pylon");
		add(DM300.class, VERY_HIGH, HIGH, NORMAL, HIGH).can("dm300")
				.drop(MetalShard.class, 1f).pieces("2-4");

		//**** city ****
		add(Ghoul.class, LOW, NORMAL, NORMAL, NORMAL).can("ghoul")
				.drop(Gold.class, 0.2f);
		add(Elemental.FireElemental.class, NORMAL, NORMAL, NORMAL, NORMAL).can("fireelemental")
				.drop(PotionOfLiquidFlame.class, 0.125f);
		add(Elemental.FrostElemental.class, NORMAL, NORMAL, NORMAL, NORMAL).can("frostelemental")
				.drop(PotionOfFrost.class, 0.125f);
		add(Elemental.ShockElemental.class, NORMAL, NORMAL, NORMAL, NORMAL).can("shockelemental")
				.drop(ScrollOfRecharging.class, 0.25f);
		add(Warlock.class, NORMAL, NORMAL, NORMAL, HIGH).can("warlock")
				.drop(POTION, 0.5f);
		add(Monk.class, NORMAL, NORMAL, HIGH, LOW).can("monk")
				.drop(Food.class, 0.083f);
		add(Golem.class, HIGH, HIGH, NORMAL, VERY_HIGH).can("golem")
				.drop(WEAPON_ANY, 0.1f).rarer()
				.drop(ARMOR, 0.1f).rarer();

		add(Elemental.ChaosElemental.class, NORMAL, NORMAL, NORMAL, NORMAL).can("chaoselemental")
				.drop(ScrollOfTransmutation.class, 1f);
		add(Senior.class, NORMAL, NORMAL, HIGH, LOW).can("senior")
				.drop(Pasty.class, 1f);

		add(VaultSkeleton.class, LOW, LOW, NORMAL, VERY_HIGH).can("vaultskeleton")
				.drop(DwarfToken.class, 1f);
		add(VaultDM100.class, LOW, LOW, NORMAL, VERY_HIGH).can("vaultdm100")
				.drop(DwarfToken.class, 1f);
		add(VaultShaman.class, LOW, NORMAL, NORMAL, VERY_HIGH).can("vaultshaman")
				.drop(DwarfToken.class, 1f);
		add(VaultDM200.class, NORMAL, NORMAL, NORMAL, HIGH).can("vaultdm200")
				.drop(DwarfToken.class, 1f);
		add(VaultBossElemental.class, VERY_HIGH, NORMAL, NORMAL, VERY_HIGH).can("vaultbosselemental");
		hazard(VaultSentry.class).can("vaultsentry");
		hazard(VaultLaser.class).can("vaultlaser");

		add(DwarfKing.class, VERY_HIGH, NORMAL, NORMAL, VERY_HIGH).can("dwarfking")
				.drop(KingsCrown.class, 1f);

		//**** demon halls ****
		add(RipperDemon.class, LOW, NORMAL, NORMAL, LOW).can("ripperdemon");
		add(DemonSpawner.class, HIGH, NONE, NONE, NORMAL).can("demonspawner")
				.drop(PotionOfHealing.class, 1f);
		add(Succubus.class, NORMAL, NORMAL, NORMAL, NORMAL).can("succubus")
				.drop(SCROLL, 0.33f);
		add(Eye.class, NORMAL, NORMAL, NORMAL, NORMAL).can("eye")
				.drop(Dewdrop.class, 0.5f)
				.drop(SEED, 0.25f)
				.drop(STONE, 0.25f);
		add(Scorpio.class, NORMAL, HIGH, NORMAL, HIGH).can("scorpio")
				.drop(POTION, 0.5f);
		add(Acidic.class, NORMAL, HIGH, NORMAL, HIGH).can("acidic")
				.drop(PotionOfExperience.class, 1f);

		add(YogDzewa.Larva.class, LOW, NORMAL, LOW, LOW);
		add(YogFist.BurningFist.class, VERY_HIGH, NORMAL, NORMAL, HIGH).can("burningfist");
		add(YogFist.SoiledFist.class, VERY_HIGH, NORMAL, NORMAL, HIGH).can("soiledfist");
		add(YogFist.RottingFist.class, VERY_HIGH, NORMAL, NORMAL, HIGH).can("rottingfist");
		add(YogFist.RustedFist.class, VERY_HIGH, HIGH, NORMAL, HIGH).can("rustedfist");
		add(YogFist.BrightFist.class, VERY_HIGH, NORMAL, NORMAL, HIGH).can("brightfist");
		add(YogFist.DarkFist.class, VERY_HIGH, NORMAL, NORMAL, HIGH).can("darkfist");
		add(YogDzewa.class, VERY_HIGH, NORMAL, NONE, NONE).can("yogdzewa");

		//**** found on any floor ****
		add(Wraith.class, LOW, FLOOR, FLOOR, NONE).can("wraith");
		add(TormentedSpirit.class, LOW, FLOOR, FLOOR, NONE).can("tormentedspirit")
				.drop(PRIZE, 1f).note("if_cleansed");
		add(Piranha.class, FLOOR, FLOOR, FLOOR, FLOOR).can("piranha")
				.drop(MysteryMeat.class, 1f)
				.drop(FihBlade.class, 0.03f);
		add(PhantomPiranha.class, FLOOR, FLOOR, FLOOR, FLOOR).can("phantompiranha")
				.drop(PhantomMeat.class, 1f)
				.drop(FihBlade.class, 0.25f);
		add(Mimic.class, FLOOR, FLOOR, FLOOR, FLOOR).can("mimic")
				.drop(TREASURE, 1f);
		add(GoldenMimic.class, FLOOR, FLOOR, FLOOR, FLOOR).can("goldenmimic")
				.drop(TREASURE, 1f).note("better");
		add(EbonyMimic.class, FLOOR, FLOOR, FLOOR, FLOOR).can("ebonymimic")
				.drop(TREASURE, 1f).note("extra_item");
		add(CrystalMimic.class, FLOOR, FLOOR, FLOOR, FLOOR).can("crystalmimic")
				.drop(TREASURE, 1f).note("what_it_holds");
		add(Statue.class, FLOOR, WEAPON, FLOOR, FLOOR).can("statue")
				.drop(ITS_WEAPON, 1f);
		add(ArmoredStatue.class, FLOOR, WEAPON, FLOOR, FLOOR).can("armoredstatue")
				.drop(ITS_WEAPON, 1f)
				.drop(ITS_ARMOR, 1f);
		add(GuardianTrap.Guardian.class, FLOOR, WEAPON, FLOOR, FLOOR).can("guardian")
				.drop(ITS_WEAPON, 1f);
		hazard(SentryRoom.Sentry.class).can("sentry");
		add(Ijiraq.class, FLOOR, FLOOR, FLOOR, FLOOR).can("ijiraq")
				.drop(Gold.class, 1f)
				.drop(PotionOfHealing.class, 1f).note("few_per_region");
	}

}
