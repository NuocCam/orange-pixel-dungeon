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

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

//the hex button, and the power a cultist draws from the cursed gear they wear.
//Clerics dont get this buff, they cast hex from their dark tome
public class CultistPower extends Buff implements ActionIndicator.Action {

	{
		//there from the moment the mask goes on
		revivePersists = true;
	}

	@Override
	public boolean attachTo( Char target ) {
		if (super.attachTo(target)){
			ActionIndicator.setAction(this);
			return true;
		}
		return false;
	}

	@Override
	public void detach() {
		super.detach();
		ActionIndicator.clearAction(this);
	}

	//a tenth of max hp, rounded up
	public static int cost( Hero hero ){
		return (hero.HT + 9) / 10;
	}

	//spends the hero's own blood. It comes straight from hp, shields don't help
	public static boolean cast( Hero hero, Char enemy ){
		int cost = cost(hero);
		if (hero.subClass != HeroSubClass.CULTIST || hero.HP <= cost || !BloodHex.canHex(enemy)){
			return false;
		}

		Invisibility.dispel();
		BloodHex.clearAll();
		hero.HP -= cost;
		BloodHex.apply(enemy, BloodHex.TURNS, cost, false);

		int ward = hero.pointsInTalent(Talent.BLACK_WARD);
		if (ward > 0){
			Buff.affect(hero, Barrier.class).setShield(Math.round(cost * (15 + 15*ward) / 100f));
		}

		hero.spend(Actor.TICK);
		if (hero.sprite != null){
			hero.busy();
			hero.sprite.operate(enemy.pos);
			hero.sprite.showStatus(CharSprite.NEGATIVE, "-" + cost);
			CellEmitter.get(enemy.pos).burst(ShadowParticle.CURSE, 8);
			Sample.INSTANCE.play(Assets.Sounds.CURSED);
		}
		GLog.i(Messages.get(CultistPower.class, "cast", enemy.name()));
		return true;
	}

	@Override
	public String actionName() {
		return Messages.get(this, "action_name");
	}

	@Override
	public int actionIcon() {
		return HeroIcon.CULTIST;
	}

	@Override
	public int indicatorColor() {
		return 0x442266;
	}

	@Override
	public void doAction() {
		Hero hero = Dungeon.hero;
		if (hero.HP <= cost(hero)){
			GLog.w(Messages.get(this, "no_hp"));
		} else {
			GameScene.selectCell(targeting);
		}
	}

	private final CellSelector.Listener targeting = new CellSelector.Listener() {
		@Override
		public void onSelect( Integer cell ) {
			if (cell == null) return;
			if (!cast(Dungeon.hero, Actor.findChar(cell))){
				GLog.w(Messages.get(CultistPower.this, "bad_target"));
			}
		}

		@Override
		public String prompt() {
			return Messages.get(CultistPower.this, "prompt");
		}
	};

	//one for each cursed item worn, half for a curse infused weapon or armor as those
	//already get bonus levels. Wands arent worn so they never count
	public static float points( Hero hero ){
		if (hero.subClass != HeroSubClass.CULTIST) return 0;

		float points = 0;
		for (Item item : new Item[]{ hero.belongings.weapon(), hero.belongings.armor(),
				hero.belongings.artifact(), hero.belongings.misc(), hero.belongings.ring() }){
			if (item == null || !item.cursed) continue;
			boolean infused = (item instanceof Weapon && ((Weapon) item).curseInfusionBonus)
					|| (item instanceof Armor && ((Armor) item).curseInfusionBonus);
			points += infused ? 0.5f : 1f;
		}
		return Math.min(3, points);
	}

	//for weapon attacks only
	public static float damageFactor( Hero hero ){
		return 1f + 0.08f*points(hero);
	}

	public static float takenFactor( Hero hero ){
		return 1f - 0.02f*hero.pointsInTalent(Talent.CURSE_BEARER)*points(hero);
	}
}
