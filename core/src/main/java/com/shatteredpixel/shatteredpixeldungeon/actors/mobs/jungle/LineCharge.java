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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.DeepWater;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;

import java.util.ArrayList;

//a headlong run down a straight line at any angle, flattening whoever is first in the way
public class LineCharge {

	private static final float HIT_DAMAGE = 1.75f;
	private static final int KNOCKBACK = 10;

	public static class Result {
		public Char hit;
		public boolean hitWall;
		public int endPos;
	}

	//a spot with 1 tile between them and a clear run back in, the straightest one. -1 if cornered
	public static int backOffCell( Char charger, Char enemy ){
		int best = -1;
		for (int offset : PathFinder.NEIGHBOURS8){
			int cell = charger.pos + offset;
			if (!Dungeon.level.passable[cell] || DeepWater.blocks(charger, cell)
					|| Actor.findChar(cell) != null
					|| Dungeon.level.distance(cell, enemy.pos) != 2
					|| aim(cell, enemy.pos, charger) == null){
				continue;
			}
			if (best == -1 || Dungeon.level.trueDistance(cell, enemy.pos)
					< Dungeon.level.trueDistance(best, enemy.pos)){
				best = cell;
			}
		}
		return best;
	}

	//aimed the way the demon floor's death gaze is: through 'through' and on past it,
	// until somthing stops a charge. The starting cell isn't included
	public static ArrayList<Integer> path( int from, int through ){
		ArrayList<Integer> path = new ArrayList<>();
		if (from == through) return path;

		for (int cell : line(from, through)){
			if (cell == from) continue;
			if (stops(cell)) break;
			path.add(cell);
		}
		return path;
	}

	//the swing straight after a charge connects can't be dodged
	public static class SureSwing {

		private static final String TARGET = "sure_swing_target";
		private static final String FRESH  = "sure_swing_fresh";

		//an actor id, 0 for nobody
		private int target = 0;
		private boolean fresh = false;

		public void ready( Char ch ){
			target = ch.id();
			fresh = true;
		}

		//for the start of each of the charger's turns. It only lasts through the one after the charge
		public void newTurn(){
			if (!fresh) target = 0;
			fresh = false;
		}

		public float accuracy( Char enemy, float accMulti ){
			if (target == 0 || enemy == null || enemy.id() != target) return accMulti;
			target = 0;
			return Char.INFINITE_ACCURACY;
		}

		public void store( Bundle bundle ){
			bundle.put(TARGET, target);
			bundle.put(FRESH, fresh);
		}

		public void restore( Bundle bundle ){
			target = bundle.getInt(TARGET);
			fresh = bundle.getBoolean(FRESH);
		}
	}

	//the path of a charge at this cell, null if it wouldn't get there with nothing in the way
	public static ArrayList<Integer> aim( int from, int target ){
		return aim(from, target, null);
	}

	//'self' is somebody who will have moved to 'from' by then, so isn't in their own way
	public static ArrayList<Integer> aim( int from, int target, Char self ){
		ArrayList<Integer> path = path(from, target);
		int at = path.indexOf(target);
		if (at == -1) return null;
		for (int i = 0; i < at; i++){
			Char ch = Actor.findChar(path.get(i));
			if (ch != null && ch != self) return null;
		}
		return path;
	}

	private static ArrayList<Integer> line( int from, int through ){
		return new Ballistica(from, through, Ballistica.WONT_STOP).path;
	}

	private static boolean stops( int cell ){
		return Dungeon.level.solid[cell] || Dungeon.level.map[cell] == Terrain.DEEP_WATER;
	}

	//runs a path which was marked earlier from 'from' at 'aim', so things may have changed
	// since then
	public static Result run( Mob charger, int from, int aim, ArrayList<Integer> path, Object cause ){
		Result result = new Result();
		result.endPos = charger.pos;

		//rooted, or knocked somewhere the path doesn't start from any more
		if (charger.rooted || path.isEmpty() || charger.pos != from) {
			return result;
		}

		//the cell the marked path ran into. The line gives out at the map's border wall
		ArrayList<Integer> line = line(from, aim);
		int stoppedBy = path.size()+1 < line.size() ? line.get(path.size()+1) : -1;
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
		result.hitWall = result.hit == null && (stoppedBy == -1 || Dungeon.level.solid[stoppedBy]);

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
				//knocked on the way the charge was going, if the map goes any further
				Ballistica knock = null;
				int far = line.get(line.size()-1);
				if (far != result.hit.pos){
					knock = new Ballistica(result.hit.pos, far, Ballistica.MAGIC_BOLT);
					WandOfBlastWave.throwChar(result.hit, knock, KNOCKBACK, false, true, cause);
				}
				Actor.add(new FollowUp(charger, result.hit, knock));
			}
		}

		return result;
	}

	//goes off once the knockback has played out. Adds the extra turn of paralysis, and
	// brings the charger along, so it gets another swing in
	private static class FollowUp extends Actor {

		{
			//after the push, before anyone gets a turn
			actPriority = VFX_PRIO;
		}

		private final Mob charger;
		private final Char target;
		private final Ballistica knock;

		private FollowUp( Mob charger, Char target, Ballistica knock ){
			this.charger = charger;
			this.target = target;
			this.knock = knock;
		}

		@Override
		protected boolean act() {
			Actor.remove(this);
			if (target.isAlive()){
				Buff.affect(target, Paralysis.class).extend(TICK);
				if (charger.isAlive()) follow(charger, target, knock);
			}
			return true;
		}
	}

	//to the cell just behind where the target came down, or as close to it as it can get
	private static void follow( Mob charger, Char target, Ballistica knock ){
		if (charger.rooted || knock == null) return;
		//not on the way it was knocked, so somthing else carried it off
		int landed = knock.path.indexOf(target.pos);
		if (landed < 1) return;

		boolean[] open = new boolean[Dungeon.level.length()];
		for (int i = 0; i < open.length; i++){
			open[i] = Dungeon.level.passable[i] && !DeepWater.blocks(charger, i);
		}
		for (Char ch : Actor.chars()){
			open[ch.pos] = false;
		}
		PathFinder.buildDistanceMap(charger.pos, open);

		//no long ways round
		int reach = Dungeon.level.distance(charger.pos, target.pos) + 1;
		int behind = knock.path.get(landed-1);
		int best = charger.pos;
		for (int i = 0; i < open.length; i++){
			if (PathFinder.distance[i] <= reach && closer(i, best, target.pos, behind)){
				best = i;
			}
		}

		if (best != charger.pos){
			int from = charger.pos;
			charger.move(best, false);
			if (charger.pos == best){
				Actor.add(new Pushing(charger, from, best));
			}
		}
	}

	//nearer the target wins, then nearer the spot right behind it
	private static boolean closer( int cell, int than, int target, int behind ){
		int gap = Dungeon.level.distance(cell, target) - Dungeon.level.distance(than, target);
		return gap < 0 || gap == 0
				&& Dungeon.level.trueDistance(cell, behind) < Dungeon.level.trueDistance(than, behind);
	}

}
