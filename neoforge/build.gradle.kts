plugins {
    id("multiloader-loader")
    id("net.neoforged.moddev")
    id("me.modmuss50.mod-publish-plugin")
}

val dataSourceSet = sourceSets.named("datagen")

// These source-sets only exist for the sake of assembling a dependency graph
// for IntelliJ. Each of these will become an IntelliJ module when the project is imported,
// and will be set as the "main module" for the IntelliJ runs. A compile dependency
// on the other source sets is enough, since runtime classpath is managed by MDG anyway.
val runMain = sourceSets.register("runMain") {
    val main = sourceSets.main.get()
    compileClasspath += main.output
    runtimeClasspath += main.runtimeClasspath
}
val runData = sourceSets.register("runData") {
    val mainRun = runMain.get()
    val data = dataSourceSet.get()
    compileClasspath += mainRun.compileClasspath + data.output
    runtimeClasspath += mainRun.runtimeClasspath + data.runtimeClasspath
}

neoForge {
    enable {
        version = sc.properties["dependencies.neo_version"]
        enabledSourceSets = sourceSets // All source sets use Minecraft code
    }

    val common = sc.tree["common"]!!.project
    val at = common.file("src/main/resources/META-INF/accesstransformer.cfg")
    //Use the corresponding common project's StoneCutter to process the path, so that it points at the identical absolute path
    // and MDG is able to more reliably re-use the recompiled minecraft
    val commonProject = sc.node.sibling("common")!!.project
    accessTransformers.from(commonProject.sc.process(at, "build/dev.at").absolutePath)
    validateAccessTransformers = true

    val mod = mods.register(sc.properties["mod_id"]) {
        modSourceSets.add(sourceSets.main)
    }
    val dataMod = mods.register("${sc.properties["mod_id"] as String}_data") {
        modSourceSets.set(mod.get().modSourceSets)
        modSourceSets.add(dataSourceSet)
    }

    runs {
        configureEach {
            systemProperty("neoforge.enabledGameTestNamespaces", sc.properties["mod_id"])
            ideName = "NeoForge ${name.replaceFirstChar(Char::titlecase)} ($path)"
            gameDirectory = file("../../run")

            val forceAnsi = providers.gradleProperty("forge_force_ansi")
            if (forceAnsi.isPresent) {
                // Force ANSI if declared as a Gradle property, as the auto detection
                // doesn't detect IntelliJ properly or Eclipse's ANSI console plugin.
                systemProperties.put("terminal.ansi", forceAnsi.get())
            }

            sourceSet = runMain
            loadedMods.add(mod.get())
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
            loadedMods.empty()
            loadedMods.add(dataMod.get())
            sourceSet = runData

            programArguments.addAll("--all", "--output", file("src/generated/resources").absolutePath,
                "--mod", sc.properties["mod_id"], "--existing", file("src/main/resources").absolutePath,
                "--existing", common.file("src/main/resources").absolutePath
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

rootProject.tasks.named("runData").configure {
    dependsOn(tasks.named("runData"))
}

sourceSets.configureEach {
    configurations.named(getTaskName(null, "jarJar")) {
        attributes {
            attribute(loaderAttribute, loader)
        }
    }
}

publishMods {
    dryRun = performDryRun
    modrinth {
        from(modrinthOps, basePublishingOps)
        additionalFile(tasks.sourcesJar.flatMap { it.archiveFile }) { type.set(SOURCES_JAR) }
        additionalFile(tasks.javadocJar.flatMap { it.archiveFile }) { type.set(JAVADOC_JAR) }
    }
    curseforge {
        from(cfOps, basePublishingOps)
    }
}
