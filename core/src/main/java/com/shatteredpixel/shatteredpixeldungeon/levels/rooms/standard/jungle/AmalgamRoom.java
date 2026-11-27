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

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.jungle.Amalgam;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.StandardRoom;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

//Amalgam's clearing. Every layout has trees to charge into, and keeps a clear way
// round the edge so no door gets cut off
public abstract class AmalgamRoom extends StandardRoom {

	@Override
	public float[] sizeCatProbs() {
		return new float[]{0, 1, 0};
	}

	@Override
	public boolean canMerge(Level l, Room other, Point p, int mergeTerrain) {
		return false;
	}

	public static AmalgamRoom randomArena(){
		return Random.Int(2) == 0 ? new AmalgamGroveRoom() : new AmalgamPoolRoom();
	}

	@Override
	public void paint( Level level ) {
		Painter.fill( level, this, Terrain.WALL );
		Painter.fill( level, this, 1, Terrain.EMPTY );

		layout( level );

		for (Door door : connected.values()) {
			door.set( Door.Type.REGULAR );
			Painter.drawInside( level, this, door, 1, Terrain.EMPTY );
		}

		Amalgam boss = new Amalgam();
		boss.pos = level.pointToCell( center() );
		level.mobs.add( boss );
	}

	//the trees, and anything else out in the clearing
	protected abstract void layout( Level level );
}
