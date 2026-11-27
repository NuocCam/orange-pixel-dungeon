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

package com.shatteredpixel.shatteredpixeldungeon.items.bombs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.EarthParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Random;

import java.util.ArrayList;

//an arcane bomb's blast made out of beast marrow. Armor works against it, but it
// sends everything flying
public class StampedeBomb extends ArcaneBomb {

	private static final int KNOCKBACK = 4;

	{
		image = ItemSpriteSheet.STAMPEDE_BOMB;
	}

	@Override
	protected void hit( Char ch, int cell ) {
		//another bomb may have got there first
		if (!ch.isAlive()) return;

		int damage = Random.NormalIntRange( 4 + Dungeon.scalingDepth(), 12 + 3*Dungeon.scalingDepth() );
		damage -= ch.drRoll();
		if (damage > 0) {
			ch.damage(damage, this);
		}

		if (!ch.isAlive()){
			if (ch == Dungeon.hero) {
				GLog.n(Messages.get(this, "ondeath"));
				Dungeon.fail(this);
			}

		//anything right on top of the bomb has no way to be thrown
		} else if (ch.pos != cell){
			//straight on from the middle of the blast, through where they stand
			ArrayList<Integer> line = new Ballistica(cell, ch.pos, Ballistica.STOP_TARGET).path;
			Ballistica away = new Ballistica(ch.pos, line.get(line.size()-1), Ballistica.PROJECTILE);
			WandOfBlastWave.throwChar(ch, away, KNOCKBACK, false, true, this);
		}
	}

	@Override
	protected Emitter.Factory warnParticle() {
		return EarthParticle.FACTORY;
	}
}
