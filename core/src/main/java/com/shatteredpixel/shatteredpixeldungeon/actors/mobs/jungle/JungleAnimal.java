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

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.watabou.utils.Bundle;

//the beasts of the jungle. A thrown berry buys their friendship for a little while
public abstract class JungleAnimal extends Mob {

	//food for something bigger, too busy being hunted to care about berries
	public boolean prey = false;

	//how long it stays on your side while eating a berry
	public abstract int berryTurns();

	//small ones slip right between the stalks
	public boolean small(){
		return false;
	}

	@Override
	public boolean slowedByTallGrass() {
		return !small() && super.slowedByTallGrass();
	}

	private static final String PREY = "prey";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle(bundle);
		bundle.put(PREY, prey);
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
		prey = bundle.getBoolean(PREY);
	}
}
