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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class MagicDildo extends MissileWeapon {
	
	{
		image = ItemSpriteSheet.THROWING_SPEAR; // Используем спрайт копья
		hitSound = Assets.Sounds.HIT_STAB;
		hitSoundPitch = 1.0f;
		
		bones = false;
		
		tier = 3; // Средний тир для баланса
		baseUses = 10; // Больше использований, чем у обычного копья
		sticky = false;
	}
	
	@Override
	public int max(int lvl) {
		return  6 * tier +                      // 18 базовый урон
				tier * lvl;                     // Масштабирование с уровнем
	}
	
	@Override
	public int min(int lvl) {
		return  3 * tier +                      // 9 минимальный урон
				lvl;                            // Масштабирование с уровнем
	}
}
