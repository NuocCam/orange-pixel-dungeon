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
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BlastParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.AntiMagic;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfImplosion;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

//the wand of implosion's mark. It blows up when the clock runs out, or when whoever wears it dies
public class Implode extends FlavourBuff {

	public static final float FUSE = 3f;
	public static final float PER_ZAP = 0.4f;
	public static final float OVERLAP = 0.4f;

	public int zaps;
	public int level;

	{
		type = buffType.NEGATIVE;
		announced = true;
	}

	//marks the target, or zaps an existing mark: more damage and a fresh clock
	public static void apply( Char target, int wandLevel ){
		Implode mark = Buff.prolong(target, Implode.class, FUSE);
		mark.zaps++;
		mark.level = Math.max(mark.level, wandLevel);
	}

	private float multiplier(){
		return 1f + PER_ZAP*(zaps-1);
	}

	private int roll(){
		return Math.round(new WandOfImplosion().damageRoll(level) * multiplier());
	}

	@Override
	public boolean act() {
		int cell = target.pos;
		detach();
		blast(cell, roll(), false);
		return true;
	}

	//the wearer died first. Called after the mark is already off them
	public void targetDied( Char dead, Object cause ){
		blast(dead.pos, roll(), staffStrike(cause));
	}

	//everything within a tile, half physical and half magic. Allies of the hero get half
	public static void blast( int cell, int damage, boolean spareHero ){
		Sample.INSTANCE.play( Assets.Sounds.BLAST );
		if (Dungeon.level.heroFOV[cell] && Dungeon.hero.sprite != null && Dungeon.hero.sprite.parent != null){
			CellEmitter.center(cell).burst(BlastParticle.FACTORY, 30);
		}

		for (int offset : PathFinder.NEIGHBOURS9){
			Char ch = Actor.findChar(cell + offset);
			if (ch == null || !ch.isAlive() || (spareHero && ch == Dungeon.hero)){
				continue;
			}

			int dmg = damage;
			if (ch != Dungeon.hero && ch.alignment == Char.Alignment.ALLY){
				dmg = Math.round(dmg/2f);
			}
			dmg = overlapped(ch, dmg);

			//armor only stops the physical half, anti-magic only the magic half
			int physical = dmg/2;
			int magic = dmg - physical;
			physical = Math.max(0, physical - ch.drRoll());
			magic -= AntiMagic.drRoll(ch, ch.glyphLevel(AntiMagic.class));
			if (ch.buff(ArcaneArmor.class) != null){
				magic -= Random.NormalIntRange(0, ch.buff(ArcaneArmor.class).level());
			}

			ch.damage(physical + Math.max(0, magic), new WandOfImplosion());
			if (ch == Dungeon.hero && !ch.isAlive()){
				Badges.validateDeathFromFriendlyMagic();
				Dungeon.fail(new WandOfImplosion());
				GLog.n(Messages.get(WandOfImplosion.class, "ondeath"));
			}
		}
	}

	//blasts landing together dont add up in full: the strongest one counts, every other adds 40%.
	// Returns what this blast still has to deal, so the order they go off in doesnt matter
	private static int overlapped( Char ch, int damage ){
		Blasted hits = Buff.affect(ch, Blasted.class);
		int strongest = hits.strongest;
		if (damage <= strongest){
			return Math.round(OVERLAP*damage);
		}
		hits.strongest = damage;
		return damage - strongest + Math.round(OVERLAP*strongest);
	}

	//remembers the strongest blast a character took this moment, and is gone right after it
	public static class Blasted extends FlavourBuff {

		{
			//after every mark which goes off at the same time
			actPriority = BUFF_PRIO - 1;
		}

		int strongest;

		private static final String STRONGEST = "strongest";

		@Override
		public void storeInBundle( Bundle bundle ) {
			super.storeInBundle(bundle);
			bundle.put(STRONGEST, strongest);
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
			super.restoreFromBundle(bundle);
			strongest = bundle.getInt(STRONGEST);
		}
	}

	//true while a battlemage is hitting with a staff of implosion
	public static boolean staffStrike( Object src ){
		if (src != Dungeon.hero || !(src instanceof Hero)
				|| ((Hero) src).subClass != HeroSubClass.BATTLEMAGE){
			return false;
		}
		Object weapon = ((Hero) src).belongings.attackingWeapon();
		return weapon instanceof MagesStaff
				&& ((MagesStaff) weapon).wandClass() == WandOfImplosion.class;
	}

	//damage left over from a killing staff hit, in full to every enemy next to the body
	public static void spill( int cell, int damage ){
		for (int offset : PathFinder.NEIGHBOURS8){
			Char ch = Actor.findChar(cell + offset);
			if (ch != null && ch.isAlive() && ch.alignment == Char.Alignment.ENEMY){
				ch.damage(damage, new WandOfImplosion());
			}
		}
	}

	@Override
	public int icon() {
		return BuffIndicator.IMPLODE;
	}

	@Override
	public String iconTextDisplay() {
		return Integer.toString((int)Math.ceil(cooldown()));
	}

	@Override
	public String desc() {
		WandOfImplosion wand = new WandOfImplosion();
		return Messages.get(this, "desc",
				Math.round(wand.min(level)*multiplier()),
				Math.round(wand.max(level)*multiplier()),
				iconTextDisplay());
	}

	private static final String ZAPS    = "zaps";
	private static final String LEVEL   = "wand_level";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle(bundle);
		bundle.put(ZAPS, zaps);
		bundle.put(LEVEL, level);
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
		zaps = bundle.getInt(ZAPS);
		level = bundle.getInt(LEVEL);
	}
}
