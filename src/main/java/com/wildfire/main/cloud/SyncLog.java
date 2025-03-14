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

import com.wildfire.main.config.GeneralClientConfig;
import com.wildfire.main.config.enums.SyncVerbosity;
import com.wildfire.main.text.ILangEntry;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

public final class SyncLog {
	public static final List<Entry> SYNC_LOG = new ArrayList<>();

	public static int verbosity() {
		return GeneralClientConfig.INSTANCE.syncLogVerbosity.get().ordinal();
	}

	public static void add(ILangEntry langEntry, SyncVerbosity verbosity) {
		if (verbosity() >= verbosity.ordinal()) {
			add(langEntry);
		}
	}

	public static void add(ILangEntry langEntry) {
		SYNC_LOG.add(new Entry(langEntry.translate(), Instant.now()));
		if(SYNC_LOG.size() > 6) {
			SYNC_LOG.removeFirst();
		}
	}

	public record Entry(Component text, Instant timestamp) {
		public static final int NEW_COLOR = 0x00FF00;
		public static final int OLD_COLOR = 0x34A100;

		public int color() {
			long secondsPassed = Instant.now().getEpochSecond() - timestamp.getEpochSecond();
			float delta = Mth.clamp(secondsPassed / 60f, 0f, 1f);
			return FastColor.ARGB32.lerp(delta, NEW_COLOR, OLD_COLOR);
		}
	}
}
