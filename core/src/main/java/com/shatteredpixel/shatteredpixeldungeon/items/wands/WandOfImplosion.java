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

package com.shatteredpixel.shatteredpixeldungeon.items.wands;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Implode;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

public class WandOfImplosion extends DamageWand {

	{
		image = ItemSpriteSheet.WAND_IMPLOSION;
	}

	@Override
	public int min(int lvl){
		return 10+2*lvl;
	}

	@Override
	public int max(int lvl){
		return 18+6*lvl;
	}

	@Override
	public int initialCharges() {
		return 1;
	}

	//it hits hard, so a new charge only comes every 3 upgrades
	@Override
	public int maxChargesAt(int lvl) {
		return Math.min(initialCharges() + lvl/3, 10);
	}

	@Override
	public void onZap(Ballistica bolt) {
		Char ch = Actor.findChar(bolt.collisionPos);
		//never mark the one zapping, a bolt can end on their own tile
		if (ch != null && ch != curUser){
			//mark first, a priest's zap can kill and then the mark has to blow up
			Implode.apply(ch, buffedLvl());
			wandProc(ch, chargesPerCast());
			Sample.INSTANCE.play( Assets.Sounds.HIT_MAGIC, 1, 1.1f * Random.Float(0.87f, 1.15f) );
		} else {
			//nothing to mark, so it goes off right here at half strenght
			breakThings(bolt.collisionPos);
			Implode.blast(bolt.collisionPos, Math.round(damageRoll()/2f), false);
		}
	}

	//the same things a bomb breaks: whatever burns, and loose things on the floor
	private void breakThings(int cell){
		boolean changed = false;
		for (int offset : PathFinder.NEIGHBOURS9){
			int c = cell + offset;
			if (c < 0 || c >= Dungeon.level.length()){
				continue;
			}
			if (Dungeon.level.flamable[c]){
				Dungeon.level.destroy(c);
				GameScene.updateMap(c);
				changed = true;
			}
			Heap heap = Dungeon.level.heaps.get(c);
			if (heap != null){
				heap.explode();
			}
		}
		if (changed){
			Dungeon.observe();
		}
	}

	@Override
	public void onHit(MagesStaff staff, Char attacker, Char defender, int damage) {
		//the battlemage's part happens after the damage lands, see Char.damage and Implode.spill
	}

	@Override
	public void fx(Ballistica bolt, Callback callback) {
		MagicMissile.boltFromChar(curUser.sprite.parent,
				MagicMissile.FORCE,
				curUser.sprite,
				bolt.collisionPos,
				callback);
		Sample.INSTANCE.play(Assets.Sounds.ZAP);
	}

	@Override
	public void staffFx(MagesStaff.StaffParticle particle) {
		particle.color(0xFFE066);
		particle.am = 0.6f;
		particle.setLifespan(0.8f);
		//sparks falling in towards the tip
		float angle = Random.Float(PointF.PI2);
		particle.speed.polar(angle, -4f);
		particle.setSize(0.5f, 2f);
		particle.radiateXY(3f);
	}

}
