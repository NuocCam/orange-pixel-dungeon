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

package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.jungle;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.WallPath;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.entrance.EntranceRoom;

//the way up is a path leading back into the trees
public class JungleEntranceRoom extends EntranceRoom {

	private boolean noSpot = false;

	@Override
	public void paint(Level level) {
		//the regular room does everything else, guide pages included. Its transition is
		// the next one added, which we then move into the wall
		int added = level.transitions.size();
		super.paint(level);
		noSpot = !WallPath.moveTransition(level, this, added);
	}

	@Override
	public boolean canFailPaint() {
		return true;
	}

	@Override
	public boolean paintFailed() {
		return noSpot;
	}
}
