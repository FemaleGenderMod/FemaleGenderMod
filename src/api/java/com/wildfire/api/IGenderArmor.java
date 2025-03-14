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
import com.wildfire.api.impl.GenderArmor;
import net.neoforged.neoforge.common.util.TriState;
import org.jetbrains.annotations.NotNull;

/**
 * Expose this as a capability on your chestplates or items that go in the chest slot to configure how it interacts with breast rendering.
 */
public interface IGenderArmor {

    Codec<IGenderArmor> CODEC = RecordCodecBuilder.create(instance -> instance.group(
          Codec.floatRange(0f, 1f).optionalFieldOf("resistance", 0.5f)
                .forGetter(IGenderArmor::physicsResistance),
          Codec.floatRange(0f, 1f).optionalFieldOf("tightness", 0f)
                .forGetter(IGenderArmor::tightness),
          Codec.BOOL.optionalFieldOf("covers_breasts", true)
                .forGetter(IGenderArmor::coversBreasts),
          Codec.BOOL.optionalFieldOf("hide_breasts", false)
                .forGetter(IGenderArmor::alwaysHidesBreasts),
          WildfireAPI.TRISTATE.optionalFieldOf("render_on_armor_stands", TriState.DEFAULT)
                .forGetter(armor -> armor.armorStandsCopySettings() ? TriState.TRUE : TriState.FALSE),
          IBreastArmorTexture.CODEC.optionalFieldOf("texture", IBreastArmorTexture.DEFAULT)
                .forGetter(IGenderArmor::texture)
    ).apply(instance, (resistance, tightness, covers, hideBreasts, armorStands, texture) -> {
        if (!covers) {
            return GenderArmor.EMPTY;
        }
        //TODO - 1.21.4: Replace this once we are using vanilla's tristate
        return new GenderArmor(resistance, tightness, true, hideBreasts, armorStands.isDefault() ? resistance == 1f : armorStands.isTrue(), texture);
        //return new GenderArmor(resistance, tightness, true, hideBreasts, armorStands.asBoolean(resistance == 1f), texture);
    }));

    /**
     * <p>Determines whether this {@link IGenderArmor} "covers" the breasts or if it has an open front ({@code false}) like the elytra.</p>
     *
     * <p>If this returns {@code false} the breast armor layer will not be rendered while this item is worn, as if the item simply didn't exist.</p>
     *
     * @return {@code true} if the breasts are covered.
     *
     * @implNote Defaults to {@code true}.
     */
    default boolean coversBreasts() {
        return true;
    }

    /**
     * Determines if this {@link IGenderArmor} should always hide the wearer's breasts when worn even if they have {@code showBreastsInArmor} set to {@code true}.
     *
     * <p>This is useful for armors that may have custom rendering that is not compatible with how the breasts render and would just lead to clipping.
     *
     * @return {@code true} to hide the breasts regardless of what {@code showBreastsInArmor} is set to.
     *
     * @implNote Defaults to {@code false}.
     */
    default boolean alwaysHidesBreasts() {
        return false;
    }

    /**
     * The percent of physical resistance this {@link IGenderArmor} provides to the wearer's breasts when calculating the corresponding physics.
     *
     * @return Value between {@code 0} (no resistance, full physics) and {@code 1} (total resistance, no physics).
     *
     * @implNote Defaults to {@code 0.5} (50% physics resistance).
     */
    default float physicsResistance() {
        return 0.5F;
    }

    /**
     * Value representing how "tight" this {@link IGenderArmor} is. Tightness "compresses" the breasts against the wearer, causing the breasts to appear up to {@code 15%}
     * smaller.
     *
     * @return Value between {@code 0} (no tightness, no size reduction) and {@code 1} (full tightness, {@code 15%} size reduction).
     *
     * @implNote Defaults to {@code 0} (no tightness, no size reduction).
     */
    default float tightness() {
        return 0;
    }

    /**
     * Determines whether armor stands should copy the breast settings of the player equipping this chestplate onto it.
     *
     * <p>If this returns {@code true}, the equipping player's breast settings will also be rendered when this armor piece is equipped onto an armor stand.</p>
     *
     * <p>This is designed for armor types that are metallic in nature, and not armor types that would (realistically) be flexible enough to accommodate for a player's
     * breasts on their own (such as Leather and Chain).</p>
     *
     * @return {@code true} to copy the equipping player's breast settings onto this armor type when equipped onto armor stands, and render the relevant breast settings
     * on the armor stand.
     *
     * @implNote Defaults to returning {@code true} if this armor {@link #coversBreasts() covers the breasts} (and {@link #alwaysHidesBreasts() doesn't hide them}), and
     * {@link #physicsResistance() has complete physics resistance}.
     * @see com.wildfire.main.entitydata.BreastDataComponent
     * @since 3.2.0
     */
    default boolean armorStandsCopySettings() {
        return !alwaysHidesBreasts() && coversBreasts() && physicsResistance() == 1F;
    }

    /**
     * Overrides certain values when this armor piece is being rendered
     *
     * @return The relevant {@link IBreastArmorTexture}
     *
     * @implNote Defaults to {@link IBreastArmorTexture#DEFAULT}
     *
     * @see IBreastArmorTexture
     *
     * @since 4.0.0
     */
    @NotNull
    default IBreastArmorTexture texture() {
        return IBreastArmorTexture.DEFAULT;
    }
}