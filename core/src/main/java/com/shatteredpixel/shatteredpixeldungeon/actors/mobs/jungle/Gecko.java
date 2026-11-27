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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfParalyticGas;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.GeckoTail;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GeckoSprite;
import com.watabou.utils.Random;

//a rare cousin of the skink with a stunning touch, collectors would love its tail
public class Gecko extends JungleAnimal {

	{
		spriteClass = GeckoSprite.class;

		HP = HT = 14;
		defenseSkill = 12;

		EXP = 3;
		maxLvl = 9;

		loot = GeckoTail.class;
		lootChance = 0.67f;
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

	@Override
	public int berryTurns() {
		return 4;
	}

	@Override
	public boolean small() {
		return true;
	}

	@Override
	public int attackProc( Char enemy, int damage ) {
		tryParalyze(enemy);
		return super.attackProc(enemy, damage);
	}

	@Override
	public int defenseProc( Char enemy, int damage ) {
		//it's the touch that does it, so shooting or throwing things is safe
		if (enemy == Dungeon.hero
				&& !(Dungeon.hero.belongings.attackingWeapon() instanceof MissileWeapon)){
			tryParalyze(enemy);
		}
		return super.defenseProc(enemy, damage);
	}

	private static void tryParalyze( Char ch ){
		if (ch == Dungeon.hero && ch.buff(ParalyzeCooldown.class) == null && Random.Int(5) == 0){
			Buff.prolong(ch, Paralysis.class, 2f);
			Buff.affect(ch, ParalyzeCooldown.class, 3f);
		}
	}

	@Override
	public void rollToDropLoot() {
		super.rollToDropLoot();

		if (Dungeon.hero.lvl > maxLvl + 2) return;

		//the gas it stuns with, which always comes along
		Item potion = new PotionOfParalyticGas();
		Dungeon.level.drop(potion, pos).sprite.drop();
	}

	//on the hero, so a pack of geckos can't keep them stunned forever
	public static class ParalyzeCooldown extends FlavourBuff {}
}
