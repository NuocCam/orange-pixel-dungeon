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

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Adrenaline;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.jungle.Squirrel;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.food.JungleBerry;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.RegularLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

//Idol's Offering, once in every jungle run. A line of trees cuts the room in two and the
// idol fills the only gap. Feed it to get at the prize, and mind what happens when you take it
public class IdolRoom extends SpecialRoom {

	@Override
	public int minWidth() { return 7; }
	public int maxWidth() { return 9; }

	@Override
	public int minHeight() { return 7; }
	public int maxHeight() { return 9; }

	//the floor of this run which gets the idol
	public static int depthForRun( long seed ){
		Random.pushGenerator( seed + 4600 );
		int depth = Random.IntRange( 2, 4 );
		Random.popGenerator();
		return depth;
	}

	//always the middle of the room, so it can still be found once it's gone
	public Point idol(){
		return new Point( (left + right)/2, (top + bottom)/2 );
	}

	@Override
	public void paint( Level level ) {
		Painter.fill( level, this, Terrain.WALL );
		Painter.fill( level, this, 1, Terrain.EMPTY_SP );

		Door door = entrance();
		door.set( Door.Type.REGULAR );

		//the trees run across the way from the door to the prize
		Point idol = idol();
		Point prize;
		if (door.x == left || door.x == right){
			Painter.fill( level, idol.x, top+1, 1, height()-2, Terrain.WALL );
			prize = new Point( door.x == left ? right-1 : left+1, idol.y );
		} else {
			Painter.fill( level, left+1, idol.y, width()-2, 1, Terrain.WALL );
			prize = new Point( idol.x, door.y == top ? bottom-1 : top+1 );
		}
		Painter.set( level, idol, Terrain.IDOL );

		int pos = level.pointToCell( prize );
		Painter.set( level, pos, Terrain.PEDESTAL );
		Heap heap = level.drop( ThornHollowRoom.junglePrize( level ), pos );
		heap.type = Heap.Type.CHEST;
		heap.idolAlarm = true;

		//one berry more than the floor would've had, growing with all the others
		level.addItemToSpawn( new JungleBerry() );
	}

	@Override
	public boolean canPlaceTrap( Point p ) {
		return false;
	}

	//the hero has walked up to the idol
	public static void interact( final Hero hero, final int idolPos ){
		if (hero.belongings.getItem(JungleBerry.class) == null){
			GLog.w( Messages.get(IdolRoom.class, "no_berry") );
			return;
		}
		Game.runOnRenderThread(new Callback() {
			@Override
			public void call() {
				GameScene.show( new WndOptions( new ItemSprite( new JungleBerry() ),
						Messages.titleCase( Messages.get(Level.class, "idol_name") ),
						Messages.get(IdolRoom.class, "offer_prompt"),
						Messages.get(IdolRoom.class, "offer_yes"),
						Messages.get(IdolRoom.class, "offer_no") ){
					@Override
					protected void onSelect( int index ) {
						if (index == 0){
							offer( hero, idolPos );
						}
					}
				});
			}
		});
	}

	//gives up a berry to open the way, false if that can't be done
	public static boolean offer( Hero hero, int idolPos ){
		Item berry = hero.belongings.getItem(JungleBerry.class);
		if (berry == null || Dungeon.level.map[idolPos] != Terrain.IDOL){
			return false;
		}
		berry.detach( hero.belongings.backpack );

		Level.set( idolPos, Terrain.EMPTY_SP );
		GameScene.updateMap( idolPos );
		Dungeon.observe();

		Sample.INSTANCE.play( Assets.Sounds.UNLOCK );
		GLog.i( Messages.get(IdolRoom.class, "offered") );
		hero.sprite.operate( idolPos );
		hero.busy();
		hero.spendAndNext( 1f );
		return true;
	}

	//the chest at this cell has just been opened
	public static void prizeTaken( Level level, int chestPos ){
		Room room = level instanceof RegularLevel ? ((RegularLevel) level).room( chestPos ) : null;
		alarm( level, room instanceof IdolRoom ? level.pointToCell( ((IdolRoom) room).idol() ) : chestPos );
	}

	//every enemy comes to see who took it, the ones nearby in a hurry
	public static void alarm( Level level, int idolPos ){
		for (Mob mob : level.mobs.toArray( new Mob[0] )){
			if (mob.alignment != Char.Alignment.ENEMY || mob instanceof Squirrel){
				continue;
			}
			mob.beckon( Dungeon.hero.pos );
			if (level.distance( mob.pos, idolPos ) <= 10){
				Buff.prolong( mob, Adrenaline.class, 3f );
			}
		}
		GLog.n( Messages.get(IdolRoom.class, "alarm") );
		Sample.INSTANCE.play( Assets.Sounds.CHALLENGE );
		PixelScene.shake( 3, 0.7f );
	}
}
