import org.gradle.kotlin.dsl.creating

plugins {
    id("multiloader-common")
    id("net.neoforged.moddev")
}

neoForge {
    neoFormVersion = commonMod.dep("neo_form_version")

    val at = project(":common").file("src/main/resources/META-INF/accesstransformer.cfg")
    accessTransformers.from(commonProject.sc.process(at, "build/dev.at").absolutePath)
    validateAccessTransformers = true
}

dependencies {
    // Fabric and NeoForge both bundle Fabric Mixin, so it is safe to use it in common
    // If you need to update, check what version they are using to see what is compatible
    // https://github.com/neoforged/NeoForge/blob/26.2.x/gradle.properties#L37
    // https://github.com/FabricMC/fabric-loader/blob/master/gradle.properties#L12
    compileOnly("net.fabricmc:sponge-mixin:0.17.3+mixin.0.8.7")
    // Fabric and NeoForge both bundle MixinExtras, so it is safe to use it in common
    //compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:0.5.3"))
}

val commonJava: Configuration by configurations.creating {
    isCanBeResolved = false
    isCanBeConsumed = true
}

val commonResources: Configuration by configurations.creating {
    isCanBeResolved = false
    isCanBeConsumed = true
}

tasks.named("createMinecraftArtifacts") {
    dependsOn("stonecutterGenerate")
}

artifacts {
    afterEvaluate {
        val mainSourceSet = sourceSets.main.get()

        mainSourceSet.java.sourceDirectories.files.forEach {
            add(commonJava.name, it)
        }

        mainSourceSet.resources.sourceDirectories.files.forEach {
            add(commonResources.name, it)
        }
    }
}

val loaderAttribute = Attribute.of("io.github.mcgradleconventions.loader", String::class.java)

listOf("apiElements", "runtimeElements", "sourcesElements", "javadocElements").forEach { variant ->
    configurations.named(variant) {
        attributes {
            attribute(loaderAttribute, "common")
        }
    }
}

sourceSets.configureEach {
    listOf(compileClasspathConfigurationName, runtimeClasspathConfigurationName).forEach { variant ->
        configurations.named(variant) {
            attributes {
                attribute(loaderAttribute, "common")
            }
        }
    }
}
