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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Guard;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Ijiraq;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.CultistMask;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.TengusMask;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CloakOfShadows;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndQuest;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndVesk;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret.VeskRoom;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.VeskSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Vesk extends NPC {

	{
		spriteClass = VeskSprite.class;

		properties.add(Property.IMMOVABLE);
	}

	@Override
	protected boolean act() {
		//the first time anyone is on his floor
		if (!Quest.heard && Dungeon.hero != null && Dungeon.hero.isAlive()){
			Quest.heard = true;
			GLog.w( Messages.get(this, "chant") );
		}
		return super.act();
	}

	@Override
	protected Char chooseEnemy() {
		return null;
	}

	@Override
	public int defenseSkill( Char enemy ) {
		//weak enough that a guard usually lands one soon
		return isKeeper(enemy) ? 6 : INFINITE_EVASION;
	}

	@Override
	public void damage( int dmg, Object src ) {
		//one hit from a guard is the end of him, nothing else can touch him
		if (isKeeper(src)){
			killedByGuard((Guard) src);
		}
	}

	//a real prison guard who is still hostile. An ijiraq posing as one doesnt count
	public static boolean isKeeper( Object ch ){
		return ch != null && ch.getClass() == Guard.class
				&& ((Guard) ch).alignment == Char.Alignment.ENEMY;
	}

	private boolean spotted;

	public void spottedBy( Guard guard ){
		if (!spotted){
			spotted = true;
			guard.yell( Messages.get(this, "guard_spots") );
			yell( Messages.get(this, "not_the_keepers") );
		}
	}

	public void killedByGuard( Guard guard ){
		if (Quest.dead) return;
		Quest.dead = true;

		int cell = pos;
		yell( Messages.get(this, "death") );
		HP = 0;
		die( guard );

		GLog.w( Messages.get(this, "emerges") );
		Ijiraq.ambush( cell );

		Heap heap = Dungeon.level.drop( cursedDrop(), cell );
		if (heap.sprite != null) heap.sprite.drop( cell );
	}

	//what he was still holding on to: a cursed weapon or armor of this floor's usual tiers
	private static Item cursedDrop(){
		int level = cursedDropLevel();
		if (Random.Int(2) == 0){
			MeleeWeapon weapon = Generator.randomWeapon(true);
			weapon.level(level);
			weapon.enchant(Weapon.Enchantment.randomCurse());
			weapon.cursed = true;
			return weapon;
		} else {
			Armor armor = Generator.randomArmor();
			armor.level(level);
			armor.inscribe(Armor.Glyph.randomCurse());
			armor.cursed = true;
			return armor;
		}
	}

	//+1 35%, +2 55%, +3 10%
	public static int cursedDropLevel(){
		float roll = Random.Float();
		if (roll < 0.35f) return 1;
		if (roll < 0.90f) return 2;
		return 3;
	}

	private static final String SPOTTED = "spotted";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle(bundle);
		bundle.put(SPOTTED, spotted);
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
		spotted = bundle.getBoolean(SPOTTED);
	}

	@Override
	public boolean add( Buff buff ) {
		return false;
	}

	@Override
	public boolean reset() {
		return true;
	}

	@Override
	public boolean interact(Char c) {
		if (sprite != null) sprite.turnTo( pos, c.pos );
		if (c != Dungeon.hero){
			return super.interact(c);
		}

		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
				if (Quest.dealDone){
					GameScene.show(new WndQuest(Vesk.this, afterDealLine()));
				} else {
					GameScene.show(new WndVesk(Vesk.this));
				}
			}
		});
		return true;
	}

	private String afterDealLine(){
		switch (Quest.deal){
			case MASK:
				boolean waiting = Dungeon.hero.belongings.getItem(CultistMask.class) != null;
				return Messages.get(this, waiting ? "after_mask_waiting" : "after_mask_worn");
			case CLOAK:
				return Messages.get(this, "after_cloak");
			default:
				return Messages.get(this, "after_gear");
		}
	}

	//he only takes gear the player already knows is cursed, plus two special things
	public static boolean accepts( Item item ){
		if (item instanceof TengusMask || item instanceof CloakOfShadows){
			return true;
		}
		if (item.unique || !item.cursed || !item.cursedKnown){
			return false;
		}
		if (item instanceof Armor){
			return !(item instanceof ClassArmor) && ((Armor) item).checkSeal() == null;
		}
		return item instanceof MeleeWeapon || item instanceof Ring
				|| item instanceof Artifact || item instanceof Wand;
	}

	//0 for the offers that pay with something other than gold
	public static int goldFor( Item item ){
		if (item instanceof TengusMask || item instanceof CloakOfShadows){
			return 0;
		}
		int tier;
		if (item instanceof MeleeWeapon){
			tier = ((MeleeWeapon) item).tier;
		} else if (item instanceof Armor){
			tier = ((Armor) item).tier;
		} else {
			return 1500;
		}
		//200 per tier, plus half as much again for each upgrade they know about
		return tier * 100 * (2 + Math.max(0, item.visiblyUpgraded()));
	}

	public static final int GEAR  = 1;
	public static final int MASK  = 2;
	public static final int CLOAK = 3;
	public static final int FAVOR = 2000;

	public static void makeDeal( Hero hero, Item item ){
		if (Quest.dealDone || !accepts(item)){
			return;
		}
		int gold = goldFor(item);
		take(hero, item);
		Quest.dealDone = true;

		if (item instanceof TengusMask){
			Quest.deal = MASK;
			CultistMask mask = new CultistMask();
			if (!mask.collect(hero.belongings.backpack)){
				Dungeon.level.drop(mask, hero.pos);
			}
		} else if (item instanceof CloakOfShadows){
			Quest.deal = CLOAK;
			//before the blacksmith quest is done there is nothing to add it to
			if (Blacksmith.Quest.completed()){
				Blacksmith.Quest.favor += FAVOR;
			} else {
				Quest.heldFavor = FAVOR;
			}
		} else {
			Quest.deal = GEAR;
			pay(hero, gold);
		}
	}

	//removes it for good, even if it is worn and cursed
	private static void take( Hero hero, Item item ){
		if (item instanceof EquipableItem && item.isEquipped(hero)){
			boolean wasCursed = item.cursed;
			item.cursed = false;
			((EquipableItem) item).doUnequip(hero, false, false);
			item.cursed = wasCursed;
			hero.spend(-hero.cooldown());
		} else {
			item.detachAll(hero.belongings.backpack);
		}
		Item.updateQuickslot();
	}

	private static void pay( Hero hero, int gold ){
		Dungeon.gold += gold;
		Statistics.goldCollected += gold;
		Badges.validateGoldCollected();
		Sample.INSTANCE.play( Assets.Sounds.GOLD );
		if (hero.sprite != null){
			hero.sprite.showStatusWithIcon( CharSprite.NEUTRAL, Integer.toString(gold), FloatingText.GOLD );
		}
	}

	public static class Quest {

		public static boolean heard;     //the chanting message has been shown
		public static boolean dealDone;
		public static boolean dead;
		public static int deal;          //0 none, 1 cursed gear, 2 tengu's mask, 3 cloak
		public static boolean met;       //he has introduced himself
		public static int heldFavor;     //owed by the blacksmith once his quest is done

		//one of floors 7-9. It comes from the seed, so picking it doesnt disturb level generation
		public static int floor(){
			return 7 + (int)Math.floorMod(Dungeon.seed / 31, 3L);
		}

		public static ArrayList<Room> spawnRoom( ArrayList<Room> rooms ){
			if (Dungeon.branch == 0 && Dungeon.depth == floor()){
				rooms.add(new VeskRoom());
			}
			return rooms;
		}

		public static void reset(){
			heard = dealDone = dead = met = false;
			deal = heldFavor = 0;
		}

		private static final String NODE      = "vesk";
		private static final String HEARD     = "heard";
		private static final String DEAL_DONE = "deal_done";
		private static final String DEAD      = "dead";
		private static final String DEAL      = "deal";
		private static final String HELD      = "held_favor";
		private static final String MET       = "met";

		public static void storeInBundle( Bundle bundle ){
			Bundle node = new Bundle();
			node.put(HEARD, heard);
			node.put(DEAL_DONE, dealDone);
			node.put(DEAD, dead);
			node.put(DEAL, deal);
			node.put(HELD, heldFavor);
			node.put(MET, met);
			bundle.put(NODE, node);
		}

		public static void restoreFromBundle( Bundle bundle ){
			reset();
			Bundle node = bundle.getBundle(NODE);
			if (!node.isNull()){
				heard = node.getBoolean(HEARD);
				dealDone = node.getBoolean(DEAL_DONE);
				dead = node.getBoolean(DEAD);
				deal = node.getInt(DEAL);
				heldFavor = node.getInt(HELD);
				met = node.getBoolean(MET);
			}
		}
	}
}
