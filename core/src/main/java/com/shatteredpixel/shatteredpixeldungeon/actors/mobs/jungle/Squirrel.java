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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SquirrelSprite;
import com.watabou.utils.Random;

//no threat to anyone, but it might be sitting on something good
public class Squirrel extends JungleAnimal {

	{
		spriteClass = SquirrelSprite.class;

		HP = HT = 4;
		defenseSkill = 15;
		baseSpeed = 2f;

		EXP = 0;

		lootChance = 0.5f; //see createLoot

		FLEEING = new Running();

		immunities.add(ChampionEnemy.class);
	}

	@Override
	protected Char chooseEnemy() {
		Char enemy = super.chooseEnemy();
		if (Dungeon.hero.invisible <= 0 && fieldOfView[Dungeon.hero.pos]){
			state = FLEEING;
			return Dungeon.hero;
		}
		//it never stands and fights, whatever set it off
		if (state == HUNTING){
			state = FLEEING;
		}
		return enemy;
	}

	@Override
	protected boolean canAttack( Char enemy ) {
		return false;
	}

	@Override
	public int damageRoll() {
		return 0;
	}

	@Override
	public int attackSkill( Char target ) {
		return 0;
	}

	@Override
	public int berryTurns() {
		return 0;
	}

	@Override
	public boolean small() {
		return true;
	}

	//they don't take up a spot on the floor's enemy limit
	@Override
	public float spawningWeight() {
		return 0;
	}

	@Override
	public Item createLoot() {
		//its little stash, a seed or a few coins
		return Random.Int(2) == 0
				? Generator.randomUsingDefaults(Generator.Category.SEED)
				: new Gold(Random.IntRange(15, 30));
	}

	private class Running extends Fleeing {
		@Override
		protected void escaped() {
			state = WANDERING;
			target = Dungeon.level.randomDestination(Squirrel.this);
		}

		@Override
		protected void nowhereToRun() {
			//cornered, it just cowers
		}
	}
}
