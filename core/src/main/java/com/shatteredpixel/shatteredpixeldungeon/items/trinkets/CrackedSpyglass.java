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

package com.shatteredpixel.shatteredpixeldungeon.items.trinkets;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

public class CrackedSpyglass extends Trinket{

	{
		image = ItemSpriteSheet.SPYGLASS;
	}

	public static final int LOOTS_TO_REPAIR = 10;

	private int hiddenLooted = 0;

	@Override
	protected int upgradeEnergyCost() {
		//6 -> 6(12) -> 8(20) -> 10(30)
		return 6+2*level();
	}

	@Override
	public String statsDesc() {
		String desc;
		if (isIdentified()){
			if (buffedLvl() >= 2){
				desc = Messages.get(this, "stats_desc_upgraded", Messages.decimalFormat("#.##", 100 * (extraLootChance(buffedLvl())-1f)));
			} else {
				desc = Messages.get(this, "stats_desc", Messages.decimalFormat("#.##", 100 * extraLootChance(buffedLvl())));
			}
		} else {
			desc = Messages.get(this, "typical_stats_desc", Messages.decimalFormat("#.##", 100 * extraLootChance(0)));
		}
		return desc + "\n\n" + Messages.get(this, "repair_progress", hiddenLooted, LOOTS_TO_REPAIR);
	}

	public static float extraLootChance(){
		return extraLootChance(trinketLevel(CrackedSpyglass.class));
	}

	public static float extraLootChance(int level ){
		if (level <= -1){
			return 0;
		} else {
			return 0.375f*(level+1);
		}
	}

	public static void onHiddenItemLooted(){
		if (Dungeon.hero == null) return;
		CrackedSpyglass glass = Dungeon.hero.belongings.getItem(CrackedSpyglass.class);
		if (glass == null) return;

		glass.hiddenLooted++;
		if (glass.hiddenLooted >= LOOTS_TO_REPAIR){
			Spyglass fixed = new Spyglass();
			fixed.level(glass.level());
			fixed.identify();
			glass.detachAll(Dungeon.hero.belongings.backpack);
			if (!fixed.collect()){
				Dungeon.level.drop(fixed, Dungeon.hero.pos);
			}
			GLog.p(Messages.get(CrackedSpyglass.class, "repaired"));
		} else {
			GLog.i(Messages.get(CrackedSpyglass.class, "progress", glass.hiddenLooted, LOOTS_TO_REPAIR));
		}
	}

	private static final String LOOTED = "hidden_looted";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(LOOTED, hiddenLooted);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		hiddenLooted = bundle.getInt(LOOTED);
	}

}
