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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

//the cultist's hex. The game already has a Hex debuff from shamans, which is something
//else, so this one has its own name
public class BloodHex extends Buff {

	public static final int TURNS = 10;

	public int turns;
	public int paid;            //hp spent on the cast, blood tithe and the ward use it
	public boolean tithePaid;   //once per cast, or a chain of jumps would heal more than it cost

	{
		type = buffType.NEGATIVE;
		announced = true;
	}

	//there is only ever one, and it has to be something the hero can see
	public static boolean canHex( Char ch ){
		return ch != null && ch.isAlive()
				&& ch.alignment == Char.Alignment.ENEMY
				&& ch.invisible <= 0
				&& Dungeon.level.heroFOV[ch.pos]
				&& !ch.isImmune(BloodHex.class);
	}

	public static void clearAll(){
		for (Mob m : Dungeon.level.mobs.toArray(new Mob[0])){
			BloodHex hex = m.buff(BloodHex.class);
			if (hex != null) hex.detach();
		}
		for (Char ch : Actor.chars().toArray(new Char[0])){
			BloodHex hex = ch.buff(BloodHex.class);
			if (hex != null) hex.detach();
		}
	}

	public static void apply( Char target, int turns, int paid, boolean tithePaid ){
		BloodHex hex = Buff.affect(target, BloodHex.class);
		hex.turns = turns;
		hex.paid = paid;
		hex.tithePaid = tithePaid;
		//the first turn only counts down once the target has had it
		hex.spend(TICK);
	}

	@Override
	public boolean act() {
		if (!target.isAlive()){
			detach();
			return true;
		}
		turns--;
		Char host = target;
		rot();
		//dying to the rot has already moved the hex on
		if (host.isAlive() && turns <= 0){
			detach();
		}
		spend(TICK);
		return true;
	}

	private void rot(){
		int points = Dungeon.hero.pointsInTalent(Talent.SOUL_ROT);
		if (points == 0) return;

		float amount = target.HT * points / 100f;
		if (Char.hasProp(target, Char.Property.BOSS) || Char.hasProp(target, Char.Property.MINIBOSS)){
			amount /= 4f;
		}
		//or the rot would shake off dread whisper's fear every turn
		Terror terror = target.buff(Terror.class);
		if (terror != null) terror.ignoreNextHit = true;
		target.damage(Math.max(1, Math.round(amount)), this);
	}

	//the target is dying: pay blood tithe, then find somewhere else to go
	public void targetDied( Char dead ){
		Hero hero = Dungeon.hero;

		int tithe = hero.pointsInTalent(Talent.BLOOD_TITHE);
		if (tithe > 0 && !tithePaid && hero.isAlive()){
			tithePaid = true;
			int heal = Math.min(hero.HT - hero.HP, Math.round(paid * (20 + 20*tithe) / 100f));
			if (heal > 0){
				hero.HP += heal;
				if (hero.sprite != null){
					hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(heal), FloatingText.HEALING);
				}
			}
		}

		if (turns <= 0) return;

		Char next = null;
		for (Mob m : Dungeon.level.mobs){
			if (m != dead && canHex(m)
					&& (next == null || Dungeon.level.distance(dead.pos, m.pos) < Dungeon.level.distance(dead.pos, next.pos))){
				next = m;
			}
		}
		if (next != null){
			apply(next, turns + 2*hero.pointsInTalent(Talent.SPREADING_ROT), paid, tithePaid);
			if (next.sprite != null && next.sprite.parent != null){
				CellEmitter.get(next.pos).burst(ShadowParticle.CURSE, 6);
			}
		}
	}

	//dread whisper
	public static void onHeroHit( Hero hero, Char enemy ){
		int points = hero.pointsInTalent(Talent.DREAD_WHISPER);
		if (points > 0 && enemy.isAlive() && enemy.buff(BloodHex.class) != null
				&& Random.Float() < 0.05f + 0.05f*points){
			Terror terror = Buff.prolong(enemy, Terror.class, 2f);
			terror.object = hero.id();
		}
	}

	@Override
	public int icon() {
		return BuffIndicator.HEX;
	}

	@Override
	public void tintIcon(Image icon) {
		icon.hardlight(1f, 0.35f, 0.35f);
	}

	@Override
	public float iconFadePercent() {
		return Math.max(0, (TURNS - turns) / (float)TURNS);
	}

	@Override
	public String iconTextDisplay() {
		return Integer.toString(turns);
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", turns);
	}

	private static final String LEFT   = "turns";
	private static final String PAID   = "paid";
	private static final String TITHED = "tithe_paid";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle(bundle);
		bundle.put(LEFT, turns);
		bundle.put(PAID, paid);
		bundle.put(TITHED, tithePaid);
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
		turns = bundle.getInt(LEFT);
		paid = bundle.getInt(PAID);
		tithePaid = bundle.getBoolean(TITHED);
	}
}
