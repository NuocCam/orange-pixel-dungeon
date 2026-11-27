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
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Healing;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Waterskin;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.PotionBandolier;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.VialOfBlood;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class MedicKit extends Artifact {

	{
		image = ItemSpriteSheet.ARTIFACT_MEDIC_KIT;

		levelCap = 10;

		charge = 0;

		defaultAction = AC_USE;
	}

	public static final String AC_USE = "USE";
	public static final String AC_ADD = "ADD";

	public static final int DEW_FOR_LEVEL = 20;

	public int maxCharge() {
		return Math.min(10, level() + 1);
	}

	public float healPercent() {
		if (level() >= 10)     return 15f;
		else if (level() >= 5) return 12.5f;
		else                   return 10f;
	}

	private String healPercentString() {
		float p = healPercent();
		return p == (int)p ? Integer.toString((int)p) : Float.toString(p);
	}

	private float turnsPerCharge() {
		return 330f - 16.5f*level();
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if (isEquipped( hero )) {
			if (charge > 0) {
				actions.add(AC_USE);
			}
			if (level() < levelCap) {
				actions.add(AC_ADD);
			}
		}
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {
		super.execute(hero, action);

		if (action.equals(AC_ADD)) {
			GameScene.selectItem( itemSelector );
		}

		if (action.equals(AC_USE)) {
			if (!isEquipped(hero)) {
				GLog.i( Messages.get(Artifact.class, "need_to_equip") );
			} else if (cursed) {
				GLog.w( Messages.get(this, "cursed") );
			} else if (hero.buff(MagicImmune.class) != null) {
				GLog.w( Messages.get(this, "magic_immune") );
			} else if (charge <= 0) {
				GLog.i( Messages.get(this, "no_charge") );
			} else {
				chooseUse(hero);
			}
		}
	}

	private int chargesNeeded(Hero hero) {
		float missing = hero.HT - hero.HP;
		return (int)Math.ceil(missing / healPerCharge(hero) - 0.001f);
	}

	private float healPerCharge(Hero hero) {
		return hero.HT * healPercent() / 100f * VialOfBlood.totalHealMultiplier();
	}

	private void chooseUse(final Hero hero) {
		final int needed = chargesNeeded(hero);

		if (Dungeon.isChallenged(Challenges.NO_HEALING)) {
			useKit(hero, charge, false);
		} else if (level() >= 6 && needed > 0 && charge > needed) {
			GameScene.show(new WndOptions(
					new ItemSprite(this),
					Messages.titleCase(name()),
					Messages.get(this, "choose_prompt", charge, needed),
					Messages.get(this, "choose_heal", needed),
					Messages.get(this, "choose_all", charge)) {
				@Override
				protected void onSelect(int index) {
					if (index == 0) {
						useKit(hero, needed, true);
					} else if (index == 1) {
						useKit(hero, charge, true);
					}
				}
			});
		} else if (needed <= 0) {
			if (level() >= 6) {
				useKit(hero, charge, true);
			} else {
				GLog.i(Messages.get(this, "full_health"));
			}
		} else {
			useKit(hero, Math.min(charge, needed), true);
		}
	}

	private void useKit(Hero hero, int used, boolean canHeal) {
		used = Math.min(used, charge);
		if (used <= 0) return;

		int heal = 0;
		int shield = 0;

		if (!canHeal || Dungeon.isChallenged(Challenges.NO_HEALING)) {
			shield = Math.round(hero.HT * healPercent() * used / 100f);
		} else {
			float perCharge = healPerCharge(hero);
			heal = Math.min(Math.round(perCharge * used), hero.HT - hero.HP);
			if (level() >= 6) {
				float chargesForHeal = heal / perCharge;
				shield = Math.round(hero.HT * healPercent() / 100f * Math.max(0f, used - chargesForHeal));
			}
		}

		charge -= used;
		if (charge < maxCharge()) {
		} else {
			partialCharge = 0;
		}

		if (shield > 0) {
			Buff.affect(hero, Barrier.class).incShield(shield);
			hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(shield), FloatingText.SHIELDING);
		}

		if (heal > 0) {
			if (level() >= 10 && !VialOfBlood.delayBurstHealing()) {
				hero.heal(heal);
			} else {
				float rate = level() >= 10 ? 1f : level() >= 3 ? 0.25f : 0.1f;
				Healing healing = Buff.affect(hero, Healing.class);
				healing.setHeal(heal, rate, 0);
				healing.limitByBloodVial();
			}
		}

		Sample.INSTANCE.play(Assets.Sounds.DRINK);
		hero.sprite.operate(hero.pos);
		updateQuickslot();

		if (level() >= 10) {
			hero.next();
		} else {
			hero.spendAndNext(Actor.TICK);
		}
	}

	@Override
	public void charge(Hero target, float amount) {
		if (target.buff(MagicImmune.class) != null) return;
		if (charge < maxCharge()) {
			partialCharge += amount * rechargeSpeedMultiplier(target) / turnsPerCharge();
			while (partialCharge >= 1 && charge < maxCharge()) {
				partialCharge--;
				charge++;
			}
			if (charge >= maxCharge()) {
				charge = maxCharge();
				partialCharge = 0;
			}
			updateQuickslot();
		}
	}

	public static boolean cursedProc(Char ch, int healAmount) {
		if (ch != Dungeon.hero || healAmount <= 0) return false;
		Hero hero = Dungeon.hero;
		MedicKit kit = null;
		if (hero.belongings.artifact instanceof MedicKit) kit = (MedicKit) hero.belongings.artifact;
		else if (hero.belongings.misc instanceof MedicKit) kit = (MedicKit) hero.belongings.misc;
		if (kit == null || !kit.cursed || hero.buff(MagicImmune.class) != null) return false;

		Buff.affect(hero, Poison.class).set(Math.max(1, healAmount / 2));
		GLog.n(Messages.get(MedicKit.class, "cursed_heal"));
		return true;
	}

	public void ankhLevelUp() {
		addLevels(1);
	}

	private void addLevels(int levels) {
		if (levels <= 0 || level() >= levelCap) return;
		levels = Math.min(levels, levelCap - level());
		upgrade(levels);
		GLog.p(Messages.get(this, "upgrade", level()));
		updateQuickslot();
	}

	@Override
	public String status() {
		return Messages.format("%d/%d", charge, maxCharge());
	}

	@Override
	public String desc() {
		String desc = super.desc();

		desc += "\n\n" + Messages.get(this, "desc_levels", maxCharge(), healPercentString());

		if (isEquipped( Dungeon.hero )) {
			if (cursed) {
				desc += "\n\n" + Messages.get(this, "desc_cursed");
			} else {
				desc += "\n\n" + Messages.get(this, "desc_charge", charge, maxCharge());
			}
		}
		return desc;
	}

	private final WndBag.ItemSelector itemSelector = new WndBag.ItemSelector() {

		@Override
		public String textPrompt() {
			return Messages.get(MedicKit.class, "inv_title");
		}

		@Override
		public Class<? extends Bag> preferredBag() {
			//that's where the healing potions are. Without one, whichever bag was open last
			return Dungeon.hero.belongings.getItem(PotionBandolier.class) != null
					? PotionBandolier.class : null;
		}

		@Override
		public boolean itemSelectable(Item item) {
			if (item instanceof PotionOfHealing) {
				return item.isIdentified();
			}
			return item instanceof Waterskin && ((Waterskin) item).isFull();
		}

		@Override
		public void onSelect( Item item ) {
			if (item == null || !itemSelectable(item)) return;

			if (item instanceof Waterskin) {
				((Waterskin) item).empty();
			} else {
				item.detach(Dungeon.hero.belongings.backpack);
			}

			Dungeon.hero.sprite.operate(Dungeon.hero.pos);
			Sample.INSTANCE.play(Assets.Sounds.DRINK);
			addLevels(1);
			Dungeon.hero.spendAndNext(Actor.TICK);
		}
	};

	@Override
	public int value() {
		return 0;
	}

	@Override
	protected ArtifactBuff passiveBuff() {
		return new MedicKitRecharge();
	}

	public class MedicKitRecharge extends ArtifactBuff {

		@Override
		public boolean act() {
			if (target instanceof Hero) {
				MedicKit.this.charge((Hero) target, 1f);
			}
			spend( TICK );
			return true;
		}
	}
}
