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

package com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CultistPower;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

//a cultist cleric casts hex from their dark tome. It costs hp, not charges
public class HexSpell extends TargetedClericSpell {

	public static final HexSpell INSTANCE = new HexSpell();

	@Override
	public float chargeUse( Hero hero ) {
		return 0;
	}

	@Override
	public boolean canCast( Hero hero ) {
		return hero.subClass == HeroSubClass.CULTIST && hero.HP > CultistPower.cost(hero);
	}

	@Override
	public int icon() {
		return HeroIcon.HEX_SPELL;
	}

	//only needs to see the enemy, so nothing standing in the way gets hexed instead
	@Override
	public int targetingFlags() {
		return Ballistica.STOP_TARGET;
	}

	@Override
	public String shortDesc() {
		return Messages.get(this, "short_desc") + " " + hpCost();
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc") + "\n\n" + hpCost();
	}

	private String hpCost(){
		return Messages.get(this, "hp_cost", CultistPower.cost(Dungeon.hero));
	}

	@Override
	protected void onTargetSelected( HolyTome tome, Hero hero, Integer target ) {
		if (target == null) return;

		if (CultistPower.cast(hero, Actor.findChar(target))){
			onSpellCast(tome, hero);
		} else {
			GLog.w(Messages.get(CultistPower.class, canCast(hero) ? "bad_target" : "no_hp"));
		}
	}
}
