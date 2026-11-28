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

package com.shatteredpixel.shatteredpixeldungeon.items.scrolls;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.CounterBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoItem;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

//sold in shops. Pray over it, pick 1 of 2 scrolls and it turns into that one.
//Not a Scroll on purpose: it has no rune, so it can't be transmuted, recycled or brewed
public class SealedScroll extends Item {

	public static final String AC_PRAY = "PRAY";

	private static final float TIME_TO_PRAY = 1f;

	{
		image = ItemSpriteSheet.SEALED_SCROLL;
		stackable = true;
		defaultAction = AC_PRAY;
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_PRAY );
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {
		super.execute( hero, action );
		if (action.equals( AC_PRAY ) && canPray( hero )) {
			GameScene.show( window( hero ) );
		}
	}

	//same rules as reading
	public boolean canPray( Hero hero ) {
		if (hero.buff( MagicImmune.class ) != null) {
			GLog.w( Messages.get(Scroll.class, "no_magic") );
			return false;
		} else if (hero.buff( Blindness.class ) != null) {
			GLog.w( Messages.get(Scroll.class, "blinded") );
			return false;
		}
		return true;
	}

	//The pair comes from the run's seed and how many times you've prayed,
	//so closing the window, reloading or splitting the stack can't reroll it
	public static ArrayList<Class<? extends Scroll>> choices() {
		//the usual scroll odds, upgrade is already 0 there
		float[] odds = Generator.Category.SCROLL.defaultProbsTotal.clone();
		Prayers prayers = Dungeon.hero.buff( Prayers.class );
		long made = prayers == null ? 0 : (long)prayers.count();

		ArrayList<Class<? extends Scroll>> result = new ArrayList<>();
		Random.pushGenerator( Dungeon.seed + 0x5EA1ED000L + made );
		for (int i = 0; i < 2; i++) {
			int pick = Random.chances( odds );
			odds[pick] = 0;
			@SuppressWarnings("unchecked")
			Class<? extends Scroll> cls = (Class<? extends Scroll>) Generator.Category.SCROLL.classes[pick];
			result.add( cls );
		}
		Random.popGenerator();
		return result;
	}

	public Scroll pray( Hero hero, int pick ) {
		Scroll result = Reflection.newInstance( choices().get(pick) );

		detach( hero.belongings.backpack );
		Buff.affect( hero, Prayers.class ).countUp( 1 );
		Catalog.countUse( getClass() );

		result.identify();
		GLog.p( Messages.get(this, "turned", result.name()) );
		if (!result.collect( hero.belongings.backpack )) {
			Heap heap = Dungeon.level.drop( result, hero.pos );
			if (heap.sprite != null) heap.sprite.drop();
		}

		Invisibility.dispel();
		hero.spend( TIME_TO_PRAY );
		hero.busy();
		if (hero.sprite instanceof HeroSprite) ((HeroSprite)hero.sprite).read();
		Sample.INSTANCE.play( Assets.Sounds.READ );
		return result;
	}

	private WndOptions window( final Hero hero ) {
		//anonymous copies show their real names without teaching you the runes
		final ArrayList<Scroll> shown = new ArrayList<>();
		for (Class<? extends Scroll> cls : choices()) {
			Scroll s = Reflection.newInstance( cls );
			s.anonymize();
			shown.add( s );
		}

		return new WndOptions( new ItemSprite(this),
				Messages.titleCase( name() ),
				Messages.get(this, "prompt"),
				Messages.titleCase( shown.get(0).name() ),
				Messages.titleCase( shown.get(1).name() ) ) {
			@Override
			protected boolean hasIcon( int index ) {
				return true;
			}

			@Override
			protected Image getIcon( int index ) {
				return new ItemSprite( shown.get(index) );
			}

			@Override
			protected boolean hasInfo( int index ) {
				return true;
			}

			@Override
			protected void onInfo( int index ) {
				GameScene.show( new WndInfoItem( shown.get(index) ) );
			}

			@Override
			protected void onSelect( int index ) {
				if (hero.belongings.contains( SealedScroll.this ) && canPray( hero )) {
					pray( hero, index );
				}
			}
		};
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public int value() {
		return 30 * quantity;
	}

	@Override
	public int energyVal() {
		return 6 * quantity;
	}

	//how many scrolls have been opened this run
	public static class Prayers extends CounterBuff {
		{
			revivePersists = true;
		}
	}

}
