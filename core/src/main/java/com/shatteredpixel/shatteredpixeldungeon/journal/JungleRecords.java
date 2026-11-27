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

package com.shatteredpixel.shatteredpixeldungeon.journal;

import com.shatteredpixel.shatteredpixeldungeon.items.quest.BearPelt;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.DireWolfPelt;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.WolfPelt;
import com.watabou.utils.Bundle;

import java.util.Arrays;
import java.util.LinkedHashSet;

//what's been done in the jungle across every run, saved along with the journal
public class JungleRecords {

	private static final LinkedHashSet<Class<?>> sold = new LinkedHashSet<>();
	private static final LinkedHashSet<Class<?>> wonOver = new LinkedHashSet<>();

	public static void sold( Class<?> item ){
		if (sold.add(item)) Journal.saveNeeded = true;
	}

	public static boolean soldAllPelts(){
		return sold.containsAll(Arrays.asList(WolfPelt.class, DireWolfPelt.class, BearPelt.class));
	}

	//an animal which took a berry and joined the hero
	public static void wonOver( Class<?> animal ){
		if (wonOver.add(animal)) Journal.saveNeeded = true;
	}

	public static int wonOverKinds(){
		return wonOver.size();
	}

	private static final String SOLD     = "jungle_sold";
	private static final String WON_OVER = "jungle_won_over";

	public static void store( Bundle bundle ){
		bundle.put(SOLD, sold.toArray(new Class[0]));
		bundle.put(WON_OVER, wonOver.toArray(new Class[0]));
	}

	public static void restore( Bundle bundle ){
		read(bundle, SOLD, sold);
		read(bundle, WON_OVER, wonOver);
	}

	private static void read( Bundle bundle, String key, LinkedHashSet<Class<?>> into ){
		into.clear();
		if (bundle.contains(key)){
			for (Class<?> cls : bundle.getClassArray(key)){
				into.add(cls);
			}
		}
	}
}
