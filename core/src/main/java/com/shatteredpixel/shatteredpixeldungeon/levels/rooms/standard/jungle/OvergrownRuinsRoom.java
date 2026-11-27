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
import com.watabou.utils.Random;

//what's left of something old. Idols and pillars still stand about, more or less in rows
public class OvergrownRuinsRoom extends StandardRoom {

	@Override
	public int minWidth() {
		return Math.max(5, super.minWidth());
	}

	@Override
	public int minHeight() {
		return Math.max(5, super.minHeight());
	}

	@Override
	public float[] sizeCatProbs() {
		return new float[]{2, 2, 1};
	}

	@Override
	public void paint(Level level) {
		Painter.fill( level, this, Terrain.WALL );
		Painter.fill( level, this, 1, Terrain.EMPTY );

		//every other cell, and never against the walls. So there's always a way between
		for (int y = top+2; y <= bottom-2; y += 2){
			for (int x = left+2; x <= right-2; x += 2){
				int cell = x + y*level.width();
				switch (Random.Int(10)){
					case 0: case 1:
						Painter.set( level, cell, Terrain.STATUE );
						break;
					case 2:
						JungleLevel.Decor.addPillar( level, cell );
						break;
					case 3:
						Painter.set( level, cell, Terrain.REGION_DECO );
						break;
					case 4:
						Painter.set( level, cell, Terrain.REGION_DECO_ALT );
						break;
				}
			}
		}

		for (Door door : connected.values()) {
			door.set( Door.Type.REGULAR );
		}
	}

}
