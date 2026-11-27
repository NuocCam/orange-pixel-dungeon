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

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.jungle;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.food.MysteryMeat;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.DeerSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

//all antlers until it's badly hurt, then it runs for it
public class Deer extends JungleAnimal {

	{
		spriteClass = DeerSprite.class;

		HP = HT = 12;
		defenseSkill = 4;

		EXP = 2;
		maxLvl = 7;

		lootChance = 0.3f; //see createLoot

		FLEEING = new Fleeing();
	}

	private static final float FLEE_SPEED = 0.8f;
	//how far off the hero has to be before it dares to stop
	private static final int SAFE_RANGE = 6;

	//stood still, out of danger, getting its health back
	private final AiState RESTING = new Resting();
	//had nowhere to run and turned to fight, until it loses track of the hero
	private boolean cornered = false;

	private boolean badlyHurt(){
		return HP*10 < HT*3;
	}

	//friends, dinner and the maddened don't run
	private boolean wild(){
		return !prey && alignment == Alignment.ENEMY && buff(Amok.class) == null;
	}

	@Override
	protected boolean act() {
		if (state != HUNTING) cornered = false;
		if (badlyHurt() && wild() && !cornered
				&& state != FLEEING && state != RESTING && state != SLEEPING){
			flee();
		}
		return super.act();
	}

	private void flee(){
		state = FLEEING;
		if (enemy == null || !enemy.isAlive()) enemy = Dungeon.hero;
		target = enemy.pos;
	}

	private boolean heroFarOff(){
		return !Dungeon.level.heroFOV[pos]
				&& Dungeon.level.distance(Dungeon.hero.pos, pos) >= SAFE_RANGE;
	}

	//attacked or crowded while it was resting
	private void disturb(){
		if (badlyHurt() && wild()) flee();
		else                       state = WANDERING;
	}

	@Override
	public void damage( int dmg, Object src ) {
		boolean wasResting = state == RESTING;
		super.damage(dmg, src);
		if (wasResting && isAlive() && !isInvulnerable(src.getClass())){
			disturb();
		}
	}

	@Override
	public float speed() {
		float speed = super.speed();
		if (state == FLEEING && badlyHurt()) speed *= FLEE_SPEED;
		return speed;
	}

	private class Fleeing extends Mob.Fleeing {
		@Override
		protected void escaped() {
			if (badlyHurt() && wild() && heroFarOff()) state = RESTING;
		}

		@Override
		protected void nowhereToRun() {
			if (!enemySeen) escaped();
			if (state == FLEEING) {
				super.nowhereToRun();
				cornered = state == HUNTING;
			}
		}
	}

	private class Resting implements AiState {
		@Override
		public boolean act( boolean enemyInFOV, boolean justAlerted ) {
			enemySeen = enemyInFOV;
			Char hero = Dungeon.hero;
			if (hero.invisible <= 0 && fieldOfView[hero.pos]
					&& Dungeon.level.distance(pos, hero.pos) < SAFE_RANGE){
				disturb();
			} else {
				if (HP < HT) {
					HP++;
					if (sprite != null && Dungeon.level.heroFOV[pos]){
						sprite.showStatusWithIcon(CharSprite.POSITIVE, "1", FloatingText.HEALING);
					}
				}
				if (HP >= HT) state = WANDERING;
			}
			spend(TICK);
			return true;
		}
	}

	private static final String RESTING_NOW = "resting";
	private static final String CORNERED    = "cornered";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle(bundle);
		bundle.put(RESTING_NOW, state == RESTING);
		bundle.put(CORNERED, cornered);
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
		//mobs don't save a state of their own making
		if (bundle.getBoolean(RESTING_NOW)) state = RESTING;
		cornered = bundle.getBoolean(CORNERED);
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 1, 4 );
	}

	@Override
	public int attackSkill( Char target ) {
		return 10;
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0, 1);
	}

	@Override
	public int berryTurns() {
		return 12;
	}

	@Override
	public Item createLoot() {
		//four seeds for every bit of meat
		return Random.Int(5) == 0 ? new MysteryMeat() : Generator.randomUsingDefaults(Generator.Category.SEED);
	}
}
