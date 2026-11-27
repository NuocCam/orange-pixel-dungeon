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

package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.secret;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Vesk;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.tiles.CustomTilemap;
import com.watabou.noosa.Tilemap;
import com.watabou.utils.Point;

//the forgotten cell vesk walled himself into. It's added on top of the normal secret
//rooms, so it isn't in the list those are picked from
public class VeskRoom extends SecretRoom {

	@Override
	public boolean canConnect(Room r) {
		return !r.isEntrance() && super.canConnect(r);
	}

	@Override
	public boolean canPlaceWater(Point p) {
		return false;
	}

	@Override
	public boolean canPlaceGrass(Point p) {
		return false;
	}

	@Override
	public boolean canPlaceTrap(Point p) {
		return false;
	}

	@Override
	public boolean canPlaceCharacter(Point p, Level l) {
		return false; //no wandering or teleporting in, same as shops
	}

	//always 5x5 inside, so the layout stays the same
	@Override
	public int minWidth() { return 7; }
	public int maxWidth() { return 7; }
	public int minHeight() { return 7; }
	public int maxHeight() { return 7; }

	@Override
	public void paint(Level level) {

		Painter.fill( level, this, Terrain.WALL );
		Painter.fill( level, this, 1, Terrain.EMPTY );

		Door entrance = entrance();
		entrance.set( Door.Type.HIDDEN );
		int door = level.pointToCell(entrance);

		int center = level.pointToCell(center());
		int w = level.width();

		//old cages in the corners away from the door
		int[] corners = new int[]{ center - 2 - 2*w, center + 2 - 2*w, center - 2 + 2*w, center + 2 + 2*w };
		int cages = 0;
		for (int cell : corners){
			if (cages < 2 && level.distance(door, cell) > 2){
				Painter.set(level, cell, Terrain.REGION_DECO);
				cages++;
			}
		}

		Painter.set(level, center - 1 - w, Terrain.EMPTY_DECO);
		Painter.set(level, center + 1 + w, Terrain.EMPTY_DECO);

		for (int cell : new int[]{ center - 1, center + 1, center + w }){
			Painter.set(level, cell, Terrain.CUSTOM_DECO_EMPTY);
			Candle candle = new Candle();
			candle.pos(cell, level);
			level.customTiles.add(candle);
		}

		Vesk vesk = new Vesk();
		vesk.pos = center;
		level.mobs.add( vesk );
	}

	//uses the candle item's picture, there's no candle tile
	public static class Candle extends CustomTilemap {

		{
			texture = Assets.Sprites.ITEMS;
		}

		@Override
		public Tilemap create() {
			Tilemap v = super.create();
			v.map(new int[]{ ItemSpriteSheet.CANDLE }, 1);
			return v;
		}

		@Override
		public String name(int tileX, int tileY) {
			return Messages.get(this, "name");
		}

		@Override
		public String desc(int tileX, int tileY) {
			return Messages.get(this, "desc");
		}
	}
}
