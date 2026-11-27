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
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes.Landmark;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.utils.Random;

public class WaterOfEnchantment extends WellWater {

	@Override
	protected boolean affectHero( Hero hero ) {

		if (!hero.isAlive()) return false;

		Item[] worn = {hero.belongings.weapon(), hero.belongings.secondWep(), hero.belongings.armor()};
		boolean changed = false;
		for (Item item : worn) {
			if (takes( item )) {
				reroll( item, hero.pos );
				changed = true;
			}
		}

		//nothing to work on, so the water stays
		if (!changed) return false;

		Sample.INSTANCE.play( Assets.Sounds.DRINK );

		Dungeon.hero.interrupt();

		GLog.p( Messages.get(this, "procced") );

		return true;
	}

	@Override
	protected Item affectItem( Item item, int pos ) {
		if (!takes( item )) return null;

		reroll( item, pos );
		return item;
	}

	//weapons you hold and armor, unless a blacksmith hardened them
	private static boolean takes( Item item ) {
		if (item instanceof Weapon) {
			return !(item instanceof MissileWeapon) && !((Weapon) item).enchantHardened;
		} else {
			return item instanceof Armor && !((Armor) item).glyphHardened;
		}
	}

	private static void reroll( Item item, int cell ) {
		boolean curse = Random.Int( 2 ) == 0;

		//set this first, the new name shows up in the quickslot right away
		item.cursed = curse;
		item.cursedKnown = true;

		if (item instanceof Weapon) {
			if (curse)  ((Weapon) item).enchant( Weapon.Enchantment.randomCurse() );
			else        ((Weapon) item).enchant();
		} else {
			if (curse)  ((Armor) item).inscribe( Armor.Glyph.randomCurse() );
			else        ((Armor) item).inscribe();
		}

		if (curse) {
			burst( cell, ShadowParticle.CURSE, 12 );
			Sample.INSTANCE.play( Assets.Sounds.CURSED );
		} else {
			burst( cell, Speck.factory( Speck.STAR ), 12 );
			Sample.INSTANCE.play( Assets.Sounds.EVOKE );
		}
		Item.updateQuickslot();
	}

	@Override
	public Landmark landmark() {
		return Landmark.WELL_OF_ENCHANTMENT;
	}

	@Override
	public void use( BlobEmitter emitter ) {
		super.use( emitter );
		emitter.pour( RuneSparkle.FACTORY, 0.25f );
	}

	@Override
	public String tileDesc() {
		return Messages.get(this, "desc");
	}

	//a little diamond which turns as it drifts up
	public static class RuneSparkle extends PixelParticle {

		public static final Emitter.Factory FACTORY = new Emitter.Factory() {
			@Override
			public void emit( Emitter emitter, int index, float x, float y ) {
				((RuneSparkle) emitter.recycle( RuneSparkle.class )).reset( x, y );
			}
			@Override
			public boolean lightMode() {
				return true;
			}
		};

		public RuneSparkle() {
			super();
			lifespan = 1.6f;
		}

		public void reset( float x, float y ) {
			revive();

			this.x = x;
			this.y = y;

			left = lifespan;

			speed.set( 0, -6 );
			angle = 45;
			angularSpeed = 90;
			color( Random.Int( 2 ) == 0 ? 0xBB55FF : 0xFF9922 );
		}

		@Override
		public void update() {
			super.update();
			//fades in, then out again
			float p = left / lifespan;
			am = p < 0.5f ? p * 2 : (1 - p) * 2;
			size( 1 + 2 * am );
		}
	}
}
