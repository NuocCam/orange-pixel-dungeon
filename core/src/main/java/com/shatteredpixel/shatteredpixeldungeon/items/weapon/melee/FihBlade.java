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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.AttackIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

public class FihBlade extends MeleeWeapon {

	{
		image = ItemSpriteSheet.FIH_BLADE;
		hitSound = Assets.Sounds.HIT_SLASH;
		hitSoundPitch = 1.2f;

		tier = 3;
		DLY = 2/3f; //1.5x speed
	}

	@Override
	public int max(int lvl) {
		return  3*(tier+1) +    //12 base, down from 20
				lvl*(tier-1);   //+2 per level, down from +4, the bleed makes up for it
	}

	@Override
	public int proc(Char attacker, Char defender, int damage) {
		if (Random.Float() < 0.25f){
			//set() keeps only the stronger bleed, so these never stack
			Buff.affect(defender, Bleeding.class).set(bleedStrength(buffedLvl()));
		}
		return super.proc(attacker, defender, damage);
	}

	private static int bleedStrength(int lvl){
		return 3 + lvl;
	}

	//what a bleed will still deal on average. totalIncomingDMG() says 4x the level, but the
	//last ticks round down to nothing, which is about 1.17 less in practise
	private static int remainingBleed(Bleeding bleed){
		return Math.max(Math.round(bleed.level()), Math.round(4f*bleed.level() - 1.17f));
	}

	@Override
	public String targetingPrompt() {
		return Messages.get(this, "prompt");
	}

	@Override
	protected void duelistAbility(Hero hero, Integer target) {
		if (target == null) {
			return;
		}

		Char enemy = Actor.findChar(target);
		if (enemy == null || enemy == hero || hero.isCharmedBy(enemy) || !Dungeon.level.heroFOV[target]) {
			GLog.w(Messages.get(this, "ability_no_target"));
			return;
		}

		if (enemy.buff(Bleeding.class) == null) {
			GLog.w(Messages.get(this, "ability_no_bleed"));
			return;
		}

		hero.belongings.abilityWeapon = this;
		if (!hero.canAttack(enemy)){
			GLog.w(Messages.get(this, "ability_target_range"));
			hero.belongings.abilityWeapon = null;
			return;
		}
		hero.belongings.abilityWeapon = null;

		hero.sprite.attack(enemy.pos, new Callback() {
			@Override
			public void call() {
				beforeAbilityUsed(hero, enemy);
				AttackIndicator.target(enemy);
				if (hero.attack(enemy, 1f, 0, Char.INFINITE_ACCURACY)){
					Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG);
					//read after the strike, so a bleed it just caused counts too
					Bleeding bleed = enemy.buff(Bleeding.class);
					if (enemy.isAlive() && bleed != null){
						int dmg = remainingBleed(bleed);
						bleed.detach();
						enemy.damage(dmg, bleed);
					}
					if (!enemy.isAlive()){
						onAbilityKill(hero, enemy);
					}
				}
				Invisibility.dispel();
				hero.spendAndNext(hero.attackDelay());
				afterAbilityUsed(hero);
			}
		});
	}

	@Override
	public String abilityInfo() {
		return Messages.get(this, levelKnown ? "ability_desc" : "typical_ability_desc");
	}

	//gut rip has no number of its own, the bleed is what grows with upgrades
	@Override
	public String upgradeAbilityStat(int level) {
		return Integer.toString(bleedStrength(level));
	}

}
