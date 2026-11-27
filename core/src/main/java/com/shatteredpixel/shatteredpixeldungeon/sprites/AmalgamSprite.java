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
import com.watabou.noosa.TextureFilm;

public class AmalgamSprite extends MobSprite {

	private Animation charge;
	private Animation stunned;
	private Animation roar;
	private Animation slamWindup;

	//these play once and settle back down. The bite copy is separate from attack, or
	// finishing it would deal the damage a second time
	private Animation eat;
	private Animation slam;
	private Animation spit;
	private Animation sweep;
	private Animation doubleBite;
	private int bites = 0;

	public AmalgamSprite() {
		super();

		texture( Assets.Sprites.AMALGAM );

		TextureFilm frames = new TextureFilm( texture, 32, 28 );

		idle = new Animation( 3, true );
		idle.frames( frames, 0, 0, 0, 1 );

		run = new Animation( 10, true );
		run.frames( frames, 2, 3, 4, 5 );

		attack = new Animation( 12, false );
		attack.frames( frames, 6, 7, 8, 0 );

		doubleBite = attack.clone();

		charge = new Animation( 15, true );
		charge.frames( frames, 9, 10, 11, 12 );

		stunned = new Animation( 6, true );
		stunned.frames( frames, 13, 14, 15 );

		roar = new Animation( 4, true );
		roar.frames( frames, 16, 17 );

		eat = new Animation( 6, false );
		eat.frames( frames, 18, 19, 20, 0 );

		slamWindup = new Animation( 1, true );
		slamWindup.frames( frames, 21 );

		slam = new Animation( 12, false );
		slam.frames( frames, 22, 23, 0 );

		spit = new Animation( 10, false );
		spit.frames( frames, 24, 25, 26, 0 );

		sweep = new Animation( 12, false );
		sweep.frames( frames, 27, 28, 29, 0 );

		die = new Animation( 8, false );
		die.frames( frames, 30, 31, 32 );

		play( idle );
	}

	public void charge(){
		play( charge );
	}

	public void stunned(){
		play( stunned );
	}

	public void roar(){
		play( roar );
	}

	public void eat(){
		once( eat );
	}

	public void slamWindup(){
		play( slamWindup );
	}

	public void slam(){
		once( slam );
	}

	public void spit( int cell ){
		turnTo( ch.pos, cell );
		once( spit );
	}

	public void sweep( int cell ){
		turnTo( ch.pos, cell );
		once( sweep );
	}

	public void doubleBite( int cell ){
		turnTo( ch.pos, cell );
		bites = 2;
		once( doubleBite );
	}

	//from the start, even if it was the last thing played
	private void once( Animation anim ){
		if (curAnim != die) {
			play( anim, true );
		}
	}

	@Override
	public synchronized void onComplete( Animation anim ) {
		if (anim == doubleBite && --bites > 0){
			once( doubleBite );
		} else if (anim == doubleBite || anim == eat || anim == slam || anim == spit || anim == sweep){
			idle();
		}
		super.onComplete( anim );
	}

	@Override
	public int blood() {
		return 0xFF6A2C40;
	}
}
