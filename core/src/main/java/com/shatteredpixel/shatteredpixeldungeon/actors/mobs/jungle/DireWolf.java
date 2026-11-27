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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.sprites.DireWolfSprite;
import com.watabou.utils.Random;

//bigger and far quicker than the rest of the pack
public class DireWolf extends JungleAnimal {

	{
		spriteClass = DireWolfSprite.class;

		HP = HT = 22;
		defenseSkill = 8;
		baseSpeed = 2f;

		EXP = 5;
		maxLvl = 9;
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 1, 6 );
	}

	@Override
	public int attackSkill( Char target ) {
		return 12;
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0, 2);
	}

	//two bites in the time others take for one
	@Override
	public float attackDelay() {
		return super.attackDelay() * 0.5f;
	}

	@Override
	public int attackProc( Char enemy, int damage ) {
		if (Random.Int(3) == 0){
			Buff.affect(enemy, Bleeding.class).set(3);
		}
		return super.attackProc(enemy, damage);
	}

	@Override
	public int berryTurns() {
		return 4;
	}
}
