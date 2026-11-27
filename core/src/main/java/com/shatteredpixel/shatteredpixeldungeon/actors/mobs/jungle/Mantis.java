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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MantisSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

//sits among the stalks, and you won't know it's there until you're right on top of it
public class Mantis extends JungleAnimal {

	{
		spriteClass = MantisSprite.class;

		HP = HT = 9;
		defenseSkill = 6;

		EXP = 4;
		maxLvl = 9;
	}

	private boolean moved = false;
	//waited out of sight and hasn't landed a hit since, the next one hurts more
	private boolean lurking = false;

	@Override
	public boolean hidden(){
		if (moved || alignment != Alignment.ENEMY || Dungeon.level == null
				|| Dungeon.level.adjacent(pos, Dungeon.hero.pos)){
			return false;
		}
		int terrain = Dungeon.level.map[pos];
		return terrain == Terrain.HIGH_GRASS || terrain == Terrain.FURROWED_GRASS;
	}

	@Override
	protected boolean act() {
		moved = false;
		boolean result = super.act();
		if (hidden()){
			lurking = true;
		}
		return result;
	}

	@Override
	public void move( int step, boolean travelling ) {
		int from = pos;
		super.move(step, travelling);
		if (pos != from){
			moved = true;
			lurking = false;
		}
	}

	//it climbs between the stalks rather than pushing through them
	@Override
	public boolean flattensGrass() {
		return false;
	}

	@Override
	public int damageRoll() {
		int damage = Random.NormalIntRange( 2, 8 );
		return lurking ? Math.round(damage * 1.5f) : damage;
	}

	@Override
	public int attackProc( Char enemy, int damage ) {
		lurking = false;
		return super.attackProc(enemy, damage);
	}

	@Override
	public int attackSkill( Char target ) {
		return 12;
	}

	//insects have no use for fruit
	@Override
	public int berryTurns() {
		return 0;
	}

	private static final String MOVED   = "moved";
	private static final String LURKING = "lurking";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle(bundle);
		bundle.put(MOVED, moved);
		bundle.put(LURKING, lurking);
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
		moved = bundle.getBoolean(MOVED);
		lurking = bundle.getBoolean(LURKING);
	}
}
