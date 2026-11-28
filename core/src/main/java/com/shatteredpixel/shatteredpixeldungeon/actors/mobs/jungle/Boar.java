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
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Dread;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.items.food.MysteryMeat;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.DeepWater;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.BoarSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.GameMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

//stubborn, and happy to prove it
public class Boar extends JungleAnimal {

	{
		spriteClass = BoarSprite.class;

		HP = HT = 12;
		defenseSkill = 4;

		EXP = 2;
		maxLvl = 8;

		loot = MysteryMeat.class;
		lootChance = 0.1f;

		HUNTING = new Hunting();
	}

	private static final int CHARGE_RANGE = 3;
	private static final float COOLDOWN = 8;
	private static final int STUN_TURNS = 2;
	//in a close fight it sometimes steps back to get a run up, but not too often
	private static final float BACK_OFF_CHANCE = 1/3f;
	private static final float BACK_OFF_REST = 3;

	//the marked cells of a charge which goes off next turn, otherwise null
	private ArrayList<Integer> chargePath;
	//where that one starts, and what it was aimed at
	private int chargeFrom, chargeAim;
	private boolean stunned = false;
	//it stepped back last turn, braced is saved and rushing is only for the turn thats running
	private boolean braced = false;
	private boolean rushing = false;
	private final LineCharge.SureSwing sureSwing = new LineCharge.SureSwing();

	public static class ChargeCooldown extends FlavourBuff {}

	public static class BackOffRest extends FlavourBuff {}

	public boolean stunned(){
		return stunned;
	}

	@Override
	protected boolean act() {
		sureSwing.newTurn();
		//the run up is only good for this one turn
		rushing = braced;
		braced = false;

		//it only gets another turn once the stun has worn off
		if (stunned){
			stunned = false;
			if (sprite != null) sprite.idle();
		}

		if (chargePath != null){
			if (paralysed > 0 || buff(Terror.class) != null || buff(Dread.class) != null){
				cancelCharge();
			} else {
				charge();
				return true;
			}
		}

		return super.act();
	}

	//after backing off it only needs 1 tile in between, and doesn't wait on the long cooldown
	private boolean prepareCharge( boolean runUp ){
		if (rooted || (!runUp && buff(ChargeCooldown.class) != null)
				|| Dungeon.level.distance(pos, enemy.pos) < (runUp ? 2 : CHARGE_RANGE)){
			return false;
		}
		//nobody gets charged while they can't move out of the way
		if (enemy.paralysed > 0) return false;
		ArrayList<Integer> path = LineCharge.aim(pos, enemy.pos);
		if (path == null) return false;

		chargePath = path;
		chargeFrom = pos;
		chargeAim = enemy.pos;
		//the same timing as Goo on badder bosses, so a slow hero still gets their turn
		float delay = GameMath.gate(attackDelay(), (int)Math.ceil(enemy.cooldown()), 3*attackDelay());
		for (int cell : path){
			GameScene.targetedCell(cell, delay);
		}
		if (sprite != null){
			sprite.turnTo(pos, enemy.pos);
			updateSpriteState();
		}
		if (Dungeon.level.heroFOV[pos]){
			GLog.w(Messages.get(this, "charge"));
			Dungeon.hero.interrupt();
		}
		spend(delay);
		return true;
	}

	private void charge(){
		ArrayList<Integer> path = chargePath;
		chargePath = null;
		Buff.affect(this, ChargeCooldown.class, COOLDOWN);
		Buff.prolong(this, BackOffRest.class, BACK_OFF_REST);

		LineCharge.Result result = LineCharge.run(this, chargeFrom, chargeAim, path, this);
		if (result.hit != null){
			Invisibility.dispel(this);
		} else if (result.hitWall){
			damage(Random.NormalIntRange(1, 4), this);
			stunned = isAlive();
		}

		if (result.hit != null){
			sureSwing.ready(result.hit);
			//being slowed can't cost it the second swing
			spendConstant(Math.min(TICK, attackDelay()));
		} else {
			spend(stunned ? attackDelay() + STUN_TURNS*TICK : attackDelay());
		}
		if (sprite != null && isAlive()){
			sprite.idle();
			updateSpriteState();
		}
	}

	//one step away from the enemy. Whatever hit it just before, it still goes
	private boolean backOff(){
		if (rooted || enemy.paralysed > 0 || buff(BackOffRest.class) != null
				|| !Dungeon.level.adjacent(pos, enemy.pos)
				|| Random.Float() >= BACK_OFF_CHANCE){
			return false;
		}
		int cell = backOffCell();
		if (cell == -1) return false;

		int from = pos;
		Buff.prolong(this, BackOffRest.class, BACK_OFF_REST);
		move(cell, false);
		//a trap it stepped on may have killed it or sent it elsewhere
		if (isAlive() && pos == cell){
			braced = true;
			//sliding keeps it facing the enemy
			Actor.add(new Pushing(this, from, cell));
			if (Dungeon.level.heroFOV[pos]){
				GLog.w(Messages.get(this, "back_off"));
				Dungeon.hero.interrupt();
			}
		}
		spend(1 / speed());
		return true;
	}

	//a spot with 1 tile between them and a clear run back in, the straightest one. -1 if cornered
	private int backOffCell(){
		int best = -1;
		for (int offset : PathFinder.NEIGHBOURS8){
			int cell = pos + offset;
			if (!Dungeon.level.passable[cell] || DeepWater.blocks(this, cell)
					|| Actor.findChar(cell) != null
					|| Dungeon.level.distance(cell, enemy.pos) != 2
					|| LineCharge.aim(cell, enemy.pos, this) == null){
				continue;
			}
			if (best == -1 || Dungeon.level.trueDistance(cell, enemy.pos)
					< Dungeon.level.trueDistance(best, enemy.pos)){
				best = cell;
			}
		}
		return best;
	}

	private void cancelCharge(){
		if (chargePath != null){
			chargePath = null;
			if (sprite != null) sprite.idle();
		}
	}

	@Override
	public boolean attack( Char enemy, float dmgMulti, float dmgBonus, float accMulti ) {
		return super.attack(enemy, dmgMulti, dmgBonus, sureSwing.accuracy(enemy, accMulti));
	}

	//a boar which just made a friend isn't going to run them over
	@Override
	public boolean add( Buff buff ) {
		boolean added = super.add(buff);
		if (added && buff instanceof AllyBuff) cancelCharge();
		return added;
	}

	@Override
	public int defenseSkill( Char enemy ) {
		return stunned ? 0 : super.defenseSkill(enemy);
	}

	@Override
	public void updateSpriteState() {
		super.updateSpriteState();
		if (sprite instanceof BoarSprite){
			if (chargePath != null) ((BoarSprite)sprite).charge();
			else if (stunned)       ((BoarSprite)sprite).stunned();
		}
	}

	private class Hunting extends Mob.Hunting {
		@Override
		public boolean act( boolean enemyInFOV, boolean justAlerted ) {
			if (enemyInFOV && !isCharmedBy(enemy)){
				if (prepareCharge(rushing) || (!rushing && backOff())){
					enemySeen = true;
					target = enemy.pos;
					return true;
				}
			}
			return super.act(enemyInFOV, justAlerted);
		}
	}

	private static final String CHARGE_PATH = "charge_path";
	private static final String CHARGE_FROM = "charge_from";
	private static final String CHARGE_AIM  = "charge_aim";
	private static final String STUNNED     = "stunned";
	private static final String BRACED      = "braced";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle(bundle);
		if (chargePath != null){
			int[] cells = new int[chargePath.size()];
			for (int i = 0; i < cells.length; i++) cells[i] = chargePath.get(i);
			bundle.put(CHARGE_PATH, cells);
			bundle.put(CHARGE_FROM, chargeFrom);
			bundle.put(CHARGE_AIM, chargeAim);
		}
		bundle.put(STUNNED, stunned);
		bundle.put(BRACED, braced);
		sureSwing.store(bundle);
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
		if (bundle.contains(CHARGE_PATH)){
			chargePath = new ArrayList<>();
			for (int cell : bundle.getIntArray(CHARGE_PATH)) chargePath.add(cell);
			chargeFrom = bundle.getInt(CHARGE_FROM);
			chargeAim = bundle.getInt(CHARGE_AIM);
		}
		stunned = bundle.getBoolean(STUNNED);
		braced = bundle.getBoolean(BRACED);
		sureSwing.restore(bundle);
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 1, 7 );
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
		return 20;
	}
}
