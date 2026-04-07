plugins {
    id("net.fabricmc.fabric-loom")
}

version = "fabric-${project.property("mod_version")}+${project.property("minecraft_version")}"
group = project.property("maven_group") as String

base {
    archivesName = project.property("archives_base_name") as String
}

repositories {
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1") { name = "DevAuth" }
    maven("https://maven.terraformersmc.com/") { name = "Terraformers" } // mod menu
}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    implementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")

    val apiVersion = project.property("fabric_version") as String
    implementation(fabricApi.module("fabric-networking-api-v1", apiVersion))
    implementation(fabricApi.module("fabric-key-mapping-api-v1", apiVersion))
    implementation(fabricApi.module("fabric-lifecycle-events-v1", apiVersion))
    implementation(fabricApi.module("fabric-command-api-v2", apiVersion))
    implementation(fabricApi.module("fabric-rendering-v1", apiVersion))
    implementation(fabricApi.module("fabric-resource-loader-v1", apiVersion))
    runtimeOnly(fabricApi.module("fabric-registry-sync-v0", apiVersion))

    // Allow logging into an actual Minecraft account in a dev env
    // See https://github.com/DJtheRedstoner/DevAuth
    localRuntime("me.djtheredstoner:DevAuth-fabric:1.2.2")

    // If you want to load Mod Menu in a development environment, change this to implementation
    // and uncomment the associated Fabric API module.
    compileOnly("com.terraformersmc:modmenu:${project.property("modmenu_version")}")
    //runtimeOnly(fabricApi.module("fabric-screen-api-v1", apiVersion))
}

tasks.processResources {
    val props = mapOf(
        "version" to project.property("mod_version") as String,
        "minecraft_version" to project.property("minecraft_version") as String,
        "minecraft_dependency" to project.property("minecraft_dependency") as String,
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

    accessWidenerPath = sc.process(rootProject.file("src/main/resources/wildfire_gender.accesswidener"), "build/dev.aw")
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${project.property("archives_base_name")}" }
    }
}
