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

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CultistPower;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoSubclass;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;

import java.util.ArrayList;

//what vesk gives for tengu's mask
public class CultistMask extends Item {

	private static final String AC_WEAR	= "WEAR";

	{
		stackable = false;
		image = ItemSpriteSheet.CULTIST_MASK;

		defaultAction = AC_WEAR;

		unique = true;
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if (hero.subClass == HeroSubClass.NONE) {
			actions.add( AC_WEAR );
		}
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {
		super.execute( hero, action );

		if (action.equals( AC_WEAR ) && hero.subClass == HeroSubClass.NONE) {
			curUser = hero;
			GameScene.show( new WndConfirm( hero ) );
		}
	}

	//what a cultist is gets shown before anything happens
	private class WndConfirm extends WndOptions {

		private final Hero hero;

		WndConfirm( Hero hero ){
			super( new ItemSprite( CultistMask.this ),
					Messages.titleCase( HeroSubClass.CULTIST.title() ),
					HeroSubClass.CULTIST.desc(),
					Messages.get( CultistMask.class, "yes" ),
					Messages.get( CultistMask.class, "talents" ),
					Messages.get( CultistMask.class, "no" ) );
			this.hero = hero;
		}

		@Override
		protected void onSelect( int index ) {
			if (index == 0){
				wear( hero );
			} else if (index == 1){
				GameScene.show( new WndInfoSubclass( hero.heroClass, HeroSubClass.CULTIST ) );
			}
		}
	}

	public void wear( Hero hero ){
		detach( hero.belongings.backpack );
		Catalog.countUse( getClass() );

		hero.spend( Actor.TICK );

		hero.subClass = HeroSubClass.CULTIST;
		Talent.initSubclassTalents( hero );
		//a cleric casts hex from their tome instead, which leaves the button for quick spells
		if (hero.heroClass != HeroClass.CLERIC) {
			Buff.affect( hero, CultistPower.class );
		}

		if (hero.sprite instanceof HeroSprite) {
			//in case they already wear class armor
			((HeroSprite) hero.sprite).updateArmor();
		}
		updateQuickslot();

		if (hero.sprite != null) {
			hero.busy();
			hero.sprite.operate( hero.pos );
			Sample.INSTANCE.play( Assets.Sounds.MASTERY );

			Emitter e = hero.sprite.centerEmitter();
			e.pos( e.x-2, e.y-6, 4, 4 );
			e.start( Speck.factory(Speck.MASK), 0.05f, 20 );
		}
		GLog.p( Messages.get( this, "used" ) );
		if (hero.heroClass == HeroClass.CLERIC) {
			GLog.p( Messages.get( this, "tome_darkens" ) );
		}
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}
}
