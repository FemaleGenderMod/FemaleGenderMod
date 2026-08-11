plugins {
    id("multiloader-loader")
    id("net.neoforged.moddev")
}

sourceSets.main {
    resources.srcDir("src/generated/resources")
    //Exclude package info's from the neo project so that when merging it doesn't fail due to duplicate package info files
    java.exclude("**/package-info.java")
}

neoForge {
    version = commonMod.dep("neoforge")

    // Automatically enable AccessTransformers if present
    val at = project(":common").file("src/main/resources/META-INF/accesstransformer.cfg")
    if (at.exists()) {
        //TODO - Neo: Figure this out to make it target same as common
        accessTransformers.from(sc.process(at, "build/dev.at").absolutePath)
        validateAccessTransformers = true
    }

    mods {
        register(commonMod.id) {
            sourceSet(sourceSets.main.get())
        }
    }

    runs {
        configureEach {
            systemProperty("neoforge.enabledGameTestNamespaces", commonMod.id)
            ideName = "NeoForge ${name.replaceFirstChar(Char::titlecase)} ($path)"
            gameDirectory = file("../../run")

            val forceAnsi = providers.gradleProperty("forge_force_ansi")
            if (forceAnsi.isPresent) {
                // Force ANSI if declared as a Gradle property, as the auto detection
                // doesn't detect IntelliJ properly or Eclipse's ANSI console plugin.
                systemProperties.put("terminal.ansi", forceAnsi.get())
            }
        }

        register("client") {
            client()
            devLogin = providers.gradleProperty("mc_devlogin")
                    .map(String::toBoolean)
                    .getOrElse(false)
        }
        register("clientAlt") {
            client()
            programArguments.addAll("--username", "AltDev")
            devLogin = false
        }

        register("data") {
            clientData()

            //TODO: Do we need to pass common in as existing?
            programArguments.addAll("--all", "--output", file("src/generated/resources").absolutePath,
                "--mod", commonMod.id, "--existing", file("src/main/resources").absolutePath
            )
        }

        register("server") {
            server()
        }
    }
}

tasks.named("createMinecraftArtifacts") {
    dependsOn("stonecutterGenerate")
}

val loaderAttribute = Attribute.of("io.github.mcgradleconventions.loader", String::class.java)

listOf(
    "apiElements",
    "runtimeElements",
    "sourcesElements",
    "javadocElements"
).forEach { variant ->
    configurations.named(variant) {
        attributes {
            attribute(loaderAttribute, "neoforge")
        }
    }
}

sourceSets.configureEach {
    listOf(
        compileClasspathConfigurationName,
        runtimeClasspathConfigurationName,
        getTaskName(null, "jarJar")
    ).forEach { variant ->
        configurations.named(variant) {
            attributes {
                attribute(loaderAttribute, "neoforge")
            }
        }
    }
}
