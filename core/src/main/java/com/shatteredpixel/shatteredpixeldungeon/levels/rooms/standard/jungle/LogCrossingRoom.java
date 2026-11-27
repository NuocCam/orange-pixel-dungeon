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

import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.StandardRoom;
import com.watabou.utils.Point;

//deep water in the middle with one narrow way over it. The walkway goes all the way
// round, so crossing is only ever a shortcut
public class LogCrossingRoom extends StandardRoom {

	@Override
	public int minWidth() {
		return Math.max(7, super.minWidth());
	}

	@Override
	public int minHeight() {
		return Math.max(7, super.minHeight());
	}

	@Override
	public float[] sizeCatProbs() {
		return new float[]{3, 1, 0};
	}

	@Override
	public void paint(Level level) {
		Painter.fill( level, this, Terrain.WALL );
		Painter.fill( level, this, 1, Terrain.EMPTY );
		Painter.fill( level, this, 2, Terrain.DEEP_WATER );

		//the crossing runs the long way
		Point c = center();
		if (width() >= height()){
			Painter.drawLine( level, new Point(left+2, c.y), new Point(right-2, c.y), Terrain.EMPTY );
		} else {
			Painter.drawLine( level, new Point(c.x, top+2), new Point(c.x, bottom-2), Terrain.EMPTY );
		}

		for (Door door : connected.values()) {
			door.set( Door.Type.REGULAR );
		}
	}

	private boolean pastWalkway(Point p){
		return p.x > left+1 && p.x < right-1 && p.y > top+1 && p.y < bottom-1;
	}

	@Override
	public boolean canPlaceWater(Point p) {
		return !pastWalkway(p);
	}

	@Override
	public boolean canPlaceGrass(Point p) {
		return !pastWalkway(p);
	}

}
