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

package com.wildfire.main.cloud;

import com.wildfire.main.WildfireLang;
import com.wildfire.main.config.ClientConfigHolder;
import com.wildfire.main.config.enums.SyncVerbosity;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class SyncLog {
    public static final List<Entry> SYNC_LOG = new ArrayList<>();

    public static int verbosity() {
        return ClientConfigHolder.syncVerbosity().ordinal();
    }

    public static void add(WildfireLang langEntry, SyncVerbosity verbosity) {
        add(langEntry.translate(), verbosity);
    }

    public static void add(Component text, SyncVerbosity verbosity) {
        if(verbosity() < verbosity.ordinal()) {
            return;
        }
        add(text);
    }

    public static void add(WildfireLang langEntry) {
        add(langEntry.translate());
    }

    public static void add(Component text) {
        SYNC_LOG.add(new Entry(text, Instant.now()));
        if(SYNC_LOG.size() > 6) {
            SYNC_LOG.removeFirst();
        }
    }

    public record Entry(Component text, Instant timestamp) {
        public static final int NEW_COLOR = CommonColors.GREEN;
        public static final int OLD_COLOR = 0xFF34A100;

        public int color() {
            long secondsPassed = Instant.now().getEpochSecond() - timestamp.getEpochSecond();
            float delta = Mth.clamp(secondsPassed / 60f, 0f, 1f);
            return ARGB.linearLerp(delta, NEW_COLOR, OLD_COLOR);
        }
    }
}
