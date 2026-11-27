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

package com.shatteredpixel.shatteredpixeldungeon.actors.blobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ArtifactRecharge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Recharging;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRecharging;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes.Landmark;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Random;

public class WaterOfElectricity extends WellWater {

	@Override
	protected boolean affectHero( Hero hero ) {

		if (!hero.isAlive()) return false;

		Sample.INSTANCE.play( Assets.Sounds.DRINK );
		zap( hero.pos );

		Buff.affect( hero, Recharging.class, Recharging.DURATION );
		//a full horn is a lot of free food, so it gets nothing from this
		Buff.affect( hero, ArtifactRecharge.class ).set( 30 ).ignoreHornOfPlenty = true;
		ScrollOfRecharging.charge( hero );

		Dungeon.hero.interrupt();
		Buff.affect( hero, Paralysis.class, 3f );

		GLog.w( Messages.get(this, "procced") );

		return true;
	}

	@Override
	protected Item affectItem( Item item, int pos ) {
		boolean filled;
		if (item instanceof Wand) {
			filled = ((Wand) item).fullyRecharge();
		} else if (item instanceof MagesStaff) {
			filled = ((MagesStaff) item).fullyRechargeWand();
		} else {
			filled = item instanceof Artifact && ((Artifact) item).fullyRecharge();
		}

		if (!filled) return null;

		zap( pos );
		return item;
	}

	private static void zap( int cell ) {
		GameScene.flash( 0x80FFFFAA );
		Sample.INSTANCE.play( Assets.Sounds.LIGHTNING );
		burst( cell, WellSpark.FACTORY, 20 );
	}

	@Override
	public Landmark landmark() {
		return Landmark.WELL_OF_ELECTRICITY;
	}

	@Override
	public void use( BlobEmitter emitter ) {
		super.use( emitter );
		emitter.pour( WellSpark.FACTORY, 0.15f );
	}

	@Override
	public String tileDesc() {
		return Messages.get(this, "desc");
	}

	//its own class so the colour never sticks to anyone else's recycled sparks
	public static class WellSpark extends SparkParticle {

		public static final Emitter.Factory FACTORY = new Emitter.Factory() {
			@Override
			public void emit( Emitter emitter, int index, float x, float y ) {
				((WellSpark) emitter.recycle( WellSpark.class )).reset( x, y );
			}
			@Override
			public boolean lightMode() {
				return true;
			}
		};

		@Override
		public void reset( float x, float y ) {
			super.reset( x, y );
			color( Random.Int( 2 ) == 0 ? 0xFFEE44 : 0x44AAFF );
		}
	}
}
