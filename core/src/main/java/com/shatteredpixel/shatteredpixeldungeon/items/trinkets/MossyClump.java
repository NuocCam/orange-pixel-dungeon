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

package com.shatteredpixel.shatteredpixeldungeon.items.trinkets;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.RegularLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Blindweed;
import com.shatteredpixel.shatteredpixeldungeon.plants.Earthroot;
import com.shatteredpixel.shatteredpixeldungeon.plants.Fadeleaf;
import com.shatteredpixel.shatteredpixeldungeon.plants.Firebloom;
import com.shatteredpixel.shatteredpixeldungeon.plants.Icecap;
import com.shatteredpixel.shatteredpixeldungeon.plants.Mageroyal;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.plants.Sorrowmoss;
import com.shatteredpixel.shatteredpixeldungeon.plants.Starflower;
import com.shatteredpixel.shatteredpixeldungeon.plants.Stormvine;
import com.shatteredpixel.shatteredpixeldungeon.plants.Sungrass;
import com.shatteredpixel.shatteredpixeldungeon.plants.Swiftthistle;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class MossyClump extends Trinket {

	{
		image = ItemSpriteSheet.MOSSY_CLUMP;
	}

	@Override
	protected int upgradeEnergyCost() {
		//6 -> 10(16) -> 15(31) -> 20(51)
		return 10+5*level();
	}

	@Override
	public String statsDesc() {
		int lvl = isIdentified() ? buffedLvl() : 0;
		String desc = Messages.get(this, isIdentified() ? "stats_desc" : "typical_stats_desc", plantsPerFloor(lvl));
		if (Dungeon.hero != null && Dungeon.level != null && Dungeon.hero.belongings.getItem(MossyClump.class) == this) {
			desc += "\n\n" + Messages.get(this, "helpful_chance", Math.round(100 * helpfulChance()), TRAMPLE_PENALTY);
		} else {
			desc += "\n\n" + Messages.get(this, "helpful_chance_typical", TRAMPLE_PENALTY);
		}
		return desc;
	}

	public static int plantsPerFloor( int level ){
		return 4 + 2*Math.max(0, level);
	}

	public static final int TRAMPLE_PENALTY = 5;

	private ArrayList<Integer> trampledKeys = new ArrayList<>();
	private ArrayList<Integer> trampledCounts = new ArrayList<>();

	private static int floorKey(){
		return Dungeon.depth + 1000*Dungeon.branch;
	}

	private int trampledOnThisFloor(){
		int i = trampledKeys.indexOf(floorKey());
		return i == -1 ? 0 : trampledCounts.get(i);
	}

	private void addTrampledOnThisFloor(){
		int i = trampledKeys.indexOf(floorKey());
		if (i == -1){
			trampledKeys.add(floorKey());
			trampledCounts.add(1);
		} else {
			trampledCounts.set(i, trampledCounts.get(i) + 1);
		}
	}

	public float helpfulChance(){
		return Math.max(0f, 0.5f - TRAMPLE_PENALTY*trampledOnThisFloor()/100f);
	}

	private static boolean isHelpful( Plant plant ){
		return plant instanceof Sungrass || plant instanceof Earthroot || plant instanceof Swiftthistle
				|| plant instanceof Mageroyal || plant instanceof Starflower;
	}

	private static final Class[] HELPFUL = new Class[]{
			Sungrass.Seed.class, Sungrass.Seed.class, Sungrass.Seed.class,
			Earthroot.Seed.class, Earthroot.Seed.class,
			Swiftthistle.Seed.class, Swiftthistle.Seed.class,
			Mageroyal.Seed.class,
			Starflower.Seed.class
	};

	private static final Class[] HARMFUL = new Class[]{
			Firebloom.Seed.class, Icecap.Seed.class, Sorrowmoss.Seed.class,
			Stormvine.Seed.class, Blindweed.Seed.class, Fadeleaf.Seed.class
	};

	private static Plant.Seed randomSeed( boolean helpful ){
		Class[] pool = helpful ? HELPFUL : HARMFUL;
		try {
			return (Plant.Seed) pool[Random.Int(pool.length)].newInstance();
		} catch (Exception e) {
			return null;
		}
	}

	private static boolean plantable( Level level, int cell ){
		int t = level.map[cell];
		return (t == Terrain.EMPTY || t == Terrain.GRASS || t == Terrain.EMPTY_DECO)
				&& level.passable[cell]
				&& level.heaps.get(cell) == null
				&& level.traps.get(cell) == null
				&& level.plants.get(cell) == null
				&& Actor.findChar(cell) == null
				&& level.findMob(cell) == null
				&& cell != level.entrance() && cell != level.exit();
	}

	private static boolean awayFromTransitions( Level level, int cell ){
		return level.distance(cell, level.entrance()) > 3 && level.distance(cell, level.exit()) > 3;
	}

	public static void populate( Level level ){
		if (!(level instanceof RegularLevel)) return;
		if (Dungeon.hero == null || Dungeon.hero.belongings.getItem(MossyClump.class) == null) return;
		if (Dungeon.isChallenged(Challenges.NO_HERBALISM)) return;

		int lvl = Dungeon.hero.belongings.getItem(MossyClump.class).buffedLvl();

		ArrayList<Integer> cells = new ArrayList<>();
		for (int i = 0; i < level.length(); i++){
			if (plantable(level, i) && awayFromTransitions(level, i)){
				cells.add(i);
			}
		}
		Random.shuffle(cells);

		int patches = plantsPerFloor(lvl) + 2 + lvl;
		for (int i = 0; i < patches && i < cells.size(); i++){
			int c = cells.get(i);
			for (int n : PathFinder.NEIGHBOURS9){
				int cc = c + n;
				if (cc < 0 || cc >= level.length()) continue;
				int t = level.map[cc];
				if ((t == Terrain.EMPTY || t == Terrain.EMPTY_DECO) && level.passable[cc]
						&& (n == 0 || Random.Int(10) < 6)){
					Level.set(cc, Terrain.GRASS, level);
				}
			}
		}

		int planted = 0;
		int starflowers = 0;
		for (int c : cells){
			if (planted >= plantsPerFloor(lvl)) break;
			if (!plantable(level, c)) continue;

			Plant.Seed seed = randomSeed(Random.Int(2) == 0);
			if (seed == null) continue;
			if (seed instanceof Starflower.Seed){
				if (starflowers >= 1){
					seed = new Sungrass.Seed();
				}
				starflowers++;
			}
			level.plant(seed, c);
			planted++;
		}
	}

	public static void onPlantTriggered( Plant plant ){
		if (Dungeon.hero == null || Dungeon.level == null) return;
		MossyClump clump = Dungeon.hero.belongings.getItem(MossyClump.class);
		if (clump == null) return;

		if (isHelpful(plant)){
			clump.addTrampledOnThisFloor();
		}

		clump.regrow(Dungeon.level);
	}

	private void regrow( Level level ){
		if (!(level instanceof RegularLevel) || Dungeon.isChallenged(Challenges.NO_HERBALISM)) return;

		ArrayList<Integer> hidden = new ArrayList<>();
		ArrayList<Integer> any = new ArrayList<>();
		for (int i = 0; i < level.length(); i++){
			if (plantable(level, i) && awayFromTransitions(level, i)){
				any.add(i);
				if (!level.heroFOV[i] && level.distance(i, Dungeon.hero.pos) > 5){
					hidden.add(i);
				}
			}
		}
		ArrayList<Integer> cells = hidden.isEmpty() ? any : hidden;
		if (cells.isEmpty()) return;

		Plant.Seed seed = randomSeed(Random.Float() < helpfulChance());
		if (seed != null){
			level.plant(seed, Random.element(cells));
		}
	}

	private static final String TRAMPLED_KEYS = "trampled_keys";
	private static final String TRAMPLED_COUNTS = "trampled_counts";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		int[] keys = new int[trampledKeys.size()];
		int[] counts = new int[trampledKeys.size()];
		for (int i = 0; i < keys.length; i++){
			keys[i] = trampledKeys.get(i);
			counts[i] = trampledCounts.get(i);
		}
		bundle.put(TRAMPLED_KEYS, keys);
		bundle.put(TRAMPLED_COUNTS, counts);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		trampledKeys.clear();
		trampledCounts.clear();
		if (bundle.contains(TRAMPLED_KEYS)){
			int[] keys = bundle.getIntArray(TRAMPLED_KEYS);
			int[] counts = bundle.getIntArray(TRAMPLED_COUNTS);
			for (int i = 0; i < keys.length && i < counts.length; i++){
				trampledKeys.add(keys[i]);
				trampledCounts.add(counts[i]);
			}
		}
	}
}
