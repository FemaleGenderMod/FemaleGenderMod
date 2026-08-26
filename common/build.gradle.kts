plugins {
    id("multiloader-common")
    id("net.neoforged.moddev")
}

neoForge {
    enable {
        neoFormVersion = "${sc.current.version}-${sc.properties["dependencies.neoform_timestamp"] as String}"
        enabledSourceSets = sourceSets // All source sets use Minecraft code
    }

    val at = sc.branch.project.file("src/main/resources/META-INF/accesstransformer.cfg")
    accessTransformers.from(sc.process(at, "build/dev.at").absolutePath)
    validateAccessTransformers = true
}

dependencies {
    // Fabric and NeoForge both bundle Fabric Mixin, so it is safe to use it in common
    // If you need to update, check what version they are using to see what is compatible
    // https://github.com/neoforged/NeoForge/blob/26.2.x/gradle.properties#L37
    // https://github.com/FabricMC/fabric-loader/blob/master/gradle.properties#L12
    compileOnly("net.fabricmc:sponge-mixin:0.17.3+mixin.0.8.7")
    // Fabric and NeoForge both bundle MixinExtras, so it is safe to use it in common
    val mixinExtras = "io.github.llamalad7:mixinextras-common:0.5.4"
    compileOnly(mixinExtras)
    annotationProcessor(mixinExtras)
}

val commonJava by configurations.registering {
    isCanBeResolved = false
    isCanBeConsumed = true
    outgoing.artifacts(sourceSets.main.map { it.java.sourceDirectories.files })

}

val commonResources by configurations.registering {
    isCanBeResolved = false
    isCanBeConsumed = true
    outgoing.artifacts(sourceSets.main.map { it.resources.sourceDirectories.files })
}

val commonDataJava by configurations.registering {
    extendsFrom(commonJava)
    isCanBeResolved = false
    isCanBeConsumed = true
    outgoing.artifacts(sourceSets.named("datagen").map { it.java.sourceDirectories.files })
}

tasks.named("createMinecraftArtifacts") {
    dependsOn("stonecutterGenerate")
}
