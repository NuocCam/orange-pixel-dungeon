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
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.jungle.Bear;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.jungle.Boar;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.jungle.Deer;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.jungle.DireWolf;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.jungle.Gecko;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.jungle.Mantis;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.jungle.Skink;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.jungle.Toad;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.jungle.Wolf;
import com.shatteredpixel.shatteredpixeldungeon.journal.Document;
import com.shatteredpixel.shatteredpixeldungeon.levels.JungleBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.JungleLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.DeepWater;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret.SecretChestChasmRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret.SecretHoardRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret.SecretRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret.SecretSummoningRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.BlossomRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.IdolRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.ThornHollowRoom;
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

import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

//takes the place of the sewers in about half of all runs
public class JungleRegion extends Region {

	@Override
	public ArrayList<Class<? extends Mob>> mobRotation( int depth ) {
		switch (depth){
			case 1:
				//3x skink, 1x deer
				return rotation(Skink.class, Skink.class, Skink.class, Deer.class);
			case 2:
				//2x skink, 1x deer, 2x boar
				return rotation(Skink.class, Skink.class, Deer.class, Boar.class, Boar.class);
			case 3:
				//1x skink, 1x deer, 2x boar, 1x mantis, 1x toad, 1x wolf
				return rotation(Skink.class, Deer.class, Boar.class, Boar.class, Mantis.class,
						Toad.class, Wolf.class);
			case 4:
				//1x boar (or mantis), 1x toad, 2x wolf, 2x bear
				return rotation(Random.Int(2) == 0 ? Mantis.class : Boar.class,
						Toad.class, Wolf.class, Wolf.class, Bear.class, Bear.class);
			default:
				//floor 5 keeps the sewer's mobs
				return null;
		}
	}

	@Override
	public Class<? extends Mob> rareAlt( int depth, Class<? extends Mob> cls ) {
		//a gecko is too much for a fresh hero
		if (cls == Skink.class)  return depth > 1 ? Gecko.class : null;
		if (cls == Wolf.class)   return DireWolf.class;
		return super.rareAlt(depth, cls);
	}

	@SafeVarargs
	private static ArrayList<Class<? extends Mob>> rotation( Class<? extends Mob>... mobs ){
		return new ArrayList<>(Arrays.asList(mobs));
	}

	@Override
	public Level newLevel() {
		return new JungleLevel();
	}

	@Override
	public Level newBossLevel() {
		return new JungleBossLevel();
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
	public Document loreDoc() {
		return Document.HUNTER_NOTES;
	}

	@Override
	public String ghostIntro() {
		return "introduce_jungle";
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
	public void adjustSpecials( ArrayList<Class<? extends Room>> specials ) {
		specials.remove(WeakFloorRoom.class);
		//these two come as their jungle versions, in the same place in line
		Collections.replaceAll(specials, ToxicGasRoom.class, BlossomRoom.class);
		Collections.replaceAll(specials, TrapsRoom.class, ThornHollowRoom.class);
		if (Dungeon.depth == IdolRoom.depthForRun(Dungeon.seed)){
			specials.add(0, IdolRoom.class);
		}
	}

	@Override
	public boolean labHere( boolean rolled ) {
		//a floor with just one special room can't take both the lab and the idol, so the
		// lab takes whichever of floors 3 and 4 the idol leaves free
		int idol = IdolRoom.depthForRun(Dungeon.seed);
		if (Dungeon.depth == idol)              return false;
		if (Dungeon.depth == 3 && idol == 4)    return true;
		return rolled;
	}

	@Override
	public Class<? extends Room> originalSpecial( Class<? extends Room> type ) {
		if (type == BlossomRoom.class)      return ToxicGasRoom.class;
		if (type == ThornHollowRoom.class)  return TrapsRoom.class;
		return type;
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
