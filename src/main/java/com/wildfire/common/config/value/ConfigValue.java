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

package com.wildfire.common.config.value;

import com.wildfire.common.config.validator.ConfigValidator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public interface ConfigValue<TYPE> extends Supplier<TYPE>, Consumer<TYPE> {

    UnaryOperator<Boolean> TOGGLE = value -> !value;

    ConfigValidator<TYPE> validator();

    /// {@return `true` if the value was changed to the default value, `false` if it was already at the default value}
    boolean reset();

    default boolean update(UnaryOperator<TYPE> transformer) {
        return update(transformer.apply(get()));
    }

    boolean update(TYPE newValue);

    @Override
    default void accept(TYPE newValue) {
        update(newValue);
    }
}
