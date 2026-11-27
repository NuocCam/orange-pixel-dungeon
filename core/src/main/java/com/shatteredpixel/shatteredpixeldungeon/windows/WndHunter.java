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
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Hunter;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.BearPelt;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.DireWolfPelt;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.GeckoTail;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.WolfPelt;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.ItemButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.utils.Reflection;

//everything the hunter buys, a row for each. A click sells one
public class WndHunter extends Window {

	private static final int WIDTH		= 120;
	private static final int BTN_HEIGHT	= 18;
	private static final int BTN_SIZE	= 32;
	private static final int BTN_GAP	= 5;
	private static final int GAP		= 2;

	private static final Class<?>[] BUYS =
			{ WolfPelt.class, BearPelt.class, DireWolfPelt.class, GeckoTail.class };

	@SuppressWarnings("unchecked")
	public WndHunter( final Hunter hunter ) {
		super();

		IconTitle titlebar = new IconTitle();
		titlebar.icon( hunter.sprite() );
		titlebar.label( Messages.titleCase( hunter.name() ) );
		titlebar.setRect( 0, 0, WIDTH, 0 );
		add( titlebar );

		RenderedTextBlock message = PixelScene.renderTextBlock( Messages.get(this, "prompt"), 6 );
		message.maxWidth(WIDTH);
		message.setPos(0, titlebar.bottom() + GAP);
		add( message );

		float pos = message.bottom() + GAP;
		for (Class<?> cls : BUYS){
			final Class<? extends Item> pelt = (Class<? extends Item>) cls;
			Item carried = Dungeon.hero.belongings.getItem(pelt);
			boolean wanted = Hunter.rewardsFor(pelt).length > 0;

			String key = wanted ? cls.getSimpleName() : "sold_out";
			RedButton btn = new RedButton(Messages.get(this, key, carried == null ? 0 : carried.quantity()), 6){
				@Override
				protected void onClick() {
					WndHunter.this.hide();
					offer( hunter, pelt );
				}
			};
			btn.icon( new ItemSprite( Reflection.newInstance(pelt) ) );
			btn.enable( carried != null && wanted );
			btn.setRect( 0, pos, WIDTH, BTN_HEIGHT );
			add( btn );
			pos = btn.bottom() + GAP;
		}

		resize(WIDTH, (int) pos);
	}

	private static void offer( final Hunter hunter, final Class<? extends Item> pelt ){
		final Item[] rewards = Hunter.rewardsFor(pelt);
		if (rewards.length == 1){
			sell( hunter, pelt, rewards[0] );
		} else if (pelt == BearPelt.class){
			GameScene.show( new BearWindow( hunter, rewards ) );
		} else {
			String[] names = new String[rewards.length];
			for (int i = 0; i < rewards.length; i++){
				//the plain name, we don't want to give away a colour or a rune
				names[i] = rewards[i] instanceof Gold
						? Messages.get(WndHunter.class, "gold", rewards[i].quantity())
						: Messages.titleCase(rewards[i].trueName());
			}
			Item shown = Reflection.newInstance(pelt);
			GameScene.show( new WndOptions( new ItemSprite( shown ), Messages.titleCase( shown.name() ),
					Messages.get(WndHunter.class, "pick"), names ){
				@Override
				protected void onSelect( int index ) {
					sell( hunter, pelt, rewards[index] );
				}
			});
		}
	}

	private static void sell( Hunter hunter, Class<? extends Item> pelt, Item reward ){
		if (hunter.buy( pelt, reward )){
			GameScene.show( new WndHunter( hunter ) );
		}
	}

	//the weapon or the armor, laid out like the ghost's reward
	private static class BearWindow extends Window {

		BearWindow( final Hunter hunter, Item[] rewards ){
			super();

			IconTitle titlebar = new IconTitle();
			BearPelt pelt = new BearPelt();
			titlebar.icon( new ItemSprite( pelt ) );
			titlebar.label( Messages.titleCase( pelt.name() ) );
			titlebar.setRect( 0, 0, WIDTH, 0 );
			add( titlebar );

			RenderedTextBlock message = PixelScene.renderTextBlock( Messages.get(WndHunter.class, "bear_pick"), 6 );
			message.maxWidth(WIDTH);
			message.setPos(0, titlebar.bottom() + GAP);
			add( message );

			float left = (WIDTH - BTN_GAP) / 2 - BTN_SIZE;
			for (Item reward : rewards){
				ItemButton btn = new ItemButton(){
					@Override
					protected void onClick() {
						GameScene.show( new ConfirmWindow( hunter, BearWindow.this, item() ) );
					}
				};
				btn.item( reward );
				btn.setRect( left, message.bottom() + BTN_GAP, BTN_SIZE, BTN_SIZE );
				add( btn );
				left = btn.right() + BTN_GAP;
			}

			resize(WIDTH, (int) (message.bottom() + 2*BTN_GAP + BTN_SIZE));
		}
	}

	private static class ConfirmWindow extends WndInfoItem {

		ConfirmWindow( final Hunter hunter, final Window choice, final Item item ) {
			super(item);

			RedButton btnConfirm = new RedButton(Messages.get(WndSadGhost.class, "confirm")){
				@Override
				protected void onClick() {
					ConfirmWindow.this.hide();
					choice.hide();
					sell( hunter, BearPelt.class, item );
				}
			};
			btnConfirm.setRect(0, height+2, width/2-1, 16);
			add(btnConfirm);

			RedButton btnCancel = new RedButton(Messages.get(WndSadGhost.class, "cancel")){
				@Override
				protected void onClick() {
					ConfirmWindow.this.hide();
				}
			};
			btnCancel.setRect(btnConfirm.right()+2, height+2, btnConfirm.width(), 16);
			add(btnCancel);

			resize(width, (int)btnCancel.bottom());
		}
	}
}
