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

package com.wildfire.client.cloud;

import com.wildfire.common.WildfireLang;
import com.wildfire.client.config.ClientConfig;
import com.wildfire.common.config.enums.SyncVerbosity;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class SyncLog {
    public static final List<Entry> SYNC_LOG = new ArrayList<>();

    public static SyncVerbosity verbosity() {
        return ClientConfig.config().cloudSync().logVerbosity().get();
    }

    public static void add(WildfireLang langEntry, SyncVerbosity verbosity) {
        if (verbosity().ordinal() >= verbosity.ordinal()) {
            add(langEntry);
        }
    }

    public static void add(WildfireLang langEntry) {
        SYNC_LOG.add(new Entry(langEntry.translate(), Instant.now()));
        if(SYNC_LOG.size() > 6) {
            SYNC_LOG.removeFirst();
        }
    }

    public record Entry(Component text, Instant timestamp) {
        public static final int NEW_COLOR = CommonColors.GREEN;
        public static final int OLD_COLOR = 0xFF34A100;

        public int color() {
            long secondsPassed = Instant.now().getEpochSecond() - timestamp.getEpochSecond();
            float delta = Math.clamp(secondsPassed / 60F, 0, 1);
            return ARGB.linearLerp(delta, NEW_COLOR, OLD_COLOR);
        }
    }
}
