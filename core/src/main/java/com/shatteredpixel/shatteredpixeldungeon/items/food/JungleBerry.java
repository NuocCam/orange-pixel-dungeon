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

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.journal.JungleRecords;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.BerryCharm;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.jungle.JungleAnimal;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;

//the jungle's answer to rations, one grows on each berry bush
public class JungleBerry extends Food {

	{
		image = ItemSpriteSheet.JUNGLE_BERRY;
		energy = Hunger.HUNGRY; //same as a ration
	}

	//a jungle animal would much rather eat than fight whoever threw it
	@Override
	protected void onThrow( int cell ) {
		Char ch = Actor.findChar(cell);
		if (ch instanceof JungleAnimal){
			JungleAnimal animal = (JungleAnimal) ch;
			if (animal.berryTurns() > 0 && !animal.prey && animal.alignment == Char.Alignment.ENEMY
					&& !animal.isImmune(BerryCharm.class)){
				Buff.affect(animal, BerryCharm.class).set(animal.berryTurns());
				JungleRecords.wonOver(animal.getClass());
				Badges.validateWildCompanion();
				Sample.INSTANCE.play(Assets.Sounds.EAT);
				return;
			}
		}
		super.onThrow(cell);
	}

}
