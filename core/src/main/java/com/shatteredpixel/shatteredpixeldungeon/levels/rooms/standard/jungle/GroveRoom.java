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
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.PatchRoom;
import com.watabou.utils.Point;

//clumps of trees to duck behind
public class GroveRoom extends PatchRoom {

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
		return new float[]{3, 2, 1};
	}

	@Override
	protected float fill() {
		return 0.40f;
	}

	@Override
	protected int clustering() {
		return 2;
	}

	@Override
	protected boolean ensurePath() {
		return connected.size() > 0;
	}

	@Override
	protected boolean cleanEdges() {
		return true;
	}

	@Override
	public void paint(Level level) {
		Painter.fill( level, this, Terrain.WALL );
		Painter.fill( level, this, 1, Terrain.EMPTY );
		for (Door door : connected.values()) {
			door.set( Door.Type.REGULAR );
		}

		setupPatch(level);
		fillPatch(level, Terrain.WALL);
		//cleared all round the edge, so the trees stand on their own out in the open
		Painter.drawLine( level, new Point(left+1, top+1), new Point(right-1, top+1), Terrain.EMPTY );
		Painter.drawLine( level, new Point(left+1, bottom-1), new Point(right-1, bottom-1), Terrain.EMPTY );
		Painter.drawLine( level, new Point(left+1, top+1), new Point(left+1, bottom-1), Terrain.EMPTY );
		Painter.drawLine( level, new Point(right-1, top+1), new Point(right-1, bottom-1), Terrain.EMPTY );
	}

}
