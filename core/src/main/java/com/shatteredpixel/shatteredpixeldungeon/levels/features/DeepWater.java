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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.MagicalFireRoom;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;

import java.util.ArrayList;

//takes the place of pits where there mustn't be any. Only things which float can be over
// it, and nothing ever falls
public class DeepWater {

	public static boolean blocks( Char ch, int cell ){
		return !ch.flying && Dungeon.level.map[cell] == Terrain.DEEP_WATER;
	}

	//puts a charcter who is on foot in deep water onto the nearest free floor
	public static void expel( Char ch ){
		if (!blocks( ch, ch.pos )) return;

		int cell = nearest( Dungeon.level, ch.pos, ch );
		if (cell == -1) return;

		ch.pos = cell;
		if (ch.sprite != null){
			ch.sprite.place( cell );
		}
		if (ch == Dungeon.hero && ShatteredPixelDungeon.scene() instanceof GameScene){
			Dungeon.observe();
			GameScene.updateFog();
		}
	}

	//where something dropped into deep water at this cell washes up, -1 if nowhere.
	// This can be a level which is still being built
	public static int shore( Level level, int cell ){
		return nearest( level, cell, null );
	}

	//searches outward ring by ring through anything which isn't a wall, so we never end
	// up behind one. A null character means we're looking for a spot for an item
	private static int nearest( Level level, int from, Char ch ){
		int w = level.width();
		int[] around = {-w-1, -w, -w+1, -1, +1, w-1, w, w+1};
		Blob fire = level.blobs.get( MagicalFireRoom.EternalFire.class );

		boolean[] seen = new boolean[level.length()];
		seen[from] = true;
		ArrayList<Integer> ring = new ArrayList<>();
		ring.add( from );

		while (!ring.isEmpty()){
			ArrayList<Integer> next = new ArrayList<>();
			int best = -1;
			for (int cell : ring){
				for (int offset : around){
					int n = cell + offset;
					if (!level.insideMap(n) || seen[n]) continue;
					//the flag maps aren't filled in during generation, terrain always is
					boolean wall = level.solid[n] || (Terrain.flags[level.map[n]] & Terrain.SOLID) != 0;
					if (wall && !level.passable[n]) continue;
					if (fire != null && fire.volume > 0 && fire.cur[n] > 0) continue;

					seen[n] = true;
					next.add( n );
					if (fits( level, n, ch )
							&& (best == -1 || level.trueDistance(from, n) < level.trueDistance(from, best))){
						best = n;
					}
				}
			}
			if (best != -1) return best;
			ring = next;
		}
		return -1;
	}

	private static boolean fits( Level level, int cell, Char ch ){
		if (ch == null){
			//chests and things for sale aren't somewhere to wash up on
			int flags = Terrain.flags[level.map[cell]];
			Heap heap = level.heaps.get( cell );
			return (flags & Terrain.PASSABLE) != 0 && (flags & Terrain.SOLID) == 0 && !level.solid[cell]
					&& (heap == null || heap.type == Heap.Type.HEAP);
		}
		return level.passable[cell]
				&& (!Char.hasProp(ch, Char.Property.LARGE) || level.openSpace[cell])
				&& Actor.findChar(cell) == null;
	}

}
