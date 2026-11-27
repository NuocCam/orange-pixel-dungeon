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

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Dread;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.DriedRose;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.WornKey;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.BeastMarrow;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.AmalgamSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.GameMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;

//the jungle's first boss, a few animals twisted into one. When it gets hurt it calls
// more of them over, and whatever it eats it learns from
public class Amalgam extends Mob {

	//a move taken from something it ate
	public enum Graft {
		WOLF(Wolf.class), BEAR(Bear.class), TOAD(Toad.class), DEER(Deer.class);

		private final Class<? extends JungleAnimal> animal;

		Graft( Class<? extends JungleAnimal> animal ){
			this.animal = animal;
		}

		private static Graft of( JungleAnimal meal ){
			for (Graft graft : values()){
				if (graft.animal == meal.getClass()) return graft;
			}
			return null;
		}
	}

	private final EnumSet<Graft> grafts = EnumSet.noneOf(Graft.class);

	{
		HP = HT = Dungeon.isChallenged(Challenges.STRONGER_BOSSES) ? 120 : 100;
		EXP = 10;
		defenseSkill = 8;
		spriteClass = AmalgamSprite.class;

		properties.add(Property.BOSS);

		HUNTING = new Hunting();

		if (Dungeon.isChallenged(Challenges.STRONGER_BOSSES)){
			grafts.add(Random.oneOf(Graft.values()));
		}
	}

	//one feeding each at 3/4, 1/2 and 1/4 health
	private static final int FEEDINGS = 3;
	private static final int PREY_COUNT = 2;
	//how many tiles off they turn up
	private static final int PREY_NEAR = 6;
	private static final int PREY_FAR = 8;
	//how long it waits for them before going to get one
	private static final int PATIENCE = 10;
	private static final int MEAL_HEALING = 10;

	private static final float MOVE_COOLDOWN = 4;
	private static final int SPIT_RANGE = 6;
	private static final float SLAM_PARALYSIS = 2;
	private static final int SWEEP_KNOCKBACK = 3;

	//how many have started, counting one which is still going
	private int feedings = 0;
	private boolean feeding = false;
	private int waited = 0;
	private int eaten = 0;

	//the stolen move being used right now
	private Graft move = null;
	//where it stood when it wound up a slam which goes off next turn, otherwise -1
	private int slamCell = -1;

	private final AiState FEEDING = new Feeding();

	public static class MoveCooldown extends FlavourBuff {}

	public EnumSet<Graft> grafts(){
		return grafts.clone();
	}

	//doesn't count the one badder bosses start with
	public int eatenCount(){
		return eaten;
	}

	public boolean feeding(){
		return feeding;
	}

	//the charge is the boar's
	private static final int CHARGE_RANGE = 3;
	private static final float CHARGE_COOLDOWN = 8;
	private static final int STUN_TURNS = 2;

	//the marked cells of a charge which goes off next turn, otherwise null
	private ArrayList<Integer> chargePath;
	//where that one starts, and what it was aimed at
	private int chargeFrom, chargeAim;
	//only true while that charge is being run
	private boolean charging = false;
	private boolean stunned = false;
	private final LineCharge.SureSwing sureSwing = new LineCharge.SureSwing();
	//on badder bosses one charge is followed by another, as soon as it can line one up
	private boolean secondCharge = false;

	public static class ChargeCooldown extends FlavourBuff {}

	@Override
	public int damageRoll() {
		if (move == Graft.BEAR) return Random.NormalIntRange( 15, 25 );
		return Random.NormalIntRange( 1, 8 );
	}

	@Override
	public int attackSkill( Char target ) {
		return 10;
	}

	@Override
	public int defenseSkill( Char enemy ) {
		return stunned ? 0 : super.defenseSkill(enemy);
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0, 2);
	}

	@Override
	protected boolean act() {
		sureSwing.newTurn();

		//it only gets another turn once the stun has worn off
		if (stunned){
			stunned = false;
			if (sprite != null) sprite.idle();
		}

		if (state != SLEEPING){
			Dungeon.level.seal();
		}

		if (chargePath != null || slamCell != -1){
			if (paralysed > 0 || buff(Terror.class) != null || buff(Dread.class) != null){
				cancelAttack();
			} else {
				if (chargePath != null) charge();
				else                    slam();
				return true;
			}
		}

		if (feeding){
			//nothing scares it off its food
			Buff.detach(this, Terror.class);
			state = FEEDING;
		}

		return super.act();
	}

	private boolean prepareCharge(){
		//a follow-up which never found its line is gone once the cooldown is
		if (buff(ChargeCooldown.class) == null) secondCharge = false;

		if (rooted || !secondCharge && (buff(ChargeCooldown.class) != null
				|| Dungeon.level.distance(pos, enemy.pos) < CHARGE_RANGE)){
			return false;
		}
		//nobody gets charged while they can't move out of the way
		if (enemy.paralysed > 0) return false;
		ArrayList<Integer> path = LineCharge.aim(pos, enemy.pos);
		if (path == null) return false;

		chargePath = path;
		chargeFrom = pos;
		chargeAim = enemy.pos;
		secondCharge = !secondCharge && Dungeon.isChallenged(Challenges.STRONGER_BOSSES);

		warn(path, "charge");
		if (sprite != null){
			sprite.turnTo(pos, enemy.pos);
			updateSpriteState();
		}
		return true;
	}

	//marks the cells which are about to be hit, and gives the hero a turn to get off them
	private void warn( ArrayList<Integer> cells, String message ){
		//the same timing as Goo on badder bosses, so a slow hero still gets their turn
		float delay = GameMath.gate(attackDelay(), (int)Math.ceil(enemy.cooldown()), 3*attackDelay());
		for (int cell : cells){
			GameScene.targetedCell(cell, delay);
		}
		if (Dungeon.level.heroFOV[pos]){
			GLog.w(Messages.get(this, message));
			Dungeon.hero.interrupt();
		}
		spend(delay);
	}

	private void charge(){
		ArrayList<Integer> path = chargePath;
		chargePath = null;
		Buff.prolong(this, ChargeCooldown.class, CHARGE_COOLDOWN);

		charging = true;
		LineCharge.Result result = LineCharge.run(this, chargeFrom, chargeAim, path, this);
		charging = false;

		if (result.hit != null){
			Invisibility.dispel(this);
		} else if (result.hitWall){
			stunned = true;
			damage(Random.NormalIntRange(1, 4), this);
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

	private void cancelAttack(){
		secondCharge = false;
		if (chargePath != null || slamCell != -1){
			chargePath = null;
			slamCell = -1;
			if (sprite != null) sprite.idle();
		}
	}

	// **** feeding ****

	//the health it can't be taken below just now
	private int healthFloor(){
		if (feedings < FEEDINGS) return HT * (FEEDINGS - feedings) / (FEEDINGS + 1);
		return feeding ? 1 : 0;
	}

	//covers things which set its health straight to 0, like an assassination
	@Override
	public boolean isAlive() {
		return super.isAlive() || healthFloor() > 0;
	}

	private void holdAtMark(){
		if (HP < healthFloor()) HP = healthFloor();
		if (!feeding && feedings < FEEDINGS && HP <= healthFloor()){
			startFeeding();
		}
	}

	private void startFeeding(){
		feedings++;
		feeding = true;
		waited = 0;
		cancelAttack();
		stunned = false;

		callPrey();

		GLog.w(Messages.get(this, "roar"));
		if (Dungeon.level.heroFOV[pos]) Dungeon.hero.interrupt();
		if (sprite instanceof AmalgamSprite) ((AmalgamSprite)sprite).roar();
	}

	//animals with moves it doesn't have yet
	private void callPrey(){
		ArrayList<Graft> missing = new ArrayList<>(EnumSet.complementOf(grafts));
		Random.shuffle(missing);
		ArrayList<Integer> cells = preyCells();

		for (int i = 0; i < PREY_COUNT && i < missing.size() && i < cells.size(); i++){
			JungleAnimal animal = Reflection.newInstance(missing.get(i).animal);
			animal.makePrey();
			animal.pos = cells.get(i);
			GameScene.add(animal);
		}
	}

	//free cells it can be walked to from, the right distance away first. If there aren't
	// enough of those, whatever comes closest to that will do
	private ArrayList<Integer> preyCells(){
		PathFinder.buildDistanceMap(pos, Dungeon.level.passable);
		final int[] distance = PathFinder.distance.clone();

		ArrayList<Integer> cells = new ArrayList<>();
		for (int i = 0; i < Dungeon.level.length(); i++){
			if (distance[i] != Integer.MAX_VALUE && Dungeon.level.passable[i]
					&& Actor.findChar(i) == null){
				cells.add(i);
			}
		}

		Random.shuffle(cells);
		//the sort keeps equally good cells in their shuffled order
		Collections.sort(cells, new Comparator<Integer>() {
			@Override
			public int compare( Integer a, Integer b ) {
				return miss(distance[a]) - miss(distance[b]);
			}
		});
		return cells;
	}

	private static int miss( int distance ){
		return Math.max(0, Math.max(PREY_NEAR - distance, distance - PREY_FAR));
	}

	private ArrayList<JungleAnimal> preyLeft(){
		ArrayList<JungleAnimal> prey = new ArrayList<>();
		for (Mob mob : Dungeon.level.mobs){
			if (mob instanceof JungleAnimal && ((JungleAnimal) mob).prey && mob.isAlive()){
				prey.add((JungleAnimal) mob);
			}
		}
		return prey;
	}

	private void eat( JungleAnimal meal ){
		grafts.add(Graft.of(meal));
		eaten++;
		GLog.n(Messages.get(this, "eat", meal.name()));

		int healed = Math.min(MEAL_HEALING, HT - HP);
		HP += healed;
		if (sprite != null){
			sprite.turnTo(pos, meal.pos);
			if (healed > 0 && Dungeon.level.heroFOV[pos]){
				sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(healed), FloatingText.HEALING);
			}
			if (sprite instanceof AmalgamSprite) ((AmalgamSprite)sprite).eat();
		}

		//the other one runs for it
		for (JungleAnimal animal : preyLeft()){
			sendAway(animal);
		}
		endFeeding();
	}

	//gone without a fight, so it doesn't count as anyone's kill
	private void sendAway( JungleAnimal animal ){
		animal.alignment = Alignment.NEUTRAL;
		animal.destroy();
		if (animal.sprite != null){
			animal.sprite.killAndErase();
		}
	}

	private void endFeeding(){
		feeding = false;
		state = HUNTING;
		//if it's already down at the next mark, that one starts right away
		holdAtMark();
	}

	//stands and roars while it waits, one turn at a time
	private class Feeding implements AiState {
		@Override
		public boolean act( boolean enemyInFOV, boolean justAlerted ) {
			enemySeen = enemyInFOV;

			ArrayList<JungleAnimal> prey = preyLeft();
			for (JungleAnimal animal : prey){
				if (Dungeon.level.adjacent(pos, animal.pos)){
					eat(animal);
					spend(TICK);
					return true;
				}
			}

			if (prey.isEmpty()){
				if (sprite != null) sprite.idle();
				endFeeding();

			} else if (waited < PATIENCE){
				waited++;
				if (sprite instanceof AmalgamSprite) ((AmalgamSprite)sprite).roar();

			} else {
				Collections.sort(prey, new Comparator<JungleAnimal>() {
					@Override
					public int compare( JungleAnimal a, JungleAnimal b ) {
						return Float.compare(Dungeon.level.trueDistance(pos, a.pos),
								Dungeon.level.trueDistance(pos, b.pos));
					}
				});
				for (JungleAnimal animal : prey){
					int oldPos = pos;
					if (getCloser(animal.pos)){
						spend(1 / speed());
						return moveSprite(oldPos, pos);
					}
				}
				//no way through to any of them, so whoever is in the way gets bitten
				Char hero = Dungeon.hero;
				if (Dungeon.level.adjacent(pos, hero.pos) && hero.invisible <= 0 && !isCharmedBy(hero)){
					enemy = hero;
					return doAttack(hero);
				}
			}

			spend(TICK);
			return true;
		}
	}

	// **** stolen moves ****

	//uses one of the moves it has, if any of them can reach just now
	private boolean useMove(){
		if (buff(MoveCooldown.class) != null) return false;

		ArrayList<Graft> usable = new ArrayList<>();
		for (Graft graft : grafts){
			//same rule as the charge, no wind up on somebody who can't get away
			if (graft == Graft.BEAR && enemy.paralysed > 0) continue;
			if (graft == Graft.TOAD ? canSpit() : Dungeon.level.adjacent(pos, enemy.pos)){
				usable.add(graft);
			}
		}
		if (usable.isEmpty()) return false;

		switch (Random.element(usable)){
			case WOLF: default: doubleBite();   break;
			case BEAR:          windUpSlam();   break;
			case TOAD:          spit();         break;
			case DEER:          sweep();        break;
		}
		return true;
	}

	private boolean canSpit(){
		int distance = Dungeon.level.distance(pos, enemy.pos);
		return distance >= 2 && distance <= SPIT_RANGE
				&& new Ballistica(pos, enemy.pos, Ballistica.PROJECTILE).collisionPos == enemy.pos;
	}

	private void moveUsed(){
		Invisibility.dispel(this);
		Buff.affect(this, MoveCooldown.class, MOVE_COOLDOWN);
		spend(attackDelay());
	}

	private void doubleBite(){
		Char target = enemy;
		if (sprite instanceof AmalgamSprite) ((AmalgamSprite)sprite).doubleBite(target.pos);

		move = Graft.WOLF;
		for (int i = 0; i < 2 && isAlive() && target.isAlive() && !feeding
				&& Dungeon.level.adjacent(pos, target.pos); i++){
			attack(target);
		}
		move = null;
		moveUsed();
	}

	private void windUpSlam(){
		slamCell = pos;
		ArrayList<Integer> cells = new ArrayList<>();
		for (int offset : PathFinder.NEIGHBOURS8){
			if (!Dungeon.level.solid[pos + offset]) cells.add(pos + offset);
		}
		warn(cells, "slam");
		if (sprite != null) updateSpriteState();
	}

	//only comes down where it was marked
	private void slam(){
		boolean inPlace = slamCell == pos;
		slamCell = -1;

		if (!inPlace){
			if (sprite != null) sprite.idle();
			spend(attackDelay());
			return;
		}

		if (sprite instanceof AmalgamSprite) ((AmalgamSprite)sprite).slam();
		if (Dungeon.level.heroFOV[pos]) PixelScene.shake( 3, 0.3f );

		move = Graft.BEAR;
		for (int offset : PathFinder.NEIGHBOURS8){
			Char ch = Actor.findChar(pos + offset);
			if (ch != null && ch.alignment != Alignment.NEUTRAL && isAlive()){
				//no dodging it, being somewhere else is how you do that
				if (attack(ch, 1f, 0f, Char.INFINITE_ACCURACY) && ch.isAlive()){
					Buff.prolong(ch, Paralysis.class, SLAM_PARALYSIS);
				}
			}
		}
		move = null;
		moveUsed();
	}

	private void spit(){
		if (sprite instanceof AmalgamSprite){
			((AmalgamSprite)sprite).spit(enemy.pos);
			if (sprite.parent != null){
				MagicMissile.boltFromChar(sprite.parent, MagicMissile.FOLIAGE, sprite, enemy.pos, null);
			}
		}
		if (Dungeon.level.heroFOV[pos]) GLog.w(Messages.get(this, "spit"));
		enemy.damage(Random.NormalIntRange(3, 6), this);
		if (enemy.isAlive()){
			Buff.affect(enemy, Poison.class).set(7);
		} else if (enemy == Dungeon.hero){
			Dungeon.fail(this);
			GLog.n(Messages.capitalize(Messages.get(Char.class, "kill", name())));
		}
		moveUsed();
	}

	private void sweep(){
		Char target = enemy;
		if (sprite instanceof AmalgamSprite) ((AmalgamSprite)sprite).sweep(target.pos);

		int away = target.pos - pos;
		if (attack(target) && target.isAlive()){
			WandOfBlastWave.throwChar(target,
					new Ballistica(target.pos, target.pos + away, Ballistica.MAGIC_BOLT),
					SWEEP_KNOCKBACK, false, true, this);
		}
		moveUsed();
	}

	@Override
	public boolean attack( Char enemy, float dmgMulti, float dmgBonus, float accMulti ) {
		return super.attack(enemy, dmgMulti, dmgBonus, sureSwing.accuracy(enemy, accMulti));
	}

	@Override
	public int attackProc( Char enemy, int damage ) {
		damage = super.attackProc( enemy, damage );
		//only gets here if the charge really landed
		if (charging && enemy == Dungeon.hero) {
			Statistics.qualifiedForBossChallengeBadge = false;
			Statistics.bossScores[0] -= 100;
		}
		if (move == Graft.WOLF && Random.Int(2) == 0){
			Buff.affect(enemy, Bleeding.class).set(6);
		}
		return damage;
	}

	@Override
	public boolean add( Buff buff ) {
		boolean added = super.add(buff);
		if (added && buff instanceof AllyBuff) cancelAttack();
		return added;
	}

	@Override
	public void updateSpriteState() {
		super.updateSpriteState();
		if (sprite instanceof AmalgamSprite){
			if (chargePath != null)     ((AmalgamSprite)sprite).charge();
			else if (slamCell != -1)    ((AmalgamSprite)sprite).slamWindup();
			else if (stunned)           ((AmalgamSprite)sprite).stunned();
			else if (feeding)           ((AmalgamSprite)sprite).roar();
		}
	}

	@Override
	public void damage( int dmg, Object src ) {
		if (!BossHealthBar.isAssigned()){
			BossHealthBar.assignBoss( this );
			Dungeon.level.seal();
		}
		int before = HP + shielding();
		super.damage(dmg, src);
		//one hit can't skip a feeding, however big it is
		holdAtMark();

		//time on a locked floor is only given for health which actually came off
		int dealt = before - HP - shielding();
		LockedFloor lock = Dungeon.hero.buff(LockedFloor.class);
		if (lock != null && dealt > 0){
			if (Dungeon.isChallenged(Challenges.STRONGER_BOSSES))   lock.addTime(dealt);
			else                                                    lock.addTime(dealt*1.5f);
		}
	}

	@Override
	public void die( Object cause ) {

		//dead is dead, no marks left to hold it up
		feedings = FEEDINGS;
		feeding = false;
		for (JungleAnimal animal : preyLeft()){
			sendAway(animal);
		}

		super.die( cause );

		Dungeon.level.unseal();

		GameScene.bossSlain();
		Dungeon.level.drop( new WornKey( Dungeon.depth ), pos ).sprite.drop();

		//the same odds as goo's blobs
		int pieces = Random.chances(new float[]{0, 0, 6, 3, 1});
		for (int i = 0; i < pieces; i++){
			int ofs;
			do {
				ofs = PathFinder.NEIGHBOURS8[Random.Int(8)];
			} while (!Dungeon.level.passable[pos + ofs]);
			Dungeon.level.drop( new BeastMarrow(), pos + ofs ).sprite.drop( pos );
		}

		Badges.validateBossSlain();
		Badges.validateAmalgamSlain(eatenCount());
		if (Statistics.qualifiedForBossChallengeBadge){
			Badges.validateBossChallengeCompleted();
		}
		Statistics.bossScores[0] += 1000;

		yell( Messages.get(this, "defeated") );
	}

	@Override
	public void notice() {
		super.notice();
		if (!BossHealthBar.isAssigned()) {
			BossHealthBar.assignBoss(this);
			Dungeon.level.seal();
			yell(Messages.get(this, "notice"));
			for (Char ch : Actor.chars()){
				if (ch instanceof DriedRose.GhostHero){
					((DriedRose.GhostHero) ch).sayBoss();
				}
			}
		}
	}

	private class Hunting extends Mob.Hunting {
		@Override
		public boolean act( boolean enemyInFOV, boolean justAlerted ) {
			if (enemyInFOV && !isCharmedBy(enemy) && (prepareCharge() || useMove())){
				enemySeen = true;
				target = enemy.pos;
				return true;
			}
			return super.act(enemyInFOV, justAlerted);
		}
	}

	private static final String CHARGE_PATH   = "charge_path";
	private static final String CHARGE_FROM   = "charge_from";
	private static final String CHARGE_AIM    = "charge_aim";
	private static final String STUNNED       = "stunned";
	private static final String SECOND_CHARGE = "second_charge";
	private static final String GRAFTS        = "grafts";
	private static final String FEEDINGS_HAD  = "feedings";
	private static final String IS_FEEDING    = "feeding";
	private static final String WAITED        = "waited";
	private static final String EATEN         = "eaten";
	private static final String SLAM_CELL     = "slam_cell";

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
		sureSwing.store(bundle);
		bundle.put(SECOND_CHARGE, secondCharge);

		String[] names = new String[grafts.size()];
		int i = 0;
		for (Graft graft : grafts) names[i++] = graft.name();
		bundle.put(GRAFTS, names);
		bundle.put(FEEDINGS_HAD, feedings);
		bundle.put(IS_FEEDING, feeding);
		bundle.put(WAITED, waited);
		bundle.put(EATEN, eaten);
		bundle.put(SLAM_CELL, slamCell);
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
		sureSwing.restore(bundle);
		secondCharge = bundle.getBoolean(SECOND_CHARGE);

		grafts.clear();
		for (String name : bundle.getStringArray(GRAFTS)) grafts.add(Graft.valueOf(name));
		feedings = bundle.getInt(FEEDINGS_HAD);
		feeding = bundle.getBoolean(IS_FEEDING);
		waited = bundle.getInt(WAITED);
		eaten = bundle.getInt(EATEN);
		slamCell = bundle.getInt(SLAM_CELL);
		//mobs don't save a state of their own making
		if (feeding) state = FEEDING;

		if (state != SLEEPING) BossHealthBar.assignBoss(this);
	}
}
