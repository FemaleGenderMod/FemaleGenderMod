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

package com.wildfire.common;

import java.nio.file.Path;
import java.util.Optional;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.i18n.MavenVersionTranslator;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import org.apache.maven.artifact.versioning.ArtifactVersion;

public class LoaderAgnosticsNeo implements LoaderAgnostics {

    @Override
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public boolean isDevelopmentEnv() {
        return !FMLEnvironment.isProduction();
    }

    @Override
    public String getModVersion(String modId) {
        Optional<? extends ModContainer> containerById = ModList.get().getModContainerById(modId);
        if (containerById.isEmpty()) {
            return "unknown";
        }
        ArtifactVersion version = containerById.get().getModInfo().getVersion();
        //Effectively the same as just calling version.toString(), but matches what the mod list screen does
        return MavenVersionTranslator.artifactVersionToString(version);
    }

    @Override
    public boolean onClient() {
        return FMLEnvironment.getDist().isClient();
    }
}
