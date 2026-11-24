package com.zrp200.scrollofdebug;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollingGridPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTextInput;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.input.PointerEvent;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

/**
 * Debug window listing every item in the game, laid out like the journal's catalog.
 * Left click gives the item at +0, right click asks for a level and quantity first.
 */
public class WndGiveItem extends Window {

    private static final int WIDTH_P = 126, HEIGHT_P = 180;
    private static final int WIDTH_L = 216, HEIGHT_L = 130;
    private static final int BUTTON_HEIGHT = 18;
    private static final int STATUS_HEIGHT = 12;

    private static final int EQUIPMENT = 0, CONSUMABLES = 1;
    private static int currentTab = EQUIPMENT;
    private static final float[] scrollPositions = new float[2];

    private final RedButton[] tabButtons = new RedButton[2];
    private final ScrollingGridPane grid;
    private final RenderedTextBlock status;

    public WndGiveItem() {
        super();

        int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;
        int height = PixelScene.landscape() ? HEIGHT_L : HEIGHT_P;
        resize(width, height);

        tabButtons[EQUIPMENT] = new RedButton("") {
            @Override protected void onClick() { showTab(EQUIPMENT); }
        };
        tabButtons[EQUIPMENT].icon(new ItemSprite(ItemSpriteSheet.WEAPON_HOLDER));
        tabButtons[CONSUMABLES] = new RedButton("") {
            @Override protected void onClick() { showTab(CONSUMABLES); }
        };
        tabButtons[CONSUMABLES].icon(new ItemSprite(ItemSpriteSheet.POTION_HOLDER));
        for (int i = 0; i < tabButtons.length; i++) {
            add(tabButtons[i]);
            tabButtons[i].setRect(i * width / 2f, 0, width / 2f, BUTTON_HEIGHT);
            PixelScene.align(tabButtons[i]);
        }

        status = PixelScene.renderTextBlock("Left click: give +0.  Right click: choose level and quantity.", 6);
        status.maxWidth(width);
        add(status);
        status.setPos(0, height - STATUS_HEIGHT + 2);

        grid = new ScrollingGridPane() {
            @Override
            public synchronized void update() {
                super.update();
                scrollPositions[currentTab] = content.camera.scroll.y;
            }
        };
        add(grid);
        grid.setRect(0, BUTTON_HEIGHT + 1, width, height - BUTTON_HEIGHT - STATUS_HEIGHT - 2);

        showTab(currentTab);
    }

    private void showTab(int tab) {
        currentTab = tab;
        for (int i = 0; i < tabButtons.length; i++) {
            if (i == tab) tabButtons[i].icon().color(TITLE_COLOR);
            else tabButtons[i].icon().resetColor();
        }

        grid.clear();
        grid.scrollTo(0, 0);
        for (Catalog catalog : tab == EQUIPMENT ? Catalog.equipmentCatalogs : Catalog.consumableCatalogs) {
            ArrayList<Class<? extends Item>> classes = new ArrayList<>();
            for (Class<?> cls : catalog.items()) {
                if (Item.class.isAssignableFrom(cls)) classes.add(cls.asSubclass(Item.class));
            }
            if (classes.isEmpty()) continue; // e.g. enchantments and glyphs are not items

            grid.addHeader("_" + Messages.titleCase(catalog.title()) + "_ (" + classes.size() + "):");
            for (Class<? extends Item> cls : classes) addGridItem(cls);
        }
        grid.setRect(grid.left(), grid.top(), grid.width(), grid.height());
        grid.scrollTo(0, scrollPositions[tab]);
    }

    private void addGridItem(final Class<? extends Item> cls) {
        Item item = Reflection.newInstance(cls);
        if (item == null) return;
        // show the real appearance rather than this run's randomized one
        if (item instanceof Ring) ((Ring) item).anonymize();
        else if (item instanceof Potion) ((Potion) item).anonymize();
        else if (item instanceof Scroll) ((Scroll) item).anonymize();

        ScrollingGridPane.GridItem gridItem = new ScrollingGridPane.GridItem(new ItemSprite(item.image, item.glowing())) {
            @Override
            public boolean onClick(float x, float y) {
                if (!inside(x, y)) return false;
                if (grid.lastClickButton == PointerEvent.RIGHT) askLevelAndQuantity(cls);
                else give(cls, 0, 1);
                return true;
            }
        };
        grid.addItem(gridItem);
    }

    private void askLevelAndQuantity(final Class<? extends Item> cls) {
        String name = Messages.titleCase(Reflection.newInstance(cls).name());
        GameScene.show(new WndTextInput(name, "Enter: level quantity (for example \"3 5\"). Both are optional.",
                "0 1", 20, false, "Give", "Cancel") {
            @Override
            public void onSelect(boolean positive, String text) {
                if (!positive) return;
                String[] parts = text.trim().split("\\s+");
                try {
                    int level = parts.length > 0 && !parts[0].isEmpty() ? Integer.parseInt(parts[0].replace("+", "")) : 0;
                    int quantity = parts.length > 1 ? Integer.parseInt(parts[1].replaceFirst("^x", "")) : 1;
                    if (quantity < 1) throw new NumberFormatException();
                    give(cls, level, quantity);
                } catch (NumberFormatException e) {
                    setStatus("Invalid input '" + text + "'. Use: level quantity");
                }
            }
        });
    }

    /** Stackable items are given as one stack, anything else as several separate items. */
    private void give(Class<? extends Item> cls, int level, int quantity) {
        Hero hero = Dungeon.hero;
        Item first = null;
        int copies = Reflection.newInstance(cls).stackable ? 1 : quantity;
        for (int i = 0; i < copies; i++) {
            Item item = Reflection.newInstance(cls);
            item.identify();
            if (level != 0) item.level(level);
            if (item.stackable) item.quantity(quantity);
            if (first == null) first = item;

            if (item.doPickUp(hero)) {
                hero.spend(-hero.cooldown()); // picking up shouldn't cost a turn
            } else {
                Dungeon.level.drop(item, hero.pos).sprite.drop();
            }
        }
        String name = first.name();
        String amount = quantity > 1 ? " x" + quantity : "";
        String lvl = level != 0 ? (level > 0 ? " +" : " ") + level : "";
        GLog.i("Gave %s%s%s", name, lvl, amount);
        setStatus("Gave " + name + lvl + amount);
    }

    private void setStatus(String text) {
        status.text(text);
        status.maxWidth(width);
    }

    /** Debug-only HUD button that opens the window. */
    public static class OpenButton extends IconButton {
        public OpenButton() {
            super(new ItemSprite(new ScrollOfDebug()));
        }

        @Override
        protected void onClick() {
            GameScene.show(new WndGiveItem());
        }

        @Override
        protected String hoverText() {
            return "Give item";
        }
    }
}
