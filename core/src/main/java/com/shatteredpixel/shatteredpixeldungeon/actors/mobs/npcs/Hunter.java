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

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfExperience;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfMastery;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.BearPelt;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.DireWolfPelt;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.GeckoTail;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.WolfPelt;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTransmutation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfEnchantment;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.JungleRecords;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.levels.JungleLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.WallPath;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HunterSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndHunter;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndQuest;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

//waits by the way down from the jungle and buys what the hero skins along the way
public class Hunter extends NPC {

	{
		spriteClass = HunterSprite.class;

		state = PASSIVE;

		properties.add(Property.IMMOVABLE);
	}

	@Override
	public Notes.Landmark landmark() {
		return Notes.Landmark.HUNTER;
	}

	@Override
	public int defenseSkill( Char enemy ) {
		return INFINITE_EVASION;
	}

	@Override
	protected Char chooseEnemy() {
		return null;
	}

	//a charging boar runs right into him, and it does nothing at all
	@Override
	public void damage( int dmg, Object src ) {
		//do nothing
	}

	@Override
	public boolean add( Buff buff ) {
		return false;
	}

	//no noise gets him up from his spot
	@Override
	public void beckon( int cell ) {
		//do nothing
	}

	@Override
	public boolean reset() {
		return true;
	}

	@Override
	public boolean interact( Char c ) {
		sprite.turnTo( pos, c.pos );

		if (c != Dungeon.hero){
			return true;
		}

		final boolean first = !Quest.met();
		Quest.meet();

		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
				if (first) {
					String speech = Messages.get(Hunter.class, "intro", Messages.titleCase(Dungeon.hero.name()));
					GameScene.show(new WndQuest(Hunter.this, speech){
						@Override
						public void hide() {
							super.hide();
							GameScene.show(new WndHunter(Hunter.this));
						}
					});
				} else {
					GameScene.show(new WndHunter(Hunter.this));
				}
			}
		});

		return true;
	}

	//what he'll give for one of these, the player picks one of them. Empty if he isn't buying
	public static Item[] rewardsFor( Class<? extends Item> pelt ){
		if (pelt == WolfPelt.class){
			return new Item[]{ new Gold(100) };
		} else if (pelt == DireWolfPelt.class){
			return new Item[]{ new PotionOfExperience(), new ScrollOfTransmutation(),
					new ScrollOfEnchantment(), new Gold(300) };
		} else if (pelt == GeckoTail.class){
			return new Item[]{ new PotionOfMastery() };
		} else if (pelt == BearPelt.class && Quest.bearReward != null){
			return new Item[]{ Quest.bearReward.weapon, Quest.bearReward.armor };
		}
		return new Item[0];
	}

	//takes one pelt and hands over the reward, false if there's no deal to be had
	public boolean buy( Class<? extends Item> pelt, Item reward ){
		Item carried = Dungeon.hero.belongings.getItem(pelt);
		if (carried == null || rewardsFor(pelt).length == 0){
			return false;
		}

		if (pelt == BearPelt.class){
			Ghost.Quest.Reward bear = Quest.bearReward;
			if (reward != bear.weapon && reward != bear.armor){
				return false;
			}
			if (reward == bear.weapon && bear.enchant != null){
				bear.weapon.enchant(bear.enchant);
			} else if (reward == bear.armor && bear.glyph != null){
				bear.armor.inscribe(bear.glyph);
			}
			//one or the other, only once
			Quest.bearReward = null;
		}

		carried.detach(Dungeon.hero.belongings.backpack);
		JungleRecords.sold(pelt);
		Badges.validateMasterTrader();

		reward.identify(false);
		if (reward.doPickUp( Dungeon.hero )) {
			if (!(reward instanceof Gold)){
				GLog.i( Messages.capitalize(Messages.get(Dungeon.hero, "you_now_have", reward.name())) );
			}
		} else {
			//not under him, nobody could ever pick it up there
			Dungeon.level.drop( reward, Dungeon.hero.pos ).sprite.drop();
		}
		return true;
	}

	public static class Quest {

		private static boolean met;

		//rolled along with the floor like the ghost's, null once it's been taken
		private static Ghost.Quest.Reward bearReward;

		public static void reset() {
			met = false;
			bearReward = null;
		}

		public static boolean met(){
			return met;
		}

		public static void meet(){
			met = true;
		}

		public static void spawn( JungleLevel level, Room room ) {
			if (Dungeon.depth == 4) {
				Hunter hunter = new Hunter();
				hunter.pos = spot(level, room);
				level.mobs.add( hunter );

				bearReward = Ghost.Quest.rollReward();
			}
		}

		//beside the way down, but never in the mouth of the path
		private static int spot( Level level, Room room ){
			int exit = level.exit();
			int mouth = exit + WallPath.opening(level.map, exit, level.width());

			ArrayList<Integer> free = new ArrayList<>();
			ArrayList<Integer> inRoom = new ArrayList<>();
			for (int offset : PathFinder.NEIGHBOURS8){
				int cell = exit + offset;
				if (cell == mouth || !room.inside(level.cellToPoint(cell))) continue;
				inRoom.add(cell);
				if (level.passable[cell] && !level.solid[cell]
						&& level.heaps.get(cell) == null && level.findMob(cell) == null){
					free.add(cell);
				}
			}
			if (!free.isEmpty()){
				return Random.element(free);
			}
			//something fills both sides, so he clears one of them off
			int cell = Random.element(inRoom);
			Level.set(cell, Terrain.EMPTY, level);
			return cell;
		}

		private static final String NODE    = "hunter";
		private static final String MET     = "met";
		private static final String WEAPON  = "weapon";
		private static final String ARMOR   = "armor";
		private static final String ENCHANT = "enchant";
		private static final String GLYPH   = "glyph";

		public static void storeInBundle( Bundle bundle ) {
			Bundle node = new Bundle();
			node.put( MET, met );
			if (bearReward != null){
				node.put( WEAPON, bearReward.weapon );
				node.put( ARMOR, bearReward.armor );
				if (bearReward.enchant != null) {
					node.put( ENCHANT, bearReward.enchant );
					node.put( GLYPH, bearReward.glyph );
				}
			}
			bundle.put( NODE, node );
		}

		public static void restoreFromBundle( Bundle bundle ) {
			reset();

			Bundle node = bundle.getBundle( NODE );
			if (node.isNull()) return;

			met = node.getBoolean( MET );
			if (node.contains( WEAPON )){
				bearReward = new Ghost.Quest.Reward();
				bearReward.weapon = (Weapon) node.get( WEAPON );
				bearReward.armor = (Armor) node.get( ARMOR );
				if (node.contains( ENCHANT )) {
					bearReward.enchant = (Weapon.Enchantment) node.get( ENCHANT );
					bearReward.glyph = (Armor.Glyph) node.get( GLYPH );
				}
			}
		}
	}
}
