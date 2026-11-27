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

package com.shatteredpixel.shatteredpixeldungeon.ui.changelist;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.ChangesScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.BlacksmithSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HunterSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.IjiraqSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

import java.util.ArrayList;

public class Orange_v1_X_Changes {

	private static final int ORANGE = 0xFF9933;

	public static void addAllChanges( ArrayList<ChangeInfo> changeInfos ){
		add_v1_2_Changes(changeInfos);
		add_v1_1_Changes(changeInfos);
		add_v1_0_Changes(changeInfos);
	}

	//every release gets its own full width heading, so nothing looks like it belongs to the one above
	private static void release( ArrayList<ChangeInfo> changeInfos, String version ){
		ChangeInfo changes = new ChangeInfo(version, true, "");
		changes.hardlight(ORANGE);
		changeInfos.add(changes);
	}

	private static ChangeInfo section( ArrayList<ChangeInfo> changeInfos, String key, int color ){
		ChangeInfo changes = new ChangeInfo(Messages.get(ChangesScene.class, key), false, null);
		changes.hardlight(color);
		changeInfos.add(changes);
		return changes;
	}

	public static void add_v1_2_Changes( ArrayList<ChangeInfo> changeInfos ) {

		release(changeInfos, "v1.2.1");

		ChangeInfo changes = section(changeInfos, "new", Window.TITLE_COLOR);

		changes.addButton( new ChangeButton( new BlacksmithSprite(), Messages.get(Orange_v1_X_Changes.class, "v121_masterwork_title"),
				Messages.get(Orange_v1_X_Changes.class, "v121_masterwork_text")));

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.SKULL_CHALICE), Messages.get(Orange_v1_X_Changes.class, "v121_skull_chalice_title"),
				Messages.get(Orange_v1_X_Changes.class, "v121_skull_chalice_text")));

		changes.addButton( new ChangeButton( Icons.get(Icons.WELL_ENCHANTMENT), Messages.get(Orange_v1_X_Changes.class, "v121_magic_wells_title"),
				Messages.get(Orange_v1_X_Changes.class, "v121_magic_wells_text")));

		changes = section(changeInfos, "changes", CharSprite.WARNING);

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.RING_TOPAZ), Messages.get(Orange_v1_X_Changes.class, "v121_ring_of_wealth_title"),
				Messages.get(Orange_v1_X_Changes.class, "v121_ring_of_wealth_text")));

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.ARTIFACT_SPELLBOOK), Messages.get(Orange_v1_X_Changes.class, "v121_spellbook_title"),
				Messages.get(Orange_v1_X_Changes.class, "v121_spellbook_text")));

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.CROSSBOW), Messages.get(Orange_v1_X_Changes.class, "v121_crossbow_title"),
				Messages.get(Orange_v1_X_Changes.class, "v121_crossbow_text")));

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.RUNIC_BLADE), Messages.get(Orange_v1_X_Changes.class, "v121_runic_blade_title"),
				Messages.get(Orange_v1_X_Changes.class, "v121_runic_blade_text")));

		changes.addButton( new ChangeButton( Icons.get(Icons.WELL_AWARENESS), Messages.get(Orange_v1_X_Changes.class, "v121_well_of_awareness_title"),
				Messages.get(Orange_v1_X_Changes.class, "v121_well_of_awareness_text")));

		changes.addButton( new ChangeButton( ChangeIcons.V061_BUGFIX, Messages.get(ChangesScene.class, "bugfixes"),
				Messages.get(Orange_v1_X_Changes.class, "v121_bugfixes_text")));

		release(changeInfos, "v1.2.0");

		changes = section(changeInfos, "new", Window.TITLE_COLOR);

		//same spot shattered puts its commentary in the big updates
		changes.addButton( new ChangeButton( Icons.get(Icons.NEWS), Messages.get(Orange_v1_X_Changes.class, "v120_developer_commentary_title"),
				Messages.get(Orange_v1_X_Changes.class, "v120_developer_commentary_text")));

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.JUNGLE_BERRY), Messages.get(Orange_v1_X_Changes.class, "v120_jungle_floors_title"),
				Messages.get(Orange_v1_X_Changes.class, "v120_jungle_floors_text")));

		changes.addButton( new ChangeButton( new HunterSprite(), Messages.get(Orange_v1_X_Changes.class, "v120_monsters_quests_title"),
				Messages.get(Orange_v1_X_Changes.class, "v120_monsters_quests_text")));

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.BEAST_MARROW), Messages.get(Orange_v1_X_Changes.class, "v120_amalgam_title"),
				Messages.get(Orange_v1_X_Changes.class, "v120_amalgam_text")));

		changes.addButton( new ChangeButton( Icons.get(Icons.BADGES), Messages.get(Orange_v1_X_Changes.class, "v120_puzzle_rooms_badges_title"),
				Messages.get(Orange_v1_X_Changes.class, "v120_puzzle_rooms_badges_text")));

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.STAMPEDE_BOMB), Messages.get(Orange_v1_X_Changes.class, "v120_new_consumables_title"),
				Messages.get(Orange_v1_X_Changes.class, "v120_new_consumables_text")));

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.MAGIC_ORANGE), Messages.get(Orange_v1_X_Changes.class, "v120_magic_orange_title"),
				Messages.get(Orange_v1_X_Changes.class, "v120_magic_orange_text")));

		changes.addButton( new ChangeButton( new IjiraqSprite(), Messages.get(Orange_v1_X_Changes.class, "v120_ijiraq_title"),
				Messages.get(Orange_v1_X_Changes.class, "v120_ijiraq_text")));

		changes = section(changeInfos, "changes", CharSprite.WARNING);

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.ARTIFACT_CAPE), Messages.get(Orange_v1_X_Changes.class, "v120_cape_of_thorns_title"),
				Messages.get(Orange_v1_X_Changes.class, "v120_cape_of_thorns_text")));

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.ARTIFACT_ROSE3), Messages.get(Orange_v1_X_Changes.class, "v120_ghost_lore_title"),
				Messages.get(Orange_v1_X_Changes.class, "v120_ghost_lore_text")));

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.ARTIFACT_MEDIC_KIT), Messages.get(Orange_v1_X_Changes.class, "v120_medic_kit_title"),
				Messages.get(Orange_v1_X_Changes.class, "v120_medic_kit_text")));

		changes.addButton( new ChangeButton( new IjiraqSprite(), Messages.get(Orange_v1_X_Changes.class, "v120_ijiraq_drops_title"),
				Messages.get(Orange_v1_X_Changes.class, "v120_ijiraq_drops_text")));

		changes.addButton( new ChangeButton( ChangeIcons.V061_BUGFIX, Messages.get(ChangesScene.class, "bugfixes"),
				Messages.get(Orange_v1_X_Changes.class, "v120_bugfixes_text")));
	}

	public static void add_v1_1_Changes( ArrayList<ChangeInfo> changeInfos ) {

		release(changeInfos, "v1.1.2");

		ChangeInfo changes = section(changeInfos, "new", Window.TITLE_COLOR);

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.ARTIFACT_CITRINE2), Messages.get(Orange_v1_X_Changes.class, "v112_ascendant_citrine_title"),
				Messages.get(Orange_v1_X_Changes.class, "v112_ascendant_citrine_text")));

		changes = section(changeInfos, "changes", CharSprite.WARNING);

		changes.addButton( new ChangeButton( ChangeIcons.V081_MISC, Messages.get(Orange_v1_X_Changes.class, "v112_title_screen_icons_title"),
				Messages.get(Orange_v1_X_Changes.class, "v112_title_screen_icons_text")));

		release(changeInfos, "v1.1.1");

		changes = section(changeInfos, "changes", CharSprite.WARNING);

		changes.addButton( new ChangeButton( ChangeIcons.V061_BUGFIX, Messages.get(ChangesScene.class, "bugfixes"),
				Messages.get(Orange_v1_X_Changes.class, "v111_bugfixes_text")));

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.ARTIFACT_MEDIC_KIT), Messages.get(Orange_v1_X_Changes.class, "v111_medic_kit_title"),
				Messages.get(Orange_v1_X_Changes.class, "v111_medic_kit_text")));

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.RETURN_BEACON), Messages.get(Orange_v1_X_Changes.class, "v111_beacon_of_returning_title"),
				Messages.get(Orange_v1_X_Changes.class, "v111_beacon_of_returning_text")));

		release(changeInfos, "v1.1.0");

		changes = section(changeInfos, "new", Window.TITLE_COLOR);

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.ARTIFACT_MEDIC_KIT), Messages.get(Orange_v1_X_Changes.class, "v110_orange_versions_changelog_title"),
				Messages.get(Orange_v1_X_Changes.class, "v110_orange_versions_changelog_text")));

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.SPYGLASS_REPAIRED), Messages.get(Orange_v1_X_Changes.class, "v110_spyglass_title"),
				Messages.get(Orange_v1_X_Changes.class, "v110_spyglass_text")));

		changes = section(changeInfos, "changes", CharSprite.WARNING);

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.MOSSY_CLUMP), Messages.get(Orange_v1_X_Changes.class, "v110_mossy_clump_title"),
				Messages.get(Orange_v1_X_Changes.class, "v110_mossy_clump_text")));

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.SUNDIAL), Messages.get(Orange_v1_X_Changes.class, "v110_dimensional_sundial_title"),
				Messages.get(Orange_v1_X_Changes.class, "v110_dimensional_sundial_text")));

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.SPYGLASS), Messages.get(Orange_v1_X_Changes.class, "v110_cracked_spyglass_title"),
				Messages.get(Orange_v1_X_Changes.class, "v110_cracked_spyglass_text")));

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.ARTIFACT_TALISMAN), Messages.get(Orange_v1_X_Changes.class, "v110_talisman_of_foresight_title"),
				Messages.get(Orange_v1_X_Changes.class, "v110_talisman_of_foresight_text")));

		changes.addButton( new ChangeButton( Icons.get(Icons.INFO), Messages.get(Orange_v1_X_Changes.class, "v110_about_screen_title"),
				Messages.get(Orange_v1_X_Changes.class, "v110_about_screen_text")));

		changes = section(changeInfos, "buffs", CharSprite.POSITIVE);

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.WAND_LIVING_EARTH), Messages.get(Orange_v1_X_Changes.class, "v110_earthen_guardian_title"),
				Messages.get(Orange_v1_X_Changes.class, "v110_earthen_guardian_text")));

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.PARCHMENT_SCRAP), Messages.get(Orange_v1_X_Changes.class, "v110_parchment_scrap_title"),
				Messages.get(Orange_v1_X_Changes.class, "v110_parchment_scrap_text")));

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.ARTIFACT_ROSE3), Messages.get(Orange_v1_X_Changes.class, "v110_dried_rose_title"),
				Messages.get(Orange_v1_X_Changes.class, "v110_dried_rose_text")));

		changes = section(changeInfos, "nerfs", CharSprite.NEGATIVE);

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.PARCHMENT_SCRAP), Messages.get(Orange_v1_X_Changes.class, "v110_parchment_scrap_2_title"),
				Messages.get(Orange_v1_X_Changes.class, "v110_parchment_scrap_2_text")));

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.WAND_LIVING_EARTH), Messages.get(Orange_v1_X_Changes.class, "v110_earthen_guardian_2_title"),
				Messages.get(Orange_v1_X_Changes.class, "v110_earthen_guardian_2_text")));

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.ARTIFACT_ROSE3), Messages.get(Orange_v1_X_Changes.class, "v110_dried_rose_2_title"),
				Messages.get(Orange_v1_X_Changes.class, "v110_dried_rose_2_text")));
	}

	public static void add_v1_0_Changes( ArrayList<ChangeInfo> changeInfos ) {

		ChangeInfo changes = new ChangeInfo("v1.0", true, "");
		changes.hardlight(ORANGE);
		changeInfos.add(changes);

		changes = section(changeInfos, "new", Window.TITLE_COLOR);

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.ARTIFACT_MEDIC_KIT), Messages.get(Orange_v1_X_Changes.class, "v10_welcome_to_orange_title"),
				Messages.get(Orange_v1_X_Changes.class, "v10_welcome_to_orange_text")));

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.ARTIFACT_MEDIC_KIT), Messages.get(Orange_v1_X_Changes.class, "v10_medic_kit_title"),
				Messages.get(Orange_v1_X_Changes.class, "v10_medic_kit_text")));

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.ARTIFACT_CAPE), Messages.get(Orange_v1_X_Changes.class, "v10_artifacts_return_title"),
				Messages.get(Orange_v1_X_Changes.class, "v10_artifacts_return_text")));

		changes.addButton( new ChangeButton( new TalentIcon(Talent.ATK_SPEED_ENHANCE), Messages.get(Orange_v1_X_Changes.class, "v10_universal_tier_3_talents_title"),
				Messages.get(Orange_v1_X_Changes.class, "v10_universal_tier_3_talents_text")));

		changes.addButton( new ChangeButton( new TalentIcon(Talent.MAX_HEALTH), Messages.get(Orange_v1_X_Changes.class, "v10_new_tier_1_2_talents_title"),
				Messages.get(Orange_v1_X_Changes.class, "v10_new_tier_1_2_talents_text")));

		changes.addButton( new ChangeButton( new TalentIcon(Talent.LETHAL_RAGE), Messages.get(Orange_v1_X_Changes.class, "v10_new_subclass_talents_title"),
				Messages.get(Orange_v1_X_Changes.class, "v10_new_subclass_talents_text")));

		changes = section(changeInfos, "changes", CharSprite.WARNING);

		changes.addButton( new ChangeButton( Icons.TALENT.get(), Messages.get(Orange_v1_X_Changes.class, "v10_interface_title"),
				Messages.get(Orange_v1_X_Changes.class, "v10_interface_text")));

		changes = section(changeInfos, "buffs", CharSprite.POSITIVE);

		changes.addButton( new ChangeButton( new ItemSprite(ItemSpriteSheet.ANKH), Messages.get(Orange_v1_X_Changes.class, "v10_blessed_ankh_title"),
				Messages.get(Orange_v1_X_Changes.class, "v10_blessed_ankh_text")));

		changes.addButton( new ChangeButton( ChangeIcons.V081_MISC, Messages.get(Orange_v1_X_Changes.class, "v10_sad_ghost_title"),
				Messages.get(Orange_v1_X_Changes.class, "v10_sad_ghost_text")));
	}
}
