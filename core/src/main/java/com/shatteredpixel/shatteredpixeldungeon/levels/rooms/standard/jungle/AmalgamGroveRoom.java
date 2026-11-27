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

//four clumps of trees around the middle
public class AmalgamGroveRoom extends AmalgamRoom {

	@Override
	protected void layout( Level level ) {
		int x = width() < 12 ? 2 : 3;
		int y = height() < 12 ? 2 : 3;
		for (int cx : new int[]{left + x, right - x - 1}){
			for (int cy : new int[]{top + y, bottom - y - 1}){
				Painter.fill( level, cx, cy, 2, 2, Terrain.WALL );
			}
		}
	}
}
