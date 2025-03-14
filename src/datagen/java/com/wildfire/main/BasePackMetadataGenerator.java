package com.wildfire.main;

import com.wildfire.main.text.IHasTranslationKey;
import com.wildfire.main.text.TextComponentUtil;
import java.util.Optional;
import net.minecraft.DetectedVersion;
import net.minecraft.data.PackOutput;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.util.InclusiveRange;

/**
 * From <a href="https://github.com/mekanism/Mekanism/blob/1.21.x/src/datagen/main/java/mekanism/common/BasePackMetadataGenerator.java">Mekanism</a>
 */
public class BasePackMetadataGenerator extends PackMetadataGenerator {

    public BasePackMetadataGenerator(PackOutput output, IHasTranslationKey description) {
        super(output);
        int minVersion = Integer.MAX_VALUE;
        int maxVersion = 0;
        for (PackType packType : PackType.values()) {
            int version = DetectedVersion.BUILT_IN.getPackVersion(packType);
            maxVersion = Math.max(maxVersion, version);
            minVersion = Math.min(minVersion, version);
        }
        add(PackMetadataSection.TYPE, new PackMetadataSection(
              TextComponentUtil.build(description),
              maxVersion,
              Optional.of(new InclusiveRange<>(minVersion, maxVersion))
        ));
    }
}