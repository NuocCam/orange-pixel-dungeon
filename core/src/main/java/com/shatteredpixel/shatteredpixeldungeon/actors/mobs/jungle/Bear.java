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
import com.shatteredpixel.shatteredpixeldungeon.items.quest.BearPelt;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.BearSprite;
import com.watabou.utils.Random;

//the biggest thing in the jungle, most days
public class Bear extends JungleAnimal {

	{
		spriteClass = BearSprite.class;

		HP = HT = 25;
		defenseSkill = 5;

		EXP = 4;
		maxLvl = 9;

		lootChance = 0.2f; //by default, see lootChance()
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 2, 7 );
	}

	@Override
	public int attackSkill( Char target ) {
		return 12;
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0, 3);
	}

	@Override
	public int berryTurns() {
		return 6;
	}

	//one pelt a run: half the time from the first bear, otherwise the second one has it for sure
	@Override
	public void rollToDropLoot() {
		if (!prey && !Dungeon.LimitedDrops.BEAR_PELT.dropped()){
			Dungeon.LimitedDrops.BEAR_KILLS.count++;
			if (Dungeon.LimitedDrops.BEAR_KILLS.count >= 2 || Random.Int(2) == 0){
				Dungeon.LimitedDrops.BEAR_PELT.drop();
				Dungeon.level.drop(new BearPelt(), pos).sprite.drop();
			}
		}
		super.rollToDropLoot();
	}

	//weapons are shared with the slimes, each one makes the next 1/4 as likely
	@Override
	public float lootChance() {
		return super.lootChance() * (float)Math.pow(1/4f, Dungeon.LimitedDrops.SLIME_WEP.count);
	}

	@Override
	public Item createLoot() {
		Dungeon.LimitedDrops.SLIME_WEP.count++;
		Item weapon = Generator.randomUsingDefaults(Generator.Category.WEP_T2);
		weapon.level(0);
		return weapon;
	}
}
