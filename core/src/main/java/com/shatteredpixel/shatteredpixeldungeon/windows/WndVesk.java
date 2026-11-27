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

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Vesk;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.TengusMask;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CloakOfShadows;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

//vesk's greeting, with the choice to offer him something
public class WndVesk extends WndOptions {

	private final Vesk vesk;

	public WndVesk( Vesk vesk ){
		super(vesk.sprite(),
				Messages.titleCase(vesk.name()),
				greeting(),
				Messages.get(WndVesk.class, "offer"),
				Messages.get(WndVesk.class, "nevermind"));
		this.vesk = vesk;
		Vesk.Quest.met = true;
	}

	private static String greeting(){
		String text = Messages.get(Vesk.class, Vesk.Quest.met ? "return" : "intro");
		if (Dungeon.hero.belongings.getItem(TengusMask.class) != null){
			text += "\n\n" + Messages.get(Vesk.class, "see_mask");
		}
		if (Dungeon.hero.heroClass == HeroClass.ROGUE
				&& Dungeon.hero.belongings.getItem(CloakOfShadows.class) != null){
			text += "\n\n" + Messages.get(Vesk.class, "see_cloak");
		}
		return text;
	}

	@Override
	protected void onSelect( int index ) {
		if (index != 0){
			vesk.yell(Messages.get(Vesk.class, "nevermind"));
		} else if (!hasOffer()){
			vesk.yell(Messages.get(Vesk.class, "nothing"));
		} else {
			GameScene.selectItem(new Offering());
		}
	}

	private static boolean hasOffer(){
		for (Item item : Dungeon.hero.belongings){
			if (Vesk.accepts(item)) return true;
		}
		return false;
	}

	private class Offering extends WndBag.ItemSelector {
		@Override
		public String textPrompt() {
			return Messages.get(WndVesk.class, "choose");
		}

		@Override
		public boolean itemSelectable( Item item ) {
			return Vesk.accepts(item);
		}

		@Override
		public void onSelect( Item item ) {
			if (item != null) GameScene.show(new Confirm(item));
		}
	}

	//which of the four offers this is, used for his lines
	private static String kind( Item item ){
		if (item instanceof TengusMask)     return "mask";
		if (item instanceof CloakOfShadows) return "cloak";
		if (item instanceof Ring || item instanceof Artifact || item instanceof Wand) return "relic";
		return "gear";
	}

	private class Confirm extends WndOptions {

		private final Item item;

		Confirm( Item item ){
			super(new ItemSprite(item),
					Messages.titleCase(item.name()),
					Messages.get(Vesk.class, "confirm_" + kind(item), Vesk.goldFor(item)),
					Messages.get(WndVesk.class, "accept"),
					Messages.get(WndVesk.class, "back"));
			this.item = item;
		}

		@Override
		protected void onSelect( int index ) {
			if (index == 0){
				String reply = Messages.get(Vesk.class, "reply_" + kind(item));
				Vesk.makeDeal(Dungeon.hero, item);
				GameScene.show(new WndQuest(vesk, reply));
			} else {
				vesk.yell(Messages.get(Vesk.class, "hesitate"));
			}
		}

		@Override
		public void onBackPressed() {
			vesk.yell(Messages.get(Vesk.class, "hesitate"));
			super.onBackPressed();
		}
	}
}
