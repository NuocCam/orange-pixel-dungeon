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

package com.shatteredpixel.shatteredpixeldungeon.levels.features;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.RegularLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Collection;

//a path set into a wall: a single walkable cell with wall on three sides of it.
// In the jungle this is how the way up and down look, a gap in the trees.
public class WallPath {

	//picks a spot on the room's wall to cut a path into, -1 if there's nowhere for one.
	//The room has to be painted already, and its doors placed. The top wall is best,
	//as the path can be seen head on there, then the sides, and the bottom comes last.
	public static int pick( Level level, Room room ){
		Collection<Room> others = otherRooms( level, room );

		ArrayList<Integer> top = new ArrayList<>();
		ArrayList<Integer> sides = new ArrayList<>();
		ArrayList<Integer> bottom = new ArrayList<>();

		//corners are left out of both loops
		for (int x = room.left+1; x < room.right; x++){
			consider( level, room, others, new Point(x, room.top), 0, -1, top );
			consider( level, room, others, new Point(x, room.bottom), 0, +1, bottom );
		}
		for (int y = room.top+1; y < room.bottom; y++){
			consider( level, room, others, new Point(room.left, y), -1, 0, sides );
			consider( level, room, others, new Point(room.right, y), +1, 0, sides );
		}

		if (!top.isEmpty())     return Random.element( top );
		if (!sides.isEmpty())   return Random.element( sides );
		if (!bottom.isEmpty())  return Random.element( bottom );
		return -1;
	}

	//adds p to the list if a path can go there. dx and dy point out of the room
	private static void consider( Level level, Room room, Collection<Room> others, Point p,
	                              int dx, int dy, ArrayList<Integer> spots ){
		int cell = level.pointToCell( p );
		if (!level.insideMap( cell )) return;

		//the wall carries on either side, and there's floor in front
		int along = dx == 0 ? 1 : level.width();
		int out = dx + dy*level.width();
		if (!wall( level.map[cell - along] ) || !wall( level.map[cell + along] )) return;
		if ((Terrain.flags[level.map[cell - out]] & Terrain.PASSABLE) == 0) return;

		//no doors beside it, not even diagonally
		for (Room.Door door : room.connected.values()){
			if (door != null && Math.abs(door.x - p.x) <= 1 && Math.abs(door.y - p.y) <= 1){
				return;
			}
		}

		//the three cells behind it can't be part of any other room. This keeps us off shared
		// walls, and stops a doorway there from leading in diagonally
		for (int i = -1; i <= 1; i++){
			int x = p.x + dx + (dx == 0 ? i : 0);
			int y = p.y + dy + (dy == 0 ? i : 0);
			for (Room r : others){
				//room edges count too, so we don't use Room.inside here
				if (x >= r.left && x <= r.right && y >= r.top && y <= r.bottom){
					return;
				}
			}
		}

		spots.add( cell );
	}

	private static Collection<Room> otherRooms( Level level, Room room ){
		ArrayList<Room> others = new ArrayList<>();
		if (level instanceof RegularLevel){
			others.addAll( ((RegularLevel) level).rooms() );
		} else {
			others.addAll( room.neigbours );
		}
		others.remove( room );
		return others;
	}

	//turns the cell picked above into the given terrain, closing in whatever is behind it
	public static void cut( Level level, Room room, int cell, int terrain ){
		Point p = level.cellToPoint( cell );
		int dx = p.x == room.left ? -1 : p.x == room.right ? +1 : 0;
		int dy = p.y == room.top  ? -1 : p.y == room.bottom ? +1 : 0;

		//floors with a chasm feeling start out as all chasm, the path musn't open onto that
		for (int i = -1; i <= 1; i++){
			int behind = cell + dx + dy*level.width() + i*(dx == 0 ? 1 : level.width());
			if (level.map[behind] == Terrain.CHASM){
				Painter.set( level, behind, Terrain.WALL );
			}
		}

		Painter.set( level, cell, terrain );
	}

	//takes the stairs a room just painted out in the open and moves them into its wall.
	// index is the room's transition in level.transitions. False if there's no spot for it.
	public static boolean moveTransition( Level level, Room room, int index ){
		int cell = pick( level, room );
		if (cell == -1) return false;

		LevelTransition old = level.transitions.get( index );
		int terrain = level.map[old.cell()];
		Painter.set( level, old.cell(), Terrain.EMPTY );
		cut( level, room, cell, terrain );
		level.transitions.set( index, new LevelTransition( level, cell, old.type,
				old.destDepth, old.destBranch, old.destType ));
		return true;
	}

	//the way a path in a wall opens,as the offset to the one neighbour that isn't wall.
	// That's +width for a path in a top wall, +1 in a left wall, -1 in a right wall and
	// -width in a bottom wall. 0 means the cell isn't set into a wall.
	public static int opening( int[] map, int cell, int width ){
		int x = cell % width;
		if (x == 0 || x == width-1 || cell < width || cell >= map.length - width){
			return 0;
		}

		int result = 0;
		//spelled out so we don't depned on PathFinder having this map's size
		for (int offset : new int[]{ -width, -1, +1, +width }){
			if (!wall( map[cell + offset] )){
				if (result != 0) return 0;
				result = offset;
			}
		}
		return result;
	}

	public static boolean isInWall( Level level, int cell ){
		return opening( level.map, cell, level.width() ) != 0;
	}

	private static boolean wall( int terrain ){
		return terrain == Terrain.WALL || terrain == Terrain.WALL_DECO
				|| terrain == Terrain.SECRET_DOOR || terrain == Terrain.BOOKSHELF;
	}

}
