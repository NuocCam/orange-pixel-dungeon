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
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.RegularLevel;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Arrays;

//once taken it stays for the rest of the run, so nothing may get it out of the inventory.
//Its effects add up as it levels
public class SkullChalice extends Trinket {

	{
		image = ItemSpriteSheet.SKULL_CHALICE;
	}

	//keeps the floor bonuses apart from the rolls which build the floor
	private static final long SEED_OFFSET = 0x5C411L;

	@Override
	protected int upgradeEnergyCost() {
		//10 -> 15(25) -> 20(45)
		return 10+5*level();
	}

	@Override
	public String statsDesc() {
		return Messages.get(this, "stats_desc");
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions(hero);
		actions.remove(AC_DROP);
		actions.remove(AC_THROW);
		return actions;
	}

	@Override
	public void doDrop( Hero hero ) {
		//it won't let go
	}

	@Override
	public void doThrow( Hero hero ) {
		//same here
	}

	@Override
	public int energyVal() {
		return 0;
	}

	@Override
	public boolean keptThroughLostInventory() {
		return true;
	}

	//-1 without one
	public static int carriedLevel(){
		return trinketLevel(SkullChalice.class);
	}

	//before the rat skull has its say
	public static float ijiraqChance(){
		int level = carriedLevel();
		if (level < 0)      return .01f;
		else if (level < 3) return .02f;
		else                return .04f;
	}

	//only call this once the upgraded chalice is back in the bag,
	//any sooner and the catalyst could take its spot
	public void onCrafted(){
		if (level() >= 3 && !Dungeon.LimitedDrops.CHALICE_CATA.dropped()){
			Dungeon.LimitedDrops.CHALICE_CATA.drop();
			TrinketCatalyst cata = new TrinketCatalyst();
			if (!cata.collect()){
				Dungeon.level.drop(cata, Dungeon.hero.pos);
			}
		}
	}

	//for freshly made regular floors on the main path. The layout and loot are already settled,
	//and this has its own generator so none of that changes
	public static void afterFloorCreated( Level level ){
		int chalice = carriedLevel();
		if (chalice < 1 || Dungeon.depth > 24) return;

		Random.pushGenerator( Dungeon.seedCurDepth() + SEED_OFFSET );
			if (chalice >= 2) upgradeGear(level);
			addExtraScroll(level);
		Random.popGenerator();
	}

	//gear lying around or in chests. Shop stock and hero remains are left alone
	private static void upgradeGear( Level level ){
		int[] cells = level.heaps.keyArray();
		Arrays.sort(cells);
		for (int cell : cells){
			Heap heap = level.heaps.get(cell);
			if (heap.type == Heap.Type.FOR_SALE || heap.type == Heap.Type.REMAINS) continue;

			for (Item item : heap.items){
				if ((item instanceof Weapon || item instanceof Armor
						|| item instanceof Wand || item instanceof Ring)
						&& item.isUpgradable() && item.trueLevel() == 0
						&& Random.Int(3) < 2){
					//not upgrade(), which can strip enchantments and curses
					item.level(1);
					if (item instanceof Wand) ((Wand) item).curCharges++;
				}
			}
		}
	}

	//one more scroll of upgrade per region, counted apart from the usual three
	private static void addExtraScroll( Level level ){
		int region = (Dungeon.depth-1)/5;
		if ((Dungeon.LimitedDrops.CHALICE_SOU_REGIONS.count & (1 << region)) != 0) return;
		//1 in however many floors the region has left, so the 4th one is certain
		if (Random.Int(4 - (Dungeon.depth-1)%5) != 0) return;

		Dungeon.LimitedDrops.CHALICE_SOU_REGIONS.count |= 1 << region;
		Dungeon.LimitedDrops.CHALICE_SOU.count++;
		//forbidden runes takes every other one of these too, starting with the frist
		if (!Dungeon.isChallenged(Challenges.NO_SCROLLS) || Dungeon.LimitedDrops.CHALICE_SOU.count%2 == 0){
			((RegularLevel) level).dropExtraItem(new ScrollOfUpgrade());
		}
	}

}
