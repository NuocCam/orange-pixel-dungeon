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

package com.shatteredpixel.shatteredpixeldungeon.levels.regions;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.MobSpawner;
import com.shatteredpixel.shatteredpixeldungeon.journal.Document;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret.SecretRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.StandardRoom;

import java.util.ArrayList;

//what sets a region apart while a floor is being put together. These can't be Level
//methods, rooms get picked before Dungeon.level is the new floor, and the loading screen
//has no level at all. Anything about a floor which already exists goes on Level.
//
//This class is the normal game, so every hook here says 'no change'. null means use the
// usual thing.
public class Region {

	private static final Region NORMAL = new Region();
	private static final Region JUNGLE = new JungleRegion();

	//the region of this depth on the current branch
	public static Region at( int depth ){
		return Dungeon.inJungle(depth) ? JUNGLE : NORMAL;
	}

	//the first region of the current run, whatever floor we happen to be on
	public static Region ofRun(){
		return Dungeon.jungle ? JUNGLE : NORMAL;
	}

	//the same for a saved game which isn't loaded yet
	public static Region ofSave( GamesInProgress.Info info ){
		return info.jungle ? JUNGLE : NORMAL;
	}

	//the plain mobs of a floor, unshuffled. A floor with its own gets no rare extras either
	public ArrayList<Class<? extends Mob>> mobRotation( int depth ){
		return null;
	}

	//the rare version of a mob on this floor, null if it has none
	public Class<? extends Mob> rareAlt( int depth, Class<? extends Mob> cls ){
		return MobSpawner.RARE_ALTS.get(cls);
	}

	//how much likelier than usual each rare version is on this floor
	public float rareAltScale( int depth ){
		return 1f;
	}

	//level for the regular floors of the first region
	public Level newLevel(){
		return null;
	}

	//level for the boss floor of the first region
	public Level newBossLevel(){
		return null;
	}

	//loading screen art of the first region, and the point it's centered on
	public String loadingSplash(){
		return null;
	}

	public int loadingCenter(){
		return 0;
	}

	//journal page shown on entering the first region
	public String introPage(){
		return null;
	}

	//the journal pages left lying around the first region
	public Document loreDoc(){
		return null;
	}

	//message key for the first thing the rose's ghost ever says
	public String ghostIntro(){
		return null;
	}

	public StandardRoom newEntrance(){
		return null;
	}

	public StandardRoom newExit(){
		return null;
	}

	//stands in for the standard room which rolled this spot in StandardRoom's list
	public StandardRoom newStandardRoom( int index ){
		return null;
	}

	//what a rolled floor feeling turns into here
	public Level.Feeling feeling( Level.Feeling rolled ){
		return rolled;
	}

	//takes rooms which don't belong here off a floor's lists, or swaps in the ones which do.
	// Only ever give these a copy, the run's own lists are shared with the later regions
	public void adjustSpecials( ArrayList<Class<? extends Room>> specials ){
	}

	//the room in the run's list which this one was swapped in for, normally just itself
	public Class<? extends Room> originalSpecial( Class<? extends Room> type ){
		return type;
	}

	//whether the lab room goes on this floor. Only asked while the region still needs its one,
	// rolled is what the dice came up with
	public boolean labHere( boolean rolled ){
		return rolled;
	}

	public void removeSecrets( ArrayList<Class<? extends SecretRoom>> secrets ){
	}
}
