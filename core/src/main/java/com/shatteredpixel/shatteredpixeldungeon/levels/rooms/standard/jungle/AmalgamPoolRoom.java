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
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;

//a row of trees on each side, with deep pools in the corners
public class AmalgamPoolRoom extends AmalgamRoom {

	@Override
	protected void layout( Level level ) {
		int w = (width() == 14 ? 4 : 2) + width()%2;
		int h = (height() == 14 ? 4 : 2) + height()%2;
		int x = width() < 12 ? 2 : 3;
		int y = height() < 12 ? 2 : 3;
		Painter.fill( level, left + (width()-w)/2, top + y, w, 1, Terrain.WALL );
		Painter.fill( level, left + (width()-w)/2, bottom - y, w, 1, Terrain.WALL );
		Painter.fill( level, left + x, top + (height()-h)/2, 1, h, Terrain.WALL );
		Painter.fill( level, right - x, top + (height()-h)/2, 1, h, Terrain.WALL );

		//three cells in each corner, which can still be slipped past diagonally
		for (int dx : new int[]{1, -1}){
			for (int dy : new int[]{1, -1}){
				int cx = dx == 1 ? left+1 : right-1;
				int cy = dy == 1 ? top+1 : bottom-1;
				Painter.set( level, cx, cy, Terrain.DEEP_WATER );
				Painter.set( level, cx+dx, cy, Terrain.DEEP_WATER );
				Painter.set( level, cx, cy+dy, Terrain.DEEP_WATER );
			}
		}
	}
}
