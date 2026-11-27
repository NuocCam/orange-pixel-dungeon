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

package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots;
import com.shatteredpixel.shatteredpixeldungeon.effects.Wound;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.Key;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLevitation;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfPurity;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

import java.util.ArrayList;

//the jungle's trap room. Same shape and reward, but thorn vines where the traps would be
public class ThornHollowRoom extends TrapsRoom {

	@Override
	public void paint( Level level ) {
		Painter.fill( level, this, Terrain.WALL );

		int pos = paintLayout( level, Terrain.THORNS, Terrain.EMPTY );
		placePrize( level, pos, Terrain.EMPTY );

		level.addItemToSpawn( new PotionOfLevitation() );
	}

	@Override
	protected Item reward( Level level ) {
		return junglePrize( level );
	}

	//things which have to stay out in the open. Nobody should need a potion or a berry
	// just to get at a key, or at the potion another room is waiting for
	private static final Class<?>[] NEVER_PRIZES = {
			Key.class, PotionOfPurity.class, PotionOfLevitation.class };

	//the trap room's prize, minus those
	@SuppressWarnings("unchecked")
	static Item junglePrize( Level level ) {
		ArrayList<Item> held = new ArrayList<>();
		for (Class<?> type : NEVER_PRIZES){
			for (Item item; (item = level.findPrizeItem((Class<? extends Item>) type)) != null; ){
				held.add(item);
			}
		}
		Item prize = prize( level );
		for (Item item : held){
			level.addItemToSpawn(item);
		}
		return prize;
	}

	@Override
	public boolean canPlaceTrap(Point p) {
		return false;
	}

	//something on foot has just stepped into the thorns
	public static void prick( Char ch ){
		if (ch.sprite != null && ch.sprite.visible){
			Wound.hit( ch );
		}
		ch.damage( Random.NormalIntRange(2, 6) + Dungeon.depth, new Thorns() );
		if (ch.isAlive()){
			Buff.affect( ch, Bleeding.class ).set( 2 + Dungeon.depth/2 );
			Buff.prolong( ch, Roots.class, 3f );
		} else if (ch == Dungeon.hero){
			Dungeon.fail( Thorns.class );
			GLog.n( Messages.get(Thorns.class, "ondeath") );
		}
	}

	//what gets the blame
	public static class Thorns {}
}
