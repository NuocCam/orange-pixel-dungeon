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

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class MagicOrange extends Food {

	{
		image = ItemSpriteSheet.MAGIC_ORANGE;
		energy = Hunger.STARVING/3f; //a third of the hunger bar, 150 food value

		bones = false;
	}

	@Override
	protected void satisfy(Hero hero) {
		super.satisfy(hero);

		//same cleanse as a healing potion or mageroyal
		PotionOfHealing.cure(hero);

		//1/6 of max HP, healed right away. Only show what actualy got restored
		int heal = Math.min(Math.round(hero.HT / 6f), hero.HT - hero.HP);
		if (heal > 0) {
			hero.HP += heal;
			hero.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString(heal), FloatingText.HEALING );
		}
	}

	@Override
	public int value() {
		return 20 * quantity;
	}

}
