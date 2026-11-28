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

package com.shatteredpixel.shatteredpixeldungeon.items.food;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Healing;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Stamina;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

//one quarter of a jungle berry pie
public class JungleBerryPieSlice extends Food {

	public static final int REMEDY  = 0;
	public static final int GLOW    = 1;
	public static final int RUSH    = 2;

	private static final float RUSH_TURNS = 10f;

	{
		image = ItemSpriteSheet.JUNGLE_PIE_SLICE;
		energy = Hunger.HUNGRY*0.75f; //4 slices feed as much as a meat pie
	}

	@Override
	protected void satisfy(Hero hero) {
		super.satisfy( hero );

		int heal = Math.min( Math.max(1, Math.round(hero.HT * 0.05f)), hero.HT - hero.HP );
		if (heal > 0) {
			hero.HP += heal;
			hero.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString(heal), FloatingText.HEALING );
		}

		//like frozen carpaccio, you don't know what you'll get. Here you always get somthing
		effect( hero, Random.Int(3) );
	}

	public static void effect( Hero hero, int which ) {
		switch (which) {
			case REMEDY:
				GLog.i( Messages.get(JungleBerryPieSlice.class, "remedy") );
				Buff.detach( hero, Poison.class );
				Buff.detach( hero, Bleeding.class );
				Buff.detach( hero, Cripple.class );
				break;
			case GLOW:
				GLog.i( Messages.get(JungleBerryPieSlice.class, "glow") );
				int amount = Math.max(1, Math.round(hero.HT * 0.1f));
				//setHeal only keeps the bigger one, so add on to whatever is already healing us
				Healing healing = hero.buff(Healing.class);
				if (healing != null)    healing.increaseHeal(amount);
				else                    Buff.affect(hero, Healing.class).setHeal(amount, 0, 1);
				break;
			case RUSH:
				GLog.i( Messages.get(JungleBerryPieSlice.class, "rush") );
				Buff.prolong( hero, Stamina.class, RUSH_TURNS );
				break;
		}
	}

	@Override
	public int value() {
		return 12 * quantity;
	}

}
