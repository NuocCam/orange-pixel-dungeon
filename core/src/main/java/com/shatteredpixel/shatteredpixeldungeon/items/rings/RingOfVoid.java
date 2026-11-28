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

package com.shatteredpixel.shatteredpixeldungeon.items.rings;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.DeathMark;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Brute;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

public class RingOfVoid extends Ring {

	{
		icon = ItemSpriteSheet.Icons.RING_VOID;
		buffClass = Devour.class;
	}

	//foes below this share of their health get swallowed
	private static float threshold( float bonus ){
		return Math.min( 0.40f, (3f + bonus)/40f );
	}

	//cursed only, the chance a hit feeds the foe instead
	private static float healChance( float bonus ){
		return Math.min( 1f, 0.125f * Math.max( 0f, -bonus ) );
	}

	//called by Char.attack once all of a landed hit's damage is done
	public static void afterHit( Char attacker, Char enemy, int damage ){
		if (attacker != Dungeon.hero || damage <= 0) return;
		float bonus = getBuffedBonusF( attacker, Devour.class );
		//not isAlive, a downed ghoul counts as alive but isnt active
		if (bonus == 0 || !enemy.isActive()) return;

		if (bonus < 0){
			//at 0 HP only something like rage holds it up, healing would undo that
			if (enemy.HP > 0 && Random.Float() < healChance( bonus )){
				enemy.heal( Math.max( 1, damage/2 ) );
			}

		} else if (enemy.alignment == Char.Alignment.ENEMY
				&& !Char.hasProp( enemy, Char.Property.BOSS )
				&& !Char.hasProp( enemy, Char.Property.MINIBOSS )
				&& !Char.hasProp( enemy, Char.Property.OBJECT )
				&& !enemy.isInvulnerable( attacker.getClass() )
				//at 0 HP its raging, death marked or such, those have to play out
				&& enemy.HP > 0
				&& enemy.HP/(float)enemy.HT < threshold( bonus )){
			swallow( attacker, enemy );
		}
	}

	//same steps as the assassination in Char.attack
	private static void swallow( Char attacker, Char enemy ){
		enemy.HP = 0;
		for (Buff b : enemy.buffs( Brute.BruteRage.class )){
			b.detach();
		}
		if (!enemy.isAlive()) {
			enemy.die( attacker );
		} else {
			//helps with triggering any on-damage effects that need to activate
			enemy.damage( -1, attacker );
			DeathMark.processFearTheReaper( enemy );
		}
		if (enemy.sprite != null) {
			enemy.sprite.showStatus( CharSprite.NEGATIVE, Messages.get( RingOfVoid.class, "swallowed" ) );
		}
	}

	@Override
	protected RingBuff buff( ) {
		return new Devour();
	}

	public class Devour extends RingBuff {}

	@Override
	public String statsInfo() {
		if (isIdentified()){
			String info = describe( "stats", "cursed_stats", soloBuffedBonus() );

			if (isEquipped(Dungeon.hero) && soloBuffedBonus() != combinedBuffedBonusF(Dungeon.hero)){
				info += "\n\n" + describe("combined_stats", "combined_cursed_stats", combinedBuffedBonusF(Dungeon.hero) );
			}

			return info;
		} else {
			return Messages.get(this, "typical_stats", percent(threshold(1)));
		}
	}

	private String describe( String good, String bad, float bonus ){
		return bonus > 0
				? Messages.get(this, good, percent(threshold(bonus)))
				: Messages.get(this, bad, percent(healChance(bonus)));
	}

	private static String percent( float share ){
		return Messages.decimalFormat("#.##", 100f * share);
	}

	@Override
	public String upgradeStat1(int level){
		if (cursed && cursedKnown) level = Math.min(-1, level-3);
		return percent(level+1 > 0 ? threshold(level+1) : healChance(level+1)) + "%";
	}

}
