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
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.WallPath;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.exit.ExitRoom;
import com.watabou.utils.Point;

//the gate on the way to floor 6. The locked exit only has art for a top wall, so that's
// the only wall it ever goes in
public class JungleBossExitRoom extends ExitRoom {

	private boolean noSpot = false;

	@Override
	public int minWidth() {
		return Math.max(super.minWidth(), 7);
	}

	@Override
	public int minHeight() {
		return Math.max(super.minHeight(), 7);
	}

	//no doors up there, the gate needs it
	@Override
	public boolean canConnect( Point p ) {
		return super.canConnect(p) && p.y != top;
	}

	@Override
	public void paint(Level level) {
		Painter.fill( level, this, Terrain.WALL );
		Painter.fill( level, this, 1, Terrain.EMPTY );

		for (Door door : connected.values()) {
			door.set( Door.Type.REGULAR );
		}

		//a side wall only comes up if nowhere along the top will do, then we try again
		int cell = WallPath.pick( level, this );
		noSpot = cell == -1 || level.cellToPoint( cell ).y != top;
		if (!noSpot){
			WallPath.cut( level, this, cell, Terrain.LOCKED_EXIT );
			level.transitions.add( new LevelTransition( level, cell, LevelTransition.Type.REGULAR_EXIT ) );
		}
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
