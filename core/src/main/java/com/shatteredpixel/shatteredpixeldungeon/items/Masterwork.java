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

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Blacksmith;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;

//the troll blacksmith's best work: a one time boost to a weapon or armor, stronger on low tiers
public class Masterwork {

	public static final int COST = 3000;

	//percents for tiers 1 to 5: damage, accuracy, attack speed, defense, evasion
	private static final int[][] BONUS = {
			{ 12, 15, 8, 25, 12 },
			{ 11, 13, 7, 22, 11 },
			{ 10, 12, 6, 20, 10 },
			{  8, 10, 5, 17,  9 },
			{  7,  8, 4, 15,  8 }
	};

	public static float damage( int tier ){
		return BONUS[tier-1][0] / 100f;
	}

	public static float accuracy( int tier ){
		return BONUS[tier-1][1] / 100f;
	}

	public static float speed( int tier ){
		return BONUS[tier-1][2] / 100f;
	}

	public static float defense( int tier ){
		return BONUS[tier-1][3] / 100f;
	}

	public static float evasion( int tier ){
		return BONUS[tier-1][4] / 100f;
	}

	public static boolean has( Item item ){
		return (item instanceof Weapon && ((Weapon) item).masterwork)
				|| (item instanceof Armor && ((Armor) item).masterwork);
	}

	//the spirit bow has no tier of its own, it counts as tier 3
	private static int tier( Item item ){
		if (item instanceof MeleeWeapon)    return ((MeleeWeapon) item).tier;
		if (item instanceof Armor)          return ((Armor) item).tier;
		return 3;
	}

	//these are all 1 for items without it
	public static float damageMulti( Item item ){
		return has(item) ? 1 + damage(tier(item)) : 1;
	}

	public static float accuracyMulti( Item item ){
		return has(item) ? 1 + accuracy(tier(item)) : 1;
	}

	public static float speedMulti( Item item ){
		return has(item) ? 1 + speed(tier(item)) : 1;
	}

	public static float defenseMulti( Item item ){
		return has(item) ? 1 + defense(tier(item)) : 1;
	}

	public static float evasionMulti( Item item ){
		return has(item) ? 1 + evasion(tier(item)) : 1;
	}

	//the line in the item's info window
	public static String info( Item item ){
		int[] row = BONUS[tier(item)-1];
		if (item instanceof Armor){
			return Messages.get(Armor.class, "masterwork", row[3], row[4]);
		} else {
			return Messages.get(Weapon.class, "masterwork", row[0], row[1], row[2]);
		}
	}

	public static boolean allowed( Item item ){
		return (item instanceof MeleeWeapon || item instanceof SpiritBow || item instanceof Armor)
				&& item.isIdentified() && !item.cursed && !has(item);
	}

	public static boolean affordable(){
		return Blacksmith.Quest.favor >= COST;
	}

	//expects an allowed item
	public static void forge( Item item ){
		if (item instanceof Armor){
			((Armor) item).masterwork = true;
		} else {
			((Weapon) item).masterwork = true;
		}
		Blacksmith.Quest.favor -= COST;
	}

	//double border and a little spiral in the bottom left corner of the item slot,
	// the bottom right already holds the +level text
	public static class SlotMark extends Component {

		//outer ring first, the inner one is a bit see-through
		private static final int[] RINGS = { 0xFFE46222, 0xBEA6321C };

		//top, bottom, left and right for each ring
		private ColorBlock[] edges;
		private Image spiral;

		@Override
		protected void createChildren() {
			edges = new ColorBlock[4*RINGS.length];
			for (int i = 0; i < edges.length; i++){
				edges[i] = new ColorBlock(1, 1, RINGS[i/4]);
				add(edges[i]);
			}
			spiral = new Image(Assets.Interfaces.MASTERWORK);
			add(spiral);
		}

		@Override
		protected void layout() {
			for (int in = 0; in < RINGS.length; in++){
				ColorBlock top = edges[4*in], bottom = edges[4*in+1], left = edges[4*in+2], right = edges[4*in+3];
				float w = width - 2*in, h = height - 2*in;

				top.x = bottom.x = left.x = x + in;
				right.x = x + in + w - 1;
				top.y = y + in;
				bottom.y = y + in + h - 1;
				top.size(w, 1);
				bottom.size(w, 1);

				//the sides stop short of the corners, so the see-through ring doesn't double up
				left.y = right.y = y + in + 1;
				left.size(1, h - 2);
				right.size(1, h - 2);
			}

			spiral.x = x + 1;
			spiral.y = y + height - spiral.height - 1;
		}

		public void alpha( float value ){
			for (ColorBlock edge : edges) edge.alpha(value);
			spiral.alpha(value);
		}
	}
}
