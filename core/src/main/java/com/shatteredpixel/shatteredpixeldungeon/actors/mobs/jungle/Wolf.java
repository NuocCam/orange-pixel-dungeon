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

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.jungle;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.WolfPelt;
import com.shatteredpixel.shatteredpixeldungeon.sprites.WolfSprite;
import com.watabou.utils.Random;

//hunts anything it thinks it can bring down, you included
public class Wolf extends JungleAnimal {

	{
		spriteClass = WolfSprite.class;

		HP = HT = 15;
		defenseSkill = 5;

		EXP = 3;
		maxLvl = 9;
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 2, 9 );
	}

	@Override
	public int attackSkill( Char target ) {
		return 12;
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0, 2);
	}

	@Override
	public int berryTurns() {
		return 16;
	}

	//the pelt goes by its own odds, however strong the hero is and whatever they're wearing
	@Override
	public void rollToDropLoot() {
		if (!prey && Dungeon.LimitedDrops.WOLF_PELT.count < 3 && Random.Int(3) < 2){
			Dungeon.LimitedDrops.WOLF_PELT.count++;
			Dungeon.level.drop(new WolfPelt(), pos).sprite.drop();
		}
		super.rollToDropLoot();
	}
}
