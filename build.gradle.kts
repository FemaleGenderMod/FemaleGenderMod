plugins {
    // plugin versions are defined in stonecutter.gradle.kts
    id("net.fabricmc.fabric-loom")
}

version = "${project.property("mod_version")}+${sc.current.project}"
group = project.property("maven_group") as String

base {
    archivesName = project.property("archives_base_name") as String
}

repositories {
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1") { name = "DevAuth" }
    maven("https://maven.terraformersmc.com/") { name = "Terraformers" }
}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
    implementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")

    implementation(platform("net.fabricmc.fabric-api:fabric-api-bom:${property("dependencies.fabric")}"))
    implementation("net.fabricmc.fabric-api:fabric-networking-api-v1")
    implementation("net.fabricmc.fabric-api:fabric-key-mapping-api-v1")
    implementation("net.fabricmc.fabric-api:fabric-lifecycle-events-v1")
    implementation("net.fabricmc.fabric-api:fabric-command-api-v2")
    implementation("net.fabricmc.fabric-api:fabric-rendering-v1")
    implementation("net.fabricmc.fabric-api:fabric-resource-loader-v1")
    runtimeOnly("net.fabricmc.fabric-api:fabric-registry-sync-v0")

    // Allow logging into an actual Minecraft account in a dev env
    // See https://github.com/DJtheRedstoner/DevAuth
    localRuntime("me.djtheredstoner:DevAuth-fabric:1.2.2")

    val modmenu: String = sc.properties["dependencies.modmenu"]
    compileOnly("com.terraformersmc:modmenu:${modmenu}")
    if(sc.properties["debug.load_modmenu"]) {
        localRuntime("com.terraformersmc:modmenu:${modmenu}")
        localRuntime("net.fabricmc.fabric-api:fabric-screen-api-v1")
    }
}

tasks.processResources {
    val props = mapOf(
        "version" to project.version as String,
        "minecraft_dependency" to sc.properties["dependencies.minecraft"],
    )

    inputs.properties(props)
    filesMatching("fabric.mod.json") {
        expand(props)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 25
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

loom {
    decompilers {
        named("vineflower") {
            options.put("mark-corresponding-synthetics", "1")
        }
    }

    runConfigs.configureEach {
        ideConfigGenerated(stonecutter.current.isActive)
        // by default loom will use versions/*/run for the run dir, so instead tell it to use the
        // run dir in the project root directory
        runDir = "../../run"
    }

    accessWidenerPath = sc.process(rootProject.file("src/main/resources/wildfire_gender.accesswidener"), "build/dev.aw")
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${project.property("archives_base_name")}" }
    }
}
