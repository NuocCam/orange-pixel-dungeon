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

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;

public class BoarSprite extends JungleSprite {

	private Animation charge;
	private Animation stunned;

	public BoarSprite() {
		super( Assets.Sprites.BOAR, 22, 16 );

		charge = new Animation( 15, true );
		charge.frames( frames, 12, 13, 14, 15 );

		stunned = new Animation( 6, true );
		stunned.frames( frames, 16, 17, 18 );
	}

	public void charge(){
		play( charge );
	}

	public void stunned(){
		play( stunned );
	}
}
