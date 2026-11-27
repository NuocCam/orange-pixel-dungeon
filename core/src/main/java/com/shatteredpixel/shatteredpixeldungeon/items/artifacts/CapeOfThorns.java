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

package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class CapeOfThorns extends Artifact {

	{
		image = ItemSpriteSheet.ARTIFACT_CAPE;

		levelCap = 10;

		charge = 0;
		chargeCap = 100;
		cooldown = 0;

		defaultAction = AC_ACTIVATE;
	}

	public static final String AC_ACTIVATE = "ACTIVATE";

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		if (isEquipped(hero) && cooldown == 0 && charge >= chargeCap) {
			actions.add(AC_ACTIVATE);
		}
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);

		if (action.equals(AC_ACTIVATE)) {
			if (!isEquipped(hero)) {
				GLog.i(Messages.get(Artifact.class, "need_to_equip"));
			} else if (cooldown > 0 || charge < chargeCap) {
				GLog.i(Messages.get(this, "not_charged"));
			} else {
				//takes no time, so it can be saved for the right moment
				charge = 0;
				partialCharge = 0;
				cooldown = ((Thorns)passiveBuff).duration();
				Sample.INSTANCE.play(Assets.Sounds.CHARGEUP);
				GLog.p(Messages.get(Thorns.class, "radiating"));
				updateQuickslot();
				//no turn goes by, so the buff icon has to be told
				BuffIndicator.refreshHero();
			}
		}
	}

	private static final String COOLDOWN = "cooldown";

	//the turns left while it's switched on
	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(COOLDOWN, cooldown);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		//older saves don't have it, which reads as 0
		cooldown = bundle.getInt(COOLDOWN);
	}

	//fills up and then waits to be used
	private void gain(int amount) {
		if (charge >= chargeCap) return;
		charge = Math.min(chargeCap, charge + amount);
		if (charge == chargeCap) {
			GLog.p(Messages.get(Thorns.class, "ready"));
		}
	}

	@Override
	protected ArtifactBuff passiveBuff() {
		return new Thorns();
	}
	
	@Override
	public void charge(Hero target, float amount) {
		if (cooldown == 0) {
			gain(Math.round(4*amount));
			updateQuickslot();
		}
	}
	
	@Override
	public String desc() {
		String desc = Messages.get(this, "desc");
		if (isEquipped( Dungeon.hero )) {
			desc += "\n\n";
			if (cooldown == 0)
				desc += Messages.get(this, "desc_inactive");
			else
				desc += Messages.get(this, "desc_active");
		}

		return desc;
	}

	public class Thorns extends ArtifactBuff{

		@Override
		public boolean act(){
			if (cooldown > 0) {
				cooldown--;
				if (cooldown == 0) {
					GLog.w( Messages.get(this, "inert") );
				}
				updateQuickslot();
			}
			spend(TICK);
			return true;
		}

		//how much damage fills the cape: all of max HP at +0, 4% less each level
		public int chargeNeeded(Hero hero){
			return Math.max(1, Math.round(hero.HT * (1 - 0.04f*level())));
		}

		public float blockFraction(){
			return 0.40f + 0.02f*level();
		}

		public float passiveFraction(){
			return 0.15f + 0.01f*level();
		}

		public int proc(int damage, Object src, Char defender){
			if (damage <= 0) return damage;

			Char attacker = src instanceof Char ? (Char)src : null;
			boolean adjacent = attacker != null && Dungeon.level.adjacent(attacker.pos, defender.pos);
			int returned = 0;

			if (cooldown == 0){
				//starving doesnt count, or you could just wait it out
				if (!(src instanceof Hunger)){
					partialCharge += damage * 100f / chargeNeeded((Hero)defender);
					int whole = (int)partialCharge;
					partialCharge -= whole;
					gain(whole);
					if (charge >= chargeCap) partialCharge = 0;
				}
				if (adjacent && !cursed){
					returned = Math.max(1, Math.round(damage * passiveFraction()));
				}
			} else {
				int blocked = Math.round(damage * blockFraction());
				damage -= blocked;
				if (adjacent) returned = blocked;
			}

			if (returned > 0){
				attacker.damage(returned, this);

				exp += returned;
				if (exp >= (level()+1)*5 && level() < levelCap){
					exp -= (level()+1)*5;
					upgrade();
					Catalog.countUse(CapeOfThorns.class);
					GLog.p( Messages.get(this, "levelup") );
				}
			}
			updateQuickslot();
			return damage;
		}

		public int duration(){
			return 10 + level()/2;
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", dispTurns(cooldown));
		}

		@Override
		public int icon() {
			if (cooldown == 0)
				return BuffIndicator.NONE;
			else
				return BuffIndicator.THORNS;
		}

		@Override
		public void detach(){
			cooldown = 0;
			charge = 0;
			super.detach();
		}

	}


}
