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

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.journal.MobInfo;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.Reflection;

//a bestiary page: the description, then the quick stats and drops. It scrolls when it's long
public class WndBestiaryEntry extends Window {

	private static final int WIDTH_P = 130;
	private static final int WIDTH_L = 180;
	private static final int GAP = 2;
	//the sprite column of the drop list
	private static final int ICON_SIZE = 16;

	public WndBestiaryEntry( Image icon, String title, String description, MobInfo info ){
		super();

		int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;

		IconTitle titlebar = new IconTitle(icon, title);
		titlebar.setRect(0, 0, width, 0);
		add(titlebar);

		Component content = new Component();
		//leaves the scroll bar its own strip, so it never covers any letters
		int inner = width - 3;

		float y = text(content, description, 0, 0, inner) + 2*GAP;

		if (info.hp != null){
			y = line(content, "hp", MobInfo.word(info.hp), y, inner);
			y = line(content, "damage", MobInfo.word(info.damage), y, inner);
			y = line(content, "evasion", MobInfo.word(info.evasion), y, inner);
			y = line(content, "defense", MobInfo.word(info.defense), y, inner);
		}
		if (info.ability != null){
			y = line(content, "ability", info.abilityText(), y, inner);
		}

		y += GAP;
		if (info.drops.isEmpty()){
			y = line(content, "drops", Messages.get(MobInfo.class, "no_drops"), y, inner);
		} else {
			y = line(content, "drops", "", y, inner);
			boolean fades = false;
			for (MobInfo.Drop drop : info.drops){
				y = dropRow(content, drop, y, inner);
				fades |= drop.fades;
			}
			if (fades){
				y = text(content, Messages.get(MobInfo.class, "fades_note"), 0, y, inner) + GAP;
			}
		}

		float top = titlebar.bottom() + 2*GAP;
		float maxHeight = PixelScene.uiCamera.height * 0.85f - top;
		int height = (int)Math.ceil(Math.min(y, maxHeight));

		content.setSize(width, y);
		resize(width, (int)top + height);

		//has to be sized after the window is, so its camera lands in the right place
		ScrollPane pane = new ScrollPane(content);
		add(pane);
		pane.setRect(0, top, width, height);
	}

	private static float text( Component content, String message, float x, float y, float width ){
		RenderedTextBlock block = PixelScene.renderTextBlock(message, 6);
		block.maxWidth((int)(width - x));
		block.setPos(x, y);
		content.add(block);
		return block.bottom();
	}

	private static float line( Component content, String label, String value, float y, float width ){
		return text(content, "_" + Messages.get(MobInfo.class, label) + ":_ " + value, 0, y, width) + GAP;
	}

	private static float dropRow( Component content, MobInfo.Drop drop, float y, float width ){
		Image sprite = new ItemSprite(drop.image());
		Image typeIcon = null;
		if (drop.item != null){
			Item item = Reflection.newInstance(drop.item);
			//shown the way the item list does it, not with this run's colors and runes
			if (item instanceof Potion){
				((Potion) item).anonymize();
			} else if (item instanceof Scroll){
				((Scroll) item).anonymize();
			} else if (item instanceof Ring){
				((Ring) item).anonymize();
			}
			sprite = new ItemSprite(item.image);
			if (item.icon != -1){
				typeIcon = new Image(Assets.Sprites.ITEM_ICONS);
				typeIcon.frame(ItemSpriteSheet.Icons.film.get(item.icon));
			}
		}

		String label = drop.name() + ": _" + drop.chanceText() + "_";
		if (drop.note != null){
			label += " (" + drop.noteText() + ")";
		}
		RenderedTextBlock block = PixelScene.renderTextBlock(label, 6);
		block.maxWidth((int)(width - ICON_SIZE - 2*GAP));

		float rowHeight = Math.max(ICON_SIZE, block.height());

		sprite.x = (ICON_SIZE - sprite.width()) / 2f;
		sprite.y = y + (rowHeight - sprite.height()) / 2f;
		PixelScene.align(sprite);
		content.add(sprite);

		if (typeIcon != null){
			typeIcon.x = ICON_SIZE - typeIcon.width();
			typeIcon.y = y + (rowHeight - ICON_SIZE) / 2f;
			PixelScene.align(typeIcon);
			content.add(typeIcon);
		}

		block.setPos(ICON_SIZE + 2*GAP, y + (rowHeight - block.height()) / 2f);
		content.add(block);

		return y + rowHeight + GAP;
	}

}
