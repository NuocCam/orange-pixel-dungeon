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

package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.JunglePainter;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret.SecretRoom;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.SpecialRoom;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;
import com.watabou.noosa.Tilemap;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

//floors 1-4 of a jungle run, taking the place of the sewers.
// There's no sad ghost quest down here.
public class JungleLevel extends RegularLevel {

	{
		color1 = 0x48763c;
		color2 = 0x59994a;
	}

	@Override
	public void playLevelMusic(){
		SewerLevel.playSewerMusic();
	}

	@Override
	protected int standardRooms(boolean forceMax) {
		if (forceMax) return 6;
		//4 to 6, average 5
		return 4+Random.chances(new float[]{1, 3, 1});
	}

	@Override
	protected int specialRooms(boolean forceMax) {
		if (forceMax) return 2;
		//1 to 2, average 1.8
		return 1+Random.chances(new float[]{1, 4});
	}

	@Override
	protected boolean build() {
		//a layout can get thrown out, mostly for lack of room in the trees for a path.
		// Note down what buliding one uses up, or the run slowly loses labs and secret rooms
		ArrayList<Item> items = new ArrayList<>(itemsToSpawn);
		int labs = Dungeon.LimitedDrops.LAB_ROOM.count;
		Bundle roomDecks = new Bundle();
		SpecialRoom.storeRoomsInBundle(roomDecks);
		SecretRoom.storeRoomsInBundle(roomDecks);

		if (super.build()){
			return true;
		}

		itemsToSpawn = items;
		Dungeon.LimitedDrops.LAB_ROOM.count = labs;
		SpecialRoom.restoreRoomsFromBundle(roomDecks);
		SecretRoom.restoreRoomsFromBundle(roomDecks);
		return false;
	}

	@Override
	protected Painter painter() {
		//no traps in the jungle, so none are set on the painter
		return new JunglePainter()
				.setWater(feeling == Feeling.WATER ? 0.85f : 0.30f, 5)
				//a good bit more grass than the sewers get
				.setGrass(feeling == Feeling.GRASS ? 0.85f : 0.35f, 4);
	}

	//props from the jungle decor sheet, drawn over whatever the tiles show there
	public static class Decor extends CustomTilemap {

		//spots on the sheet, which is 8 cells wide
		public static final int PILLAR_TOP = 16;
		public static final int PILLAR     = 24;
		public static final int LILY_PADS  = 37;

		{
			texture = Assets.Environment.JUNGLE_DECOR;
		}

		public int cell;

		//for loading
		public Decor(){}

		public Decor( int cell ){
			this.cell = cell;
		}

		//a pillar is two tiles tall, the top of it goes over whoever stands behind
		public static void addPillar( Level level, int pos ){
			Painter.set( level, pos, Terrain.CUSTOM_DECO );

			Decor base = new Decor( PILLAR );
			base.pos( pos, level );
			level.customTiles.add( base );

			Decor top = new Decor( PILLAR_TOP );
			top.pos( pos - level.width(), level );
			level.customWalls.add( top );
		}

		public static void addLilyPads( Level level, int pos ){
			Decor pads = new Decor( LILY_PADS );
			pads.pos( pos, level );
			level.customTiles.add( pads );
		}

		@Override
		public Tilemap create() {
			Tilemap v = super.create();
			v.map( new int[]{ cell }, 1 );
			return v;
		}

		//lily pads are still just water when examined
		@Override
		public String name( int tileX, int tileY ) {
			return cell == LILY_PADS ? null : Messages.get(JungleLevel.class, "pillar_name");
		}

		@Override
		public String desc( int tileX, int tileY ) {
			return cell == LILY_PADS ? null : Messages.get(JungleLevel.class, "pillar_desc");
		}

		private static final String CELL = "cell";

		@Override
		public void storeInBundle( Bundle bundle ) {
			super.storeInBundle( bundle );
			bundle.put( CELL, cell );
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
			super.restoreFromBundle( bundle );
			cell = bundle.getInt( CELL );
		}
	}

	@Override
	public String tilesTex() {
		return Assets.Environment.TILES_JUNGLE;
	}

	@Override
	public String waterTex() {
		return Assets.Environment.WATER_JUNGLE;
	}

	@Override
	public boolean activateTransition(Hero hero, LevelTransition transition) {
		if (transition.type == LevelTransition.Type.SURFACE){
			return SewerLevel.useSurfaceExit(hero);
		} else {
			return super.activateTransition(hero, transition);
		}
	}

	@Override
	public boolean wallPaths() {
		return true;
	}

	@Override
	public boolean sheetHasFeatures() {
		return true;
	}

	@Override
	public int raisedGrassRow() {
		return 5;
	}

	@Override
	protected boolean burnableDeco(int terrain) {
		//just the tree stump, the boulder is solid rock and never burns
		return terrain == Terrain.REGION_DECO_ALT;
	}

	@Override
	public void buildFlagMaps() {
		super.buildFlagMaps();
		for (int i=0; i < length(); i++) {
			if (burnableDeco(map[i])){
				flamable[i] = true;
			}
		}
	}

	@Override
	public void destroy(int pos) {
		//a burnt stump leaves embers behind, like other wood
		if (map[pos] == Terrain.REGION_DECO_ALT){
			set(pos, Terrain.EMBERS);
		}
		super.destroy(pos);
	}

	@Override
	public String tileName( int tile ) {
		switch (tile) {
			case Terrain.WATER:
				return Messages.get(JungleLevel.class, "water_name");
			case Terrain.BOOKSHELF:
				return Messages.get(JungleLevel.class, "bookshelf_name");
			case Terrain.REGION_DECO:
				return Messages.get(JungleLevel.class, "region_deco_name");
			case Terrain.REGION_DECO_ALT:
				return Messages.get(JungleLevel.class, "region_deco_alt_name");
			default:
				return super.tileName( tile );
		}
	}

	@Override
	public String tileDesc(int tile) {
		switch (tile) {
			case Terrain.EMPTY_DECO:
				return Messages.get(JungleLevel.class, "empty_deco_desc");
			case Terrain.BOOKSHELF:
				return Messages.get(JungleLevel.class, "bookshelf_desc");
			case Terrain.REGION_DECO:
				return Messages.get(JungleLevel.class, "region_deco_desc");
			case Terrain.REGION_DECO_ALT:
				return Messages.get(JungleLevel.class, "region_deco_alt_desc");
			default:
				return super.tileDesc( tile );
		}
	}
}
