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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ToadSprite;
import com.watabou.utils.Random;

//slow to kill, and its skin is nothing you want to touch
public class Toad extends JungleAnimal {

	{
		spriteClass = ToadSprite.class;

		HP = HT = 30;
		defenseSkill = 3;

		EXP = 3;
		maxLvl = 9;

		loot = PotionOfHealing.class;
		lootChance = 1/6f; //by default, see lootChance()
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 1, 4 );
	}

	@Override
	public int attackSkill( Char target ) {
		return 10;
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0, 4);
	}

	@Override
	public int berryTurns() {
		return 20;
	}

	@Override
	public boolean small() {
		return true;
	}

	@Override
	public int defenseProc( Char enemy, int damage ) {
		//only whoever gets right up close can touch it
		if (Dungeon.level.adjacent(pos, enemy.pos)
				&& !(enemy == Dungeon.hero && Dungeon.hero.belongings.attackingWeapon() instanceof MissileWeapon)
				&& Random.Float() < 0.25f){
			Buff.affect(enemy, Poison.class).set(3);
		}
		return super.defenseProc(enemy, damage);
	}

	//healing potions are rationed along with the ones the fly swarms drop
	@Override
	public float lootChance() {
		return super.lootChance() * (5f - Dungeon.LimitedDrops.SWARM_HP.count) / 5f;
	}

	@Override
	public Item createLoot() {
		Dungeon.LimitedDrops.SWARM_HP.count++;
		return super.createLoot();
	}
}
