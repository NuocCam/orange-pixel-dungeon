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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

import java.util.ArrayList;

public class RunicHammer extends MeleeWeapon {

	{
		image = ItemSpriteSheet.RUNIC_HAMMER;
		hitSound = Assets.Sounds.HIT_CRUSH;
		hitSoundPitch = 1f;

		tier = 5;
	}

	//like the runic blade, a weak start but better scaling
	//equal to a normal tier 5 weapon between +3 and +4

	@Override
	public int max(int lvl) {
		return  5*(tier) - 2 +          //23 base, down from 30
				lvl*(tier+3);           //+8 per level, up from +6
	}

	@Override
	protected int baseChargeUse(Hero hero, Char target){
		return 2;
	}

	@Override
	protected void duelistAbility(Hero hero, Integer target) {

		//picked before the first hit, as an enchantment may move enemies around
		ArrayList<Char> targets = new ArrayList<>();
		for (Char ch : Actor.chars()){
			if (ch.alignment == Char.Alignment.ENEMY
					&& !hero.isCharmedBy(ch)
					&& Dungeon.level.adjacent(hero.pos, ch.pos)){
				targets.add(ch);
			}
		}

		if (targets.isEmpty()) {
			GLog.w(Messages.get(this, "ability_no_target"));
			return;
		}

		throwSound();
		hero.sprite.attack(targets.get(0).pos, new Callback() {
			@Override
			public void call() {
				beforeAbilityUsed(hero, targets.get(0));
				for (Char ch : targets) {
					//an enchantment on an earlier hit may have killed this one, or us
					if (!ch.isAlive() || !hero.isAlive()){
						continue;
					}
					//the tracker is used up by each enchantment roll, so every hit needs its own
					RunicBlade.RunicSlashTracker tracker = Buff.affect(hero, RunicBlade.RunicSlashTracker.class);
					tracker.boost = 3f + 0.40f*buffedLvl();
					if (hero.attack(ch, 1f, 0, Char.INFINITE_ACCURACY)){
						Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG);
						if (!ch.isAlive()){
							onAbilityKill(hero, ch);
						}
					}
					tracker.detach();
				}
				Invisibility.dispel();
				hero.spendAndNext(hero.attackDelay());
				afterAbilityUsed(hero);
			}
		});
	}

	@Override
	public String abilityInfo() {
		if (levelKnown){
			return Messages.get(this, "ability_desc", 300+40*buffedLvl());
		} else {
			return Messages.get(this, "typical_ability_desc", 300);
		}
	}

	@Override
	public String upgradeAbilityStat(int level) {
		return "+" + (300+40*level) + "%";
	}

}
