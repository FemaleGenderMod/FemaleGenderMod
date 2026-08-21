/*
 * Wildfire's Female Gender Mod is a female gender mod created for Minecraft.
 * Copyright (C) 2023-present WildfireRomeo
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.wildfire.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.wildfire.api.impl.BreastArmorTexture;
import com.wildfire.api.impl.GenderArmor;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.TriState;
import org.jetbrains.annotations.ApiStatus;

/// Interface supplying values to determine how an armor piece interacts with a wearer's breasts
public interface IGenderArmor {
    /// Default implementation used to represent armor types that lack any configuration
    IGenderArmor DEFAULT = new IGenderArmor() {
    };

    /// Default implementation used when the player [`isn't wearing a chestplate`][net.minecraft.world.item.ItemStack#isEmpty()],
    /// or if the worn chestplate specifies that it doesn't cover the breasts.
    IGenderArmor EMPTY = new IGenderArmor() {
        @Override
        public boolean coversBreasts() {
            return false;
        }

        @Override
        public float physicsResistance() {
            return 0;
        }

        @Override
        public boolean armorStandsCopySettings() {
            return false;
        }
    };

    @ApiStatus.Internal
    Codec<IGenderArmor> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.floatRange(0f, 1f).optionalFieldOf("resistance", 0.5f).forGetter(IGenderArmor::physicsResistance),
            ExtraCodecs.floatRange(0f, 1f).optionalFieldOf("tightness", 0f).forGetter(IGenderArmor::tightness),
            Codec.BOOL.optionalFieldOf("covers_breasts", true).forGetter(IGenderArmor::coversBreasts),
            Codec.BOOL.optionalFieldOf("hide_breasts", false).forGetter(IGenderArmor::alwaysHidesBreasts),
            TriState.CODEC.optionalFieldOf("render_on_armor_stands", TriState.DEFAULT).forGetter(armor -> armor.armorStandsCopySettings() ? TriState.TRUE : TriState.FALSE),
            IBreastArmorTexture.CODEC.optionalFieldOf("texture", IBreastArmorTexture.DEFAULT).forGetter(IGenderArmor::texture)
    ).apply(instance, (resistance, tightness, covers, hideBreasts, armorStands, texture) -> {
        if(!covers) {
            return EMPTY;
        }
        return new GenderArmor(resistance, tightness, true, hideBreasts, armorStands.toBoolean(resistance == 1f), texture);
    }));

    /// Determines whether this [IGenderArmor] "covers" the breasts or if it has an open front (`false`) like the elytra.
    ///
    /// If this returns `false` the breast armor layer will not be rendered while this item is worn, as if
    /// the item simply didn't exist.
    ///
    /// @return `true` if this armor piece covers the wearer's breasts in any capacity.
    ///
    /// @implNote Defaults to `true`.
    default boolean coversBreasts() {
        return true;
    }

    /// Determines if this [IGenderArmor] should always hide the wearer's breasts when worn even if they have
    /// `showBreastsInArmor` set to `true`.
    ///
    /// This is intended for armors that may have custom rendering that is not compatible with how breasts render
    /// and would just lead to clipping or other unintended behavior.
    ///
    /// @return `true` to always hide the breasts of players wearing this armor piece.
    ///
    /// @implNote Defaults to `false`.
    default boolean alwaysHidesBreasts() {
        return false;
    }

    /// The percent of physical resistance this [IGenderArmor] provides to the wearer's breasts when calculating
    /// the corresponding physics.
    ///
    /// @return Value between `0` (no resistance, full physics) and `1` (total resistance, no physics).
    ///
    /// @implNote Defaults to `0.5f` (50% physics resistance).
    default float physicsResistance() {
        return 0.5f;
    }

    /// Value representing how "tight" this [IGenderArmor] is. Tightness "compresses" the breasts against the wearer,
    /// causing the breasts to appear up to 15% smaller.
    ///
    /// @return Value between `0` (no tightness, no size reduction) and `1` (full tightness, `15%` size reduction).
    ///
    /// @implNote Defaults to `0` (no tightness, no size reduction).
    default float tightness() {
        return 0;
    }

    /// Determines whether armor stands should copy the breast settings of the player equipping this chestplate
    /// onto it.
    ///
    /// If this returns `true`, the equipping player's breast settings will also be rendered when this
    /// armor piece is equipped onto an armor stand.
    ///
    /// This is designed for armor types that are metallic in nature, and not armor types that would (realistically)
    /// be flexible enough to accommodate for the wearer's breasts on their own (such as Leather and Chain).
    ///
    /// @return `true` to copy the equipping player's breast settings onto this armor type when equipped onto
    /// 		 armor stands, and render the relevant breast settings on the armor stand.
    ///
    /// @implNote Defaults to returning `true` if this armor [`covers the breasts`][#coversBreasts()]
    /// 		   (and [`doesn't hide them`][#alwaysHidesBreasts()]), and [`has complete physics resistance`][#physicsResistance()].
    ///
    /// @see com.wildfire.common.entitydata.BreastDataComponent
    default boolean armorStandsCopySettings() {
        return !alwaysHidesBreasts() && coversBreasts() && physicsResistance() == 1f;
    }

    /// Overrides certain values when this armor piece is being rendered
    ///
    /// @return The relevant [IBreastArmorTexture]
    ///
    /// @implNote Defaults to [IBreastArmorTexture#DEFAULT]
    ///
    /// @see IBreastArmorTexture
    /// @see BreastArmorTexture
    default IBreastArmorTexture texture() {
        return IBreastArmorTexture.DEFAULT;
    }
}
