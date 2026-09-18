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

package com.wildfire.neoforge.common;

import com.mojang.serialization.MapCodec;
import com.wildfire.api.WildfireAPI;
import com.wildfire.common.LoaderAgnostics;
import com.wildfire.common.entities.avatars.AvatarConfig;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.world.entity.decoration.Mannequin;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.i18n.MavenVersionTranslator;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

public class LoaderAgnosticsNeo implements LoaderAgnostics {
    @ApiStatus.Internal
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, WildfireAPI.MODID);

    private static final Supplier<AttachmentType<AvatarConfig>> AVATAR_ATTACHMENT = ATTACHMENT_TYPES.register("gender_data", () ->
        AttachmentType.builder(AvatarConfig::createDefault).serialize(MapCodec.assumeMapUnsafe(AvatarConfig.CODEC)).build());

    @Override
    public String name() {
        return "NeoForge";
    }

    @Override
    public String getLoaderVersion() {
        return getModVersion(NeoForgeMod.MOD_ID);
    }

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

    @Override
    public @Nullable AvatarConfig readFromMannequin(final Mannequin mannequin) {
        return mannequin.getData(AVATAR_ATTACHMENT);
    }

    @Override
    public void writeToMannequin(final Mannequin mannequin, final AvatarConfig config) {
        mannequin.setData(AVATAR_ATTACHMENT, config);
    }
}
