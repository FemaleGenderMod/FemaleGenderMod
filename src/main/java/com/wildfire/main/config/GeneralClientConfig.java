/*
	Wildfire's Female Gender Mod is a female gender mod created for Minecraft.
	Copyright (C) 2023 WildfireRomeo

	This program is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License as published by the Free Software Foundation; either
	version 3 of the License, or (at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
	Lesser General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package com.wildfire.main.config;

import com.wildfire.main.WildfireGender;
import com.wildfire.main.config.enums.ShowPlayerListMode;
import com.wildfire.main.config.enums.SyncVerbosity;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import net.neoforged.neoforge.common.ModConfigSpec.EnumValue;

public class GeneralClientConfig {

	private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
	public static final GeneralClientConfig INSTANCE = new GeneralClientConfig();

	public final ModConfigSpec configSpec;

	public final BooleanValue disableRendering;
	public final BooleanValue disableSoundReplacement;

	public final BooleanValue firstTimeLoad;
	public final BooleanValue cloudSync;
	public final BooleanValue syncPlayerData;
	public final ConfigValue<String> cloudServer;
	public final EnumValue<SyncVerbosity> syncLogVerbosity;
	public final EnumValue<ShowPlayerListMode> alwaysShowList;
	public final BooleanValue armorStat;

	private GeneralClientConfig() {
		ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
		disableRendering = builder.comment("Global override to disable all rendering related to the mod (including in gender menus)")
			  .define("disableRendering", false);
		disableSoundReplacement = builder.comment("Global override to disable replacing sounds of players with female variants")
			  .define("disableSoundReplacement", false);

		//TODO - 1.21: Comments and translations for these
		firstTimeLoad = builder.define("firstTimeLoad", true);
		cloudSync = builder.define("cloudSync", false);
		syncPlayerData = builder.define("syncPlayerData", false);
		cloudServer = builder.define("cloudServer", "");
		syncLogVerbosity = builder.defineEnum("syncLogVerbosity", SyncVerbosity.DEFAULT);
		alwaysShowList = builder.defineEnum("alwaysShowList", ShowPlayerListMode.MOD_UI_ONLY);
		armorStat = builder.define("armorStat", true);

		configSpec = builder.build();
	}

	public void save() {
		//TODO - 1.21: Implement/Re-evaluate if we can just call save on the ConfigValues and whether some guis are meant to only save on close
		EXECUTOR.submit(new ConfigSaver(configSpec));
	}

	private static class ConfigSaver implements Runnable {

		private final ModConfigSpec configSpec;
		private int retries = 0;

		private ConfigSaver(ModConfigSpec configSpec) {
			this.configSpec = configSpec;
		}

		@Override
		public void run() {
			try {
				configSpec.save();
			} catch (Exception e) {
				WildfireGender.LOGGER.error("Failed to save config", e);
				if (retries++ < 3) {
					EXECUTOR.submit(this);
				} else {
					WildfireGender.LOGGER.error("Giving up");
				}
			}
		}
	}
}