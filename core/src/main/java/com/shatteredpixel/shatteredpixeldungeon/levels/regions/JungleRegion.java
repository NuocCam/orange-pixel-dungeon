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

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Document;
import com.shatteredpixel.shatteredpixeldungeon.levels.JungleLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.DeepWater;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret.SecretChestChasmRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret.SecretHoardRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret.SecretRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret.SecretSummoningRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.ToxicGasRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.TrapsRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.WeakFloorRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.StandardRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.jungle.GroveRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.jungle.JungleEntranceRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.jungle.JungleExitRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.jungle.LogCrossingRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.jungle.OvergrownRuinsRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.jungle.PondRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.standard.jungle.ThicketRoom;

import java.util.ArrayList;

//takes the place of the sewers in about half of all runs
public class JungleRegion extends Region {

	@Override
	public Level newLevel() {
		return new JungleLevel();
	}

	@Override
	public String loadingSplash() {
		return Assets.Splashes.JUNGLE;
	}

	@Override
	public int loadingCenter() {
		return 400;
	}

	@Override
	public String introPage() {
		return Document.INTRO_JUNGLE;
	}

	@Override
	public StandardRoom newEntrance() {
		return new JungleEntranceRoom();
	}

	@Override
	public StandardRoom newExit() {
		return new JungleExitRoom();
	}

	@Override
	public StandardRoom newStandardRoom( int index ) {
		//floor 5 is still the sewer boss floor, and its tiles can't draw these
		if (Dungeon.bossLevel()){
			return null;
		}
		//the first five rooms on the list are the ones only the sewers use
		switch (index){
			case 0: return new GroveRoom();
			case 1: return new PondRoom();
			case 2: return new LogCrossingRoom();
			case 3: return new OvergrownRuinsRoom();
			case 4: return new ThicketRoom();
			default: return null;
		}
	}

	@Override
	public Level.Feeling feeling( Level.Feeling rolled ) {
		//there are no pits and no traps in the jungle
		if (rolled == Level.Feeling.CHASM || rolled == Level.Feeling.TRAPS){
			return Level.Feeling.NONE;
		}
		return rolled;
	}

	@Override
	public void removeSpecials( ArrayList<Class<? extends Room>> specials ) {
		specials.remove(WeakFloorRoom.class);
		specials.remove(ToxicGasRoom.class);
		specials.remove(TrapsRoom.class);
	}

	@Override
	public void removeSecrets( ArrayList<Class<? extends SecretRoom>> secrets ) {
		secrets.remove(SecretHoardRoom.class);
		secrets.remove(SecretSummoningRoom.class);
		//its chests sit on little islands. With no chasm to jump into, running out of
		// levitation on one would leave you there for good
		secrets.remove(SecretChestChasmRoom.class);
	}
}
