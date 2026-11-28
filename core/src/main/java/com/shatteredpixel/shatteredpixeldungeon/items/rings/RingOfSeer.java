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

package com.shatteredpixel.shatteredpixeldungeon.items.rings;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class RingOfSeer extends Ring {

	{
		icon = ItemSpriteSheet.Icons.RING_SEER;
		buffClass = Insight.class;
	}

	public static float damageFactor( Hero hero, Char enemy ){
		if (enemy instanceof Mob && ((Mob) enemy).surprisedBy(hero)){
			return multiplier(getBuffedBonusF(hero, Insight.class));
		}
		return 1f;
	}

	//+20% at +0 and 6% per upgrade, a cursed one takes damage away but never more then half
	private static float multiplier( float bonus ){
		if (bonus > 0)  return 1.14f + 0.06f*bonus;
		else            return Math.max(0.5f, 1f + 0.10f*bonus);
	}

	private static String percent( float bonus ){
		return Messages.decimalFormat("#.##", 100f * (multiplier(bonus) - 1f));
	}

	@Override
	public String statsInfo() {
		if (isIdentified()){
			String info = Messages.get(this, "stats", percent(soloBuffedBonus()));

			if (isEquipped(Dungeon.hero) && soloBuffedBonus() != combinedBuffedBonusF(Dungeon.hero)){
				info += "\n\n" + Messages.get(this, "combined_stats", percent(combinedBuffedBonusF(Dungeon.hero)));
			}

			return info;
		} else {
			return Messages.get(this, "typical_stats", percent(1));
		}
	}

	@Override
	public String upgradeStat1(int level){
		if (cursed && cursedKnown) level = Math.min(-1, level-3);
		return percent(level+1) + "%";
	}

	@Override
	protected RingBuff buff( ) {
		return new Insight();
	}

	public class Insight extends RingBuff {}

}
