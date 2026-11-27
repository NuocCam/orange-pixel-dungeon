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

import com.shatteredpixel.shatteredpixeldungeon.levels.JungleLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.StandardRoom;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

//a shallow pond with a few dry spots in it, and a bank to walk round on
public class PondRoom extends StandardRoom {

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
		return new float[]{4, 2, 1};
	}

	@Override
	public void paint(Level level) {
		Painter.fill( level, this, Terrain.WALL );
		Painter.fill( level, this, 1, Terrain.EMPTY );
		Painter.fillEllipse( level, this, 2, Terrain.WATER );

		//islands, bigger ponds have more of them
		int islands = 1 + (width()-6)*(height()-6)/12;
		for (int i = 0; i < islands; i++){
			Point p = new Point( Random.IntRange(left+3, right-3), Random.IntRange(top+3, bottom-3) );
			Painter.set( level, p, Terrain.EMPTY );
			if (p.x < right-3 && Random.Int(2) == 0){
				p.x++;
				Painter.set( level, p, Terrain.EMPTY );
			}
		}

		for (Point p : getPoints()){
			int cell = level.pointToCell(p);
			if (level.map[cell] == Terrain.WATER && Random.Int(8) == 0){
				JungleLevel.Decor.addLilyPads( level, cell );
			}
		}

		for (Door door : connected.values()) {
			door.set( Door.Type.REGULAR );
		}
	}

	//the shape is the whole point, so the painter keeps its water out
	@Override
	public boolean canPlaceWater(Point p) {
		return false;
	}

}
