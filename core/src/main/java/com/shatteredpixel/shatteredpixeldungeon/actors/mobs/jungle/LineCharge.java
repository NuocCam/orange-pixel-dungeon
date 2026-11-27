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

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.jungle;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;

import java.util.ArrayList;

//a headlong run down a straight line, flattening whoever is first in the way
public class LineCharge {

	private static final float HIT_DAMAGE = 1.5f;
	private static final int KNOCKBACK = 10;

	public static class Result {
		public Char hit;
		public boolean hitWall;
		public int endPos;
	}

	//same row, same column or an exact diagonal
	public static boolean onLine( int from, int to ){
		int w = Dungeon.level.width();
		int dx = Math.abs(to % w - from % w);
		int dy = Math.abs(to / w - from / w);
		return from != to && (dx == 0 || dy == 0 || dx == dy);
	}

	//every cell from 'from' towards 'through' and on past it, until somthing stops a charge
	public static ArrayList<Integer> path( int from, int through ){
		ArrayList<Integer> path = new ArrayList<>();
		if (!onLine(from, through)) return path;

		int w = Dungeon.level.width();
		int step = Integer.signum(through % w - from % w) + w*Integer.signum(through / w - from / w);
		for (int cell = from + step; Dungeon.level.insideMap(cell) && !stops(cell); cell += step){
			path.add(cell);
		}
		return path;
	}

	private static boolean stops( int cell ){
		return Dungeon.level.solid[cell] || Dungeon.level.map[cell] == Terrain.DEEP_WATER;
	}

	//runs a path which was marked earlier, so things may have changed since then
	public static Result run( Mob charger, ArrayList<Integer> path, Object cause ){
		Result result = new Result();
		result.endPos = charger.pos;

		//rooted, or knocked somewhere the path doesn't start from any more
		if (charger.rooted || path.isEmpty() || !Dungeon.level.adjacent(charger.pos, path.get(0))) {
			return result;
		}
		int step = path.get(0) - charger.pos;
		if (path.size() > 1 && path.get(1) - path.get(0) != step) {
			return result;
		}

		int stoppedBy = path.get(path.size()-1) + step;
		for (int cell : path){
			if (stops(cell)){
				stoppedBy = cell;
				break;
			}
			result.hit = Actor.findChar(cell);
			if (result.hit != null) break;
			result.endPos = cell;
		}
		//deep water isn't solid, so pulling up at it is no crash
		result.hitWall = result.hit == null && Dungeon.level.solid[stoppedBy];

		int from = charger.pos;
		if (result.endPos != from){
			charger.move(result.endPos, false);
			//a teleport has already put the sprite where it belongs
			if (charger.pos == result.endPos){
				Actor.add(new Pushing(charger, from, result.endPos));
			}
		}

		//a trap where it pulled up may have killed it or sent it elsewhere
		if (result.hit != null && charger.isAlive() && charger.pos == result.endPos){
			//no dodging it, getting off the line is how you do that
			if (charger.attack(result.hit, HIT_DAMAGE, 0f, Char.INFINITE_ACCURACY)
					&& result.hit.isAlive()){
				WandOfBlastWave.throwChar(result.hit,
						new Ballistica(result.hit.pos, result.hit.pos + step, Ballistica.MAGIC_BOLT),
						KNOCKBACK, false, true, cause);
			}
		}

		return result;
	}

}
