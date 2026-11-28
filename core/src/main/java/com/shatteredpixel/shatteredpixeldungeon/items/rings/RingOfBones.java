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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.HoldFast;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ShieldBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;

public class RingOfBones extends Ring {

	{
		icon = ItemSpriteSheet.Icons.RING_BONES;
		buffClass = Harvest.class;
	}

	//the shield can be filled up to this many kills worth
	private static final int KILLS_TO_CAP = 3;

	private static int shieldPerKill( float bonus ){
		return Math.round(2 + bonus);
	}

	public static void onEnemySlain( Hero hero, Mob mob ){
		//a deathless berserker lives only while shielded, kills must not stretch that out
		if (hero.HP <= 0) return;

		float bonus = getBuffedBonusF(hero, Harvest.class);
		if (bonus > 0){
			int per = shieldPerKill(bonus);
			BoneShield shield = Buff.affect(hero, BoneShield.class);
			int before = shield.shielding();
			//setShield never lowers, so a weaker ring leaves a bigger shield alone
			shield.setShield(Math.min(KILLS_TO_CAP*per, before + per));
			if (shield.shielding() > before){
				hero.sprite.showStatusWithIcon(CharSprite.POSITIVE,
						Integer.toString(shield.shielding() - before), FloatingText.SHIELDING);
			}
		} else if (bonus < 0){
			//stops at 1 HP. A deathless berserker may already be at 0, and mustnt be raised
			int loss = Math.min(Math.round(-bonus), hero.HP - 1);
			if (loss > 0){
				//not damage(), so nothing that reacts to being hit goes off
				hero.HP -= loss;
				hero.sprite.showStatusWithIcon(CharSprite.NEGATIVE,
						Integer.toString(loss), FloatingText.PHYS_DMG_NO_BLOCK);
				GLog.n(Messages.get(RingOfBones.class, "drain", loss));
			}
		}
	}

	@Override
	protected RingBuff buff( ) {
		return new Harvest();
	}

	public class Harvest extends RingBuff {}

	//its own shield so it stacks with barrier and the warrior's seal
	public static class BoneShield extends ShieldBuff {

		{
			type = buffType.POSITIVE;
		}

		//1 point is lost every 4 turns
		private static final float LOSS_PER_TURN = 0.25f;

		private float partialLostShield;

		@Override
		public boolean act() {
			//hold fast slows this like it does other shields
			partialLostShield += LOSS_PER_TURN * HoldFast.buffDecayFactor(target);
			if (partialLostShield >= 1f){
				partialLostShield = 0;
				absorbDamage(1); //detaches at 0
			}
			spend( TICK );
			return true;
		}

		@Override
		public int icon() {
			return BuffIndicator.ARMOR;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(2.2f, 2f, 1.5f);
		}

		@Override
		public String iconTextDisplay() {
			return Integer.toString(shielding());
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", shielding());
		}

		private static final String PARTIAL_LOST_SHIELD = "partial_lost_shield";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(PARTIAL_LOST_SHIELD, partialLostShield);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			partialLostShield = bundle.getFloat(PARTIAL_LOST_SHIELD);
		}
	}

	@Override
	public String statsInfo() {
		if (isIdentified()){
			String info = describe("stats", "cursed_stats", soloBuffedBonus());

			if (isEquipped(Dungeon.hero) && soloBuffedBonus() != combinedBuffedBonusF(Dungeon.hero)){
				info += "\n\n" + describe("combined_stats", "combined_cursed_stats", combinedBuffedBonusF(Dungeon.hero));
			}

			return info;
		} else {
			return Messages.get(this, "typical_stats", shieldPerKill(1), KILLS_TO_CAP*shieldPerKill(1));
		}
	}

	//a bonus of 0 or less is a curse, which costs health instead
	private String describe( String good, String cursedKey, float bonus ){
		if (bonus > 0){
			return Messages.get(this, good, shieldPerKill(bonus), KILLS_TO_CAP*shieldPerKill(bonus));
		} else {
			return Messages.get(this, cursedKey, Math.round(-bonus));
		}
	}

	@Override
	public String upgradeStat1(int level){
		if (cursed && cursedKnown) level = Math.min(-1, level-3);
		int bonus = level+1;
		return Integer.toString(bonus > 0 ? shieldPerKill(bonus) : bonus);
	}

}
