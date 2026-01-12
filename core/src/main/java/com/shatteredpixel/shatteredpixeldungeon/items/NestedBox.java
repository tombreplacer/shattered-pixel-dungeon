/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class NestedBox extends Item {
	
	public static final String AC_OPEN = "OPEN";
	
	private static final int MAX_NESTING = 20;
	
	private int nestingLevel = 1;
	
	{
		image = ItemSpriteSheet.CHEST;
		defaultAction = AC_OPEN;
		unique = true;
	}
	
	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_OPEN);
		return actions;
	}
	
	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);
		
		if (action.equals(AC_OPEN)) {
			openBox(hero);
		}
	}
	
	private void openBox(Hero hero) {
		detach(hero.belongings.backpack);
		
		hero.sprite.operate(hero.pos);
		Sample.INSTANCE.play(Assets.Sounds.UNLOCK);
		CellEmitter.center(hero.pos).burst(Speck.factory(Speck.STAR), 5);
		
		if (nestingLevel >= MAX_NESTING) {
			// Последний ящик - даем крутой лут
			Item loot = generateAwesomeLoot();
			if (loot != null) {
				loot.identify();
				if (loot.collect(hero.belongings.backpack)) {
					GLog.p(Messages.get(this, "final_loot", loot.name()));
				} else {
					Dungeon.level.drop(loot, hero.pos).sprite.drop();
					GLog.p(Messages.get(this, "final_loot_ground", loot.name()));
				}
			}
		} else {
			// Создаем следующий вложенный ящик
			NestedBox nextBox = new NestedBox();
			nextBox.nestingLevel = this.nestingLevel + 1;
			nextBox.identify();
			
			if (nextBox.collect(hero.belongings.backpack)) {
				GLog.i(Messages.get(this, "opened", nestingLevel, MAX_NESTING));
			} else {
				Dungeon.level.drop(nextBox, hero.pos).sprite.drop();
				GLog.i(Messages.get(this, "opened_ground", nestingLevel, MAX_NESTING));
			}
		}
		
		hero.spendAndNext(1f);
	}
	
	private Item generateAwesomeLoot() {
		// Генерируем крутой случайный лут
		// С большей вероятностью даем артефакты, улучшенное оружие/броню, редкие предметы
		float roll = Random.Float();
		
		if (roll < 0.3f) {
			// 30% - артефакт
			Artifact artifact = Generator.randomArtifact();
			if (artifact != null) {
				artifact.identify();
				// Улучшаем артефакт
				for (int i = 0; i < 3 + Random.Int(3); i++) {
					artifact.upgrade();
				}
				return artifact;
			}
		} else if (roll < 0.6f) {
			// 30% - улучшенное оружие
			MeleeWeapon weapon = Generator.randomWeapon();
			if (weapon != null) {
				weapon.identify();
				for (int i = 0; i < 5 + Random.Int(5); i++) {
					weapon.upgrade();
				}
				// Добавляем зачарование
				if (Random.Int(3) == 0) {
					weapon.enchant(MeleeWeapon.Enchantment.random());
				}
				return weapon;
			}
		} else if (roll < 0.8f) {
			// 20% - улучшенная броня
			Armor armor = Generator.randomArmor();
			if (armor != null) {
				armor.identify();
				for (int i = 0; i < 5 + Random.Int(5); i++) {
					armor.upgrade();
				}
				// Добавляем глиф
				if (Random.Int(3) == 0) {
					armor.inscribe(Armor.Glyph.random());
				}
				return armor;
			}
		} else if (roll < 0.95f) {
			// 15% - улучшенная палочка
			Item wandItem = Generator.random(Generator.Category.WAND);
			if (wandItem instanceof Wand) {
				Wand wand = (Wand)wandItem;
				wand.identify();
				for (int i = 0; i < 3 + Random.Int(3); i++) {
					wand.upgrade();
				}
				return wand;
			}
		} else {
			// 5% - улучшенное кольцо
			Item ringItem = Generator.random(Generator.Category.RING);
			if (ringItem instanceof Ring) {
				Ring ring = (Ring)ringItem;
				ring.identify();
				for (int i = 0; i < 3 + Random.Int(3); i++) {
					ring.upgrade();
				}
				return ring;
			}
		}
		
		// Fallback - случайный предмет
		Item item = Generator.random();
		if (item != null && item.isUpgradable()) {
			for (int i = 0; i < 3 + Random.Int(3); i++) {
				item.upgrade();
			}
		}
		return item;
	}
	
	@Override
	public boolean isIdentified() {
		return true;
	}
	
	@Override
	public boolean isUpgradable() {
		return false;
	}
	
	@Override
	public int value() {
		return 100 * nestingLevel;
	}
	
	private static final String NESTING = "nesting";
	
	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(NESTING, nestingLevel);
	}
	
	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		nestingLevel = bundle.getInt(NESTING);
	}
	
	@Override
	public String name() {
		if (nestingLevel >= MAX_NESTING) {
			return Messages.get(this, "name_final");
		}
		return Messages.get(this, "name", nestingLevel, MAX_NESTING);
	}
	
	@Override
	public String info() {
		if (nestingLevel >= MAX_NESTING) {
			return Messages.get(this, "desc_final");
		}
		return Messages.get(this, "desc", nestingLevel, MAX_NESTING);
	}
}
