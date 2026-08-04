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

package com.wildfire.mixins.accessors;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.K1;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Products.P11.class)//TODO: This is only really needed for 26.1 and older due to an older version of DFU, should we maybe instead try to just mixin the and methods?
public interface ProductsAccessor<F extends K1, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> {
    @Accessor("t1") App<F, T1> t1();
    @Accessor("t2") App<F, T2> t2();
    @Accessor("t3") App<F, T3> t3();
    @Accessor("t4") App<F, T4> t4();
    @Accessor("t5") App<F, T5> t5();
    @Accessor("t6") App<F, T6> t6();
    @Accessor("t7") App<F, T7> t7();
    @Accessor("t8") App<F, T8> t8();
    @Accessor("t9") App<F, T9> t9();
    @Accessor("t10") App<F, T10> t10();
    @Accessor("t11") App<F, T11> t11();
}
