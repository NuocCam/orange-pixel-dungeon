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
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfAccuracy;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfArcana;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfElements;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEnergy;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEvasion;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfFuror;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfHaste;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfMight;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfSharpshooting;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfTenacity;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfWealth;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Arrays;

public class AscendantCitrine extends Artifact {

	{
		image = ItemSpriteSheet.ARTIFACT_CITRINE1;

		levelCap = 10;

		charge = 0;

		defaultAction = AC_REROLL;
	}

	public static final String AC_REROLL = "REROLL";

	//a third of what a normal ring gives
	public static final float STRENGTH = 1/3f;

	//no ring of force here, it takes over unarmed attacks and the duelist stuff isnt worth the mess
	private static final Class<?>[] POOL = {
			RingOfAccuracy.class,
			RingOfArcana.class,
			RingOfElements.class,
			RingOfEnergy.class,
			RingOfEvasion.class,
			RingOfFuror.class,
			RingOfHaste.class,
			RingOfMight.class,
			RingOfSharpshooting.class,
			RingOfTenacity.class,
			RingOfWealth.class
	};
	private static final String[] KEYS = {
			"accuracy", "arcana", "elements", "energy", "evasion", "furor",
			"haste", "might", "sharpshooting", "tenacity", "wealth"
	};
	private static final double[] BASE = {
			1.3, 1.175, 0.825, 1.175, 1.125, 1.09051,
			1.15, 1.05, 1.25, 0.85, 1.2
	};

	private Class<? extends Ring> ringClass;
	private Ring ring;

	@SuppressWarnings("unchecked")
	private void ensureRing() {
		if (ringClass == null) {
			ringClass = (Class<? extends Ring>) Random.element(POOL);
		}
		if (ring == null) {
			buildRing();
		}
	}

	private void buildRing() {
		if (ring != null) ring.deactivate();
		ring = Reflection.newInstance(ringClass);
		ring.effectScale = STRENGTH;
		ring.citrine = this;
		ring.level(level());
	}

	private int ringIndex() {
		return Arrays.asList(POOL).indexOf(ringClass);
	}

	private static AscendantCitrine worn(Hero hero) {
		if (hero == null) return null;
		if (hero.belongings.artifact instanceof AscendantCitrine) return (AscendantCitrine) hero.belongings.artifact;
		if (hero.belongings.misc instanceof AscendantCitrine) return (AscendantCitrine) hero.belongings.misc;
		return null;
	}

	public static float buffedBonus(Hero hero, Class<? extends Ring> type) {
		AscendantCitrine citrine = worn(hero);
		if (citrine == null || citrine.ring == null || citrine.ring.getClass() != type) return 0;
		if (hero.buff(MagicImmune.class) != null) return 0;
		return citrine.ring.soloBuffedBonusF();
	}

	private void updateImage() {
		if (level() >= 10)     image = ItemSpriteSheet.ARTIFACT_CITRINE3;
		else if (level() >= 5) image = ItemSpriteSheet.ARTIFACT_CITRINE2;
		else                   image = ItemSpriteSheet.ARTIFACT_CITRINE1;
	}

	@Override
	public Item upgrade() {
		super.upgrade();
		updateImage();
		//transmuted ones already have a ring when their levels get handed over
		if (ring != null) ring.level(level());
		return this;
	}

	//cursed ones do nothing and keep rings off your fingers
	public static boolean blocksRings(Hero hero) {
		AscendantCitrine citrine = worn(hero);
		return citrine != null && citrine.cursed;
	}

	@Override
	public Item random() {
		ensureRing();
		if (Random.Float() < 0.3f) {
			cursed = true;
		}
		return this;
	}

	@Override
	public void activate(Char ch) {
		super.activate(ch);
		ensureRing();
		ring.activate(ch);
		if (ch == Dungeon.hero && Dungeon.hero != null) {
			Dungeon.hero.updateHT(false);
		}
	}

	@Override
	public boolean doUnequip(Hero hero, boolean collect, boolean single) {
		if (super.doUnequip(hero, collect, single)) {
			if (ring != null) ring.deactivate();
			hero.updateHT(false);
			return true;
		}
		return false;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		if (isEquipped(hero) && charge > 0) {
			actions.add(AC_REROLL);
		}
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);

		if (action.equals(AC_REROLL)) {
			if (!isEquipped(hero)) {
				GLog.i(Messages.get(Artifact.class, "need_to_equip"));
			} else if (cursed) {
				GLog.w(Messages.get(this, "cursed"));
			} else if (hero.buff(MagicImmune.class) != null) {
				GLog.w(Messages.get(this, "magic_immune"));
			} else if (charge <= 0) {
				GLog.i(Messages.get(this, "no_charge"));
			} else {
				reroll(hero);
			}
		}
	}

	private void reroll(Hero hero) {
		ensureRing();

		ArrayList<Class<?>> options = new ArrayList<>(Arrays.asList(POOL));
		options.remove(ringClass);
		@SuppressWarnings("unchecked")
		Class<? extends Ring> next = (Class<? extends Ring>) Random.element(options);

		charge--;
		if (level() < levelCap) {
			upgrade();
		}

		ringClass = next;
		buildRing();
		ring.activate(hero);
		hero.updateHT(false);

		if (level() >= levelCap) {
			GLog.p(Messages.get(this, "rerolled_max", ringName()));
		} else {
			GLog.p(Messages.get(this, "rerolled", ringName(), level()));
		}

		Sample.INSTANCE.play(Assets.Sounds.BEACON);
		hero.sprite.operate(hero.pos);
		updateQuickslot();
		hero.spendAndNext(Actor.TICK);
	}

	//only level ups charge this, so scrolls, meals and the like just get ignored
	@Override
	public void charge(Hero target, float amount) {
	}

	public static void onHeroLevelUp(Hero hero) {
		AscendantCitrine citrine = worn(hero);
		if (citrine != null) {
			citrine.gainFromLevel(hero);
		}
	}

	private void gainFromLevel(Hero hero) {
		if (cursed || hero.buff(MagicImmune.class) != null) return;

		partialCharge += rechargeSpeedMultiplier(hero);
		int gained = (int) partialCharge;
		if (gained > 0) {
			partialCharge -= gained;
			charge += gained;
			GLog.p(Messages.get(this, "charged"));
			updateQuickslot();
		}
	}

	private String ringName() {
		return Messages.titleCase(Messages.get(ringClass, "name"));
	}

	private float currentBonus() {
		//no hero in the journal, and the ring needs one to work out its level
		if (ring != null && Dungeon.hero != null) return ring.soloBuffedBonusF();
		return (level() + 1) * STRENGTH;
	}

	private String effectText() {
		int i = ringIndex();
		float bonus = currentBonus();
		double pct = BASE[i] > 1 ? 100.0 * (Math.pow(BASE[i], bonus) - 1.0)
				: 100.0 * (1.0 - Math.pow(BASE[i], bonus));
		return Messages.get(this, "fx_" + KEYS[i], Messages.decimalFormat("#.##", pct), (int) bonus);
	}

	@Override
	public String status() {
		if (!isIdentified()) return null;
		return Integer.toString(charge);
	}

	@Override
	public String desc() {
		String desc = super.desc();

		//journal copies never rolled a ring, so keep it generic
		if (ringClass == null) {
			return desc + "\n\n" + Messages.get(this, "desc_unknown");
		}

		desc += "\n\n" + Messages.get(this, "desc_ring",
				ringName(),
				Messages.decimalFormat("#.##", currentBonus()),
				level());
		desc += " " + effectText();

		if (level() >= levelCap) {
			desc += "\n\n" + Messages.get(this, "desc_max");
		}

		if (isEquipped(Dungeon.hero)) {
			if (cursed) {
				desc += "\n\n" + Messages.get(this, "desc_cursed");
			} else {
				desc += "\n\n" + Messages.get(this, "desc_charge", charge);
			}
		}
		return desc;
	}

	@Override
	public int value() {
		return 0;
	}

	@Override
	protected ArtifactBuff passiveBuff() {
		return new CitrineBuff();
	}

	public class CitrineBuff extends ArtifactBuff {
	}

	private static final String RING_CLASS = "ring_class";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		if (ringClass != null) {
			bundle.put(RING_CLASS, ringClass.getName());
		}
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		if (bundle.contains(RING_CLASS)) {
			String name = bundle.getString(RING_CLASS);
			for (Class<?> c : POOL) {
				if (c.getName().equals(name)) {
					ringClass = (Class<? extends Ring>) c;
				}
			}
		}
		updateImage();
	}
}
