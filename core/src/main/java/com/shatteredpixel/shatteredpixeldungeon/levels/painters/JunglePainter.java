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

package com.shatteredpixel.shatteredpixeldungeon.levels.painters;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;

import java.util.ArrayList;

//the sewers' painter, then everything which can drop or trap you is swept out. Rooms
// borowed from the rest of the game don't need to know about it.
public class JunglePainter extends SewerPainter {

	@Override
	public boolean paint(Level level, ArrayList<Room> rooms) {
		if (!super.paint(level, rooms)){
			return false;
		}

		for (int i = 0; i < level.length(); i++){
			switch (level.map[i]){
				case Terrain.CHASM:
					level.map[i] = Terrain.DEEP_WATER;
					break;
				case Terrain.TRAP:
				case Terrain.SECRET_TRAP:
				case Terrain.INACTIVE_TRAP:
					level.map[i] = Terrain.EMPTY;
					break;
			}
		}
		level.traps.clear();

		return true;
	}
}
