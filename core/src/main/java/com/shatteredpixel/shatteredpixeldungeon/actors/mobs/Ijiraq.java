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

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Adrenaline;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Dread;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.jungle.*;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.GeckoTail;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.RatSkull;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.IjiraqSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.AttackIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import com.watabou.utils.RectF;
import com.watabou.utils.Reflection;

import java.util.HashMap;

//a shapeshifter that wears the shape of a regular enemy until you get too close.
//It borrows that enemy's stats, but none of its special abilities
public class Ijiraq extends Mob {

	private static final float BASE_CHANCE = 1/100f;

	private static final int REVEAL_RANGE = 2;
	private static final float ADRENALINE_TURNS = 12f;
	private static final int HEARING_RANGE = 8; //how far off the growl still reachs the hero

	private static final int HP_MULT = 3;     //+200%
	private static final int DAMAGE_MULT = 2; //+100%

	//every enemy it can pretend to be, with the sheet that has their red eyed version.
	//only living things from the normal spawn rotations, so no bosses or summons can get in here.
	//Necromancers are left out as they have no melee attack to copy
	private static final HashMap<Class<? extends Mob>, String> DISGUISES = new HashMap<>();
	static {
		DISGUISES.put(Rat.class,                    "rat");
		DISGUISES.put(Albino.class,                 "rat");
		DISGUISES.put(Snake.class,                  "snake");
		DISGUISES.put(Gnoll.class,                  "gnoll");
		DISGUISES.put(GnollExile.class,             "gnoll");
		DISGUISES.put(Swarm.class,                  "swarm");
		DISGUISES.put(Crab.class,                   "crab");
		DISGUISES.put(HermitCrab.class,             "crab");
		DISGUISES.put(Slime.class,                  "slime");
		DISGUISES.put(CausticSlime.class,           "slime");
		DISGUISES.put(Thief.class,                  "thief");
		DISGUISES.put(Bandit.class,                 "thief");
		DISGUISES.put(Guard.class,                  "guard");
		DISGUISES.put(Bat.class,                    "bat");
		DISGUISES.put(Brute.class,                  "brute");
		DISGUISES.put(ArmoredBrute.class,           "brute");
		DISGUISES.put(Shaman.RedShaman.class,       "shaman");
		DISGUISES.put(Shaman.BlueShaman.class,      "shaman");
		DISGUISES.put(Shaman.PurpleShaman.class,    "shaman");
		DISGUISES.put(Spinner.class,                "spinner");
		DISGUISES.put(Ghoul.class,                  "ghoul");
		DISGUISES.put(Warlock.class,                "warlock");
		DISGUISES.put(Monk.class,                   "monk");
		DISGUISES.put(Senior.class,                 "monk");
		DISGUISES.put(Succubus.class,               "succubus");
		DISGUISES.put(Eye.class,                    "eye");
		DISGUISES.put(Scorpio.class,                "scorpio");
		DISGUISES.put(Acidic.class,                 "scorpio");
		//the jungle beasts, all but the squirrel
		DISGUISES.put(Skink.class,                  "skink");
		DISGUISES.put(Deer.class,                   "deer");
		DISGUISES.put(Boar.class,                   "boar");
		DISGUISES.put(Toad.class,                   "toad");
		DISGUISES.put(Mantis.class,                 "mantis");
		DISGUISES.put(Wolf.class,                   "wolf");
		DISGUISES.put(Bear.class,                   "bear");
		DISGUISES.put(Gecko.class,                  "gecko");
		DISGUISES.put(DireWolf.class,               "dire_wolf");
	}

	{
		spriteClass = IjiraqSprite.class;

		//these only matter for the journal, a spawned one takes them from its disguise
		HP = HT = 24;
		defenseSkill = 2;

		//like bosses, it can't be corrupted or enthralled, and it doesn't feel dread
		immunities.add(AllyBuff.class);
		immunities.add(Dread.class);
	}

	//a copy of the enemy being imitated. It's never added to the level, we only read stats from it
	private Mob form;
	private boolean revealed = false;

	public static boolean canDisguiseAs( Class<? extends Mob> cls ){
		return DISGUISES.containsKey(cls);
	}

	public static float spawnChance(){
		float chance = BASE_CHANCE * RatSkull.exoticChanceMultiplier();
		if (chance > BASE_CHANCE) chance = (chance + BASE_CHANCE)/2f; //rat skull is 1/2 as effective here
		return chance;
	}

	//returns an ijiraq instead of the given enemy on a rare roll, or null
	public static Ijiraq rollToReplace( Class<? extends Mob> cls ){
		//always roll, so that the number of RNG calls during levelgen doesn't change
		float roll = Random.Float();
		if (!canDisguiseAs(cls) || roll >= spawnChance()) return null;

		Ijiraq result = new Ijiraq();
		result.disguiseAs(cls);
		return result;
	}

	public void disguiseAs( Class<? extends Mob> cls ){
		form = Reflection.newInstance(cls);

		HP = HT = form.HT * HP_MULT;
		defenseSkill = form.defenseSkill;
		EXP = form.EXP;
		maxLvl = form.maxLvl;

		viewDistance = form.viewDistance;
		flying = form.flying;

		//hunting, so the ascension speed boost isn't counted twice
		form.state = form.HUNTING;
		baseSpeed = form.speed();

		properties.clear();
		properties.addAll(form.properties());
	}

	public Class<? extends Mob> disguise(){
		return form == null ? null : form.getClass();
	}

	public boolean disguised(){
		return form != null && !revealed;
	}

	@Override
	protected boolean act() {
		if (disguised()
				&& alignment == Alignment.ENEMY
				&& Dungeon.hero.invisible <= 0
				&& Dungeon.level.heroFOV[pos]
				&& Dungeon.level.distance(pos, Dungeon.hero.pos) <= REVEAL_RANGE){
			reveal();
			if (state != FLEEING) {
				alerted = true;
				aggro(Dungeon.hero);
				target = Dungeon.hero.pos;
			}
		}
		return super.act();
	}

	@Override
	public void damage( int dmg, Object src ) {
		if (disguised() && !isInvulnerable(src.getClass())){
			reveal();
		}
		super.damage(dmg, src);
	}

	public void reveal(){
		if (!disguised()) return;

		String oldName = name();
		revealed = true;
		Buff.affect(this, Adrenaline.class, ADRENALINE_TURNS);

		if (sprite != null){
			CharSprite old = sprite;
			GameScene.addSprite(this);
			old.killAndErase();
			AttackIndicator.refresh(this);
			growl(oldName);
		}
	}

	//warns the hero if they can see it happen, or are close enough to hear it
	private void growl( String disguiseName ){
		boolean seen = Dungeon.level.heroFOV[pos];
		if (!seen && Dungeon.level.distance(pos, Dungeon.hero.pos) > HEARING_RANGE){
			return;
		}

		if (seen){
			sprite.turnTo(pos, Dungeon.hero.pos);
			CellEmitter.get(pos).burst(ShadowParticle.UP, 8);
			GLog.w(Messages.get(this, "reveal", disguiseName));
		} else {
			GLog.w(Messages.get(this, "heard"));
		}

		PixelScene.shake(3, 0.7f); //same shake as a rockfall trap
		Sample.INSTANCE.play(Assets.Sounds.CHALLENGE, 1f, 0.7f); //pitched down for a growl
		Dungeon.hero.interrupt();
	}

	@Override
	public CharSprite sprite() {
		if (!disguised()) return super.sprite();

		//the normal sprite of the disguise, with its sheet swapped for the red eyed one.
		//both sheets are the same size, so all of the animation frames still line up
		CharSprite result = form.sprite();
		RectF frame = result.frame();
		result.texture(Assets.Sprites.IJIRAQ_EYES + DISGUISES.get(form.getClass()) + ".png");
		result.frame(frame);
		return result;
	}

	@Override
	public String name() {
		return disguised() ? form.name() : super.name();
	}

	@Override
	public String description() {
		if (disguised()){
			return form.description() + "\n\n" + Messages.get(this, "red_eyes");
		}
		return super.description();
	}

	@Override
	public String defenseVerb() {
		return disguised() ? form.defenseVerb() : super.defenseVerb();
	}

	@Override
	public int damageRoll() {
		if (form == null) return super.damageRoll();
		return form.damageRoll() * DAMAGE_MULT;
	}

	@Override
	public int attackSkill( Char target ) {
		if (form == null) return super.attackSkill(target);
		return form.attackSkill(target);
	}

	@Override
	public int drRoll() {
		if (form == null) return super.drRoll();
		return super.drRoll() + form.drRoll();
	}

	@Override
	public float attackDelay() {
		if (form == null) return super.attackDelay();
		float delay = form.attackDelay();
		if (buff(Adrenaline.class) != null) delay /= 1.5f;
		return delay;
	}

	//drops whatever the disguise would have dropped
	@Override
	public float lootChance() {
		return form == null ? 0 : form.lootChance();
	}

	@Override
	public Item createLoot() {
		if (form == null) return null;
		form.pos = pos; //some enemies drop things around themselves
		Item item = form.createLoot();
		//there's no tail to take, it was never really a gecko
		return item instanceof GeckoTail ? null : item;
	}

	private static final String FORM     = "form";
	private static final String REVEALED = "revealed";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle(bundle);
		if (form != null) bundle.put(FORM, form.getClass());
		bundle.put(REVEALED, revealed);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		//set up the disguise first, so the saved HP and such overwrite its defaults
		if (bundle.contains(FORM)) {
			disguiseAs((Class<? extends Mob>) bundle.getClass(FORM));
		}
		revealed = bundle.getBoolean(REVEALED);
		super.restoreFromBundle(bundle);
	}

}
