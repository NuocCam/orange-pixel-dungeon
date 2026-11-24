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

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class DimensionalSundial extends Trinket {

	{
		image = ItemSpriteSheet.SUNDIAL;
	}

	@Override
	protected int upgradeEnergyCost() {
		//6 -> 6(12) -> 8(20) -> 10(30)
		return 6+2*level();
	}

	@Override
	public String statsDesc() {
		int lvl = isIdentified() ? buffedLvl() : 0;
		return Messages.get(this,
				isIdentified() ? "stats_desc" : "typical_stats_desc",
				Messages.decimalFormat("#.#", 100*bigChange(lvl)),
				Messages.decimalFormat("#.#", 100*smallChange(lvl)));
	}

	public static boolean isNight(){
		Calendar cal = GregorianCalendar.getInstance();
		return cal.get(Calendar.HOUR_OF_DAY) >= 20 || cal.get(Calendar.HOUR_OF_DAY) <= 7;
	}

	public static float bigChange( int level ){
		return 0.05f + 0.10f*Math.max(0, level)/3f;
	}

	public static float smallChange( int level ){
		return 0.10f*Math.max(0, level)/3f;
	}

	public static float enemyHealthMultiplier(){
		int level = trinketLevel(DimensionalSundial.class);
		if (level == -1) return 1f;
		return isNight() ? 1f - bigChange(level) : 1f + smallChange(level);
	}

	public static float enemyDamageMultiplier(){
		int level = trinketLevel(DimensionalSundial.class);
		if (level == -1) return 1f;
		return isNight() ? 1f + smallChange(level) : 1f - bigChange(level);
	}

	public static void applyToNewMob( Mob mob ){
		float mult = enemyHealthMultiplier();
		if (mult == 1f || mob.properties().contains(Char.Property.BOSS) || mob.properties().contains(Char.Property.MINIBOSS)) return;
		mob.HT = Math.max(1, Math.round(mob.HT*mult));
		mob.HP = Math.max(1, Math.round(mob.HP*mult));
	}

	public static void announceTime(){
		if (trinketLevel(DimensionalSundial.class) == -1) return;
		if (isNight()){
			GLog.w(Messages.get(DimensionalSundial.class, "night"));
		} else {
			GLog.i(Messages.get(DimensionalSundial.class, "day"));
		}
	}
}
