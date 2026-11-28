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

package com.shatteredpixel.shatteredpixeldungeon.items.food;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Honeypot;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import java.util.ArrayList;

//a whole pie. Not food on purpose, you have to cut it up before anyone can eat it
public class JungleBerryPie extends Item {

	public static final String AC_CUT = "CUT";

	private static final int SLICES = 4;

	{
		stackable = true;
		image = ItemSpriteSheet.JUNGLE_PIE;

		defaultAction = AC_CUT;

		bones = true;
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_CUT );
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {

		super.execute( hero, action );

		if (action.equals( AC_CUT )) {

			detach( hero.belongings.backpack );
			Catalog.countUse(getClass());

			Item slices = new JungleBerryPieSlice().quantity(SLICES);
			if (!slices.collect()) {
				Dungeon.level.drop(slices, hero.pos).sprite.drop();
			}
			GLog.i( Messages.get(this, "cut_msg") );

			hero.sprite.operate( hero.pos );
			hero.busy();
			hero.spend( 1f );
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

	@Override
	public int value() {
		return 50 * quantity;
	}

	public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe.SimpleRecipe {

		{
			inputs = new Class[]{JungleBerry.class, Honeypot.ShatteredPot.class, MeatPie.class};
			inQuantity = new int[]{1, 1, 1};

			cost = 2;

			output = JungleBerryPie.class;
			outQuantity = 1;
		}

	}

}
