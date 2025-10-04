import org.apache.commons.lang3.SystemUtils

plugins {
    idea
    java
    id("gg.essential.loom") version "0.10.0.+"
    id("dev.architectury.architectury-pack200") version "0.1.3"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.teamdev.jxbrowser") version "1.1.0"
}

// Constants
val baseGroup: String by project
val mcVersion: String by project
val version: String by project
val modid: String by project
val mixinGroup = "$baseGroup.mixin"
val transformerFile = file("src/main/resources/accesstransformer.cfg")

// Offline mode properties
val offlineUsername: String by project
val offlineUuid: String by project

// Toolchains
java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

// Minecraft configuration
loom {
    log4jConfigs.from(file("log4j2.xml"))

    launchConfigs {
        "client" {
            property("mixin.debug", "true")
            arg("--tweakClass", "org.spongepowered.asm.launch.MixinTweaker")
            // Enable offline mode with custom username
            property("fabric.development", "true")
            arg("--username", offlineUsername)
            arg("--uuid", offlineUuid)
        }
    }

    runConfigs {
        "client" {
            if (SystemUtils.IS_OS_MAC_OSX) {
                vmArgs.remove("-XstartOnFirstThread")
            }
        }
        remove(getByName("server"))
    }

    forge {
        pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter())
        mixinConfig("mixins.$modid.json")
        if (transformerFile.exists()) {
            println("Installing access transformer")
            accessTransformer(transformerFile)
        }
    }

    mixin {
        defaultRefmapName.set("mixins.$modid.refmap.json")
    }
}

sourceSets.main {
    output.setResourcesDir(sourceSets.main.flatMap { it.java.classesDirectory })
}

// Dependencies
repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/maven/")
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
}

val shadowImpl: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    minecraft("com.mojang:minecraft:1.8.9")
    mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")

    shadowImpl("org.spongepowered:mixin:0.7.11-SNAPSHOT") {
        isTransitive = false
    }
    annotationProcessor("org.spongepowered:mixin:0.8.5-SNAPSHOT")

    // runtimeOnly("me.djtheredstoner:DevAuth-forge-legacy:1.2.1") // Disabled for offline mode

    // Extra dependencies
    shadowImpl("org.nanohttpd:nanohttpd:2.3.1") { isTransitive = true }
    shadowImpl("org.nanohttpd:nanohttpd-websocket:2.3.1") { isTransitive = true }
    shadowImpl("org.nanohttpd:nanohttpd-webserver:2.3.1") { isTransitive = true }
    shadowImpl("com.google.code.gson:gson:2.10.1")
}

// Tasks
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<org.gradle.jvm.tasks.Jar> {
    archiveBaseName.set(modid)
    manifest.attributes.run {
        this["FMLCorePluginContainsFMLMod"] = "true"
        this["ForceLoadAsMod"] = "true"
        this["TweakClass"] = "org.spongepowered.asm.launch.MixinTweaker"
        this["MixinConfigs"] = "mixins.$modid.json"
        if (transformerFile.exists())
            this["FMLAT"] = "${modid}_at.cfg"
    }
}

tasks.processResources {
    inputs.property("version", version)
    inputs.property("mcversion", mcVersion)
    inputs.property("modid", modid)
    inputs.property("basePackage", baseGroup)

    filesMatching(listOf("mcmod.info", "mixins.$modid.json")) {
        expand(inputs.properties)
    }

    // Rename AT cfg into META-INF
    rename("accesstransformer.cfg", "META-INF/${modid}_at.cfg")
    rename("(.+_at.cfg)", "META-INF/$1")
}

tasks.jar {
    archiveClassifier.set("without-deps")
    destinationDirectory.set(layout.buildDirectory.dir("intermediates"))
}

tasks.shadowJar {
    dependencies {
        exclude(dependency("org.spongepowered:mixin:.*"))
        exclude("/META-INF/versions/21/**")
        exclude("/META-INF/versions/22/**")
    }

    destinationDirectory.set(layout.buildDirectory.dir("intermediates"))
    archiveClassifier.set("non-obfuscated-with-deps")
    configurations = listOf(shadowImpl)

    // Relocations
    relocate("com.fasterxml.jackson", "$baseGroup.deps.jackson")
    relocate("com.google.gson", "$baseGroup.deps.gson")
    relocate("okhttp3", "$baseGroup.deps.okhttp3")
    relocate("okio", "$baseGroup.deps.okio")
    relocate("org.slf4j", "$baseGroup.deps.slf4j")
    relocate("com.google.protobuf", "$baseGroup.deps.protobuf")
    relocate("org.jetbrains", "$baseGroup.deps.jetbrains")

    doLast {
        configurations.forEach {
            println("Copying dependencies into mod: ${it.files}")
        }
    }
}

val remapJar by tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
    archiveClassifier.set("")
    from(tasks.shadowJar)
    input.set(tasks.shadowJar.get().archiveFile)
    destinationDirectory.set(layout.buildDirectory.dir("libs"))
}

tasks.assemble.get().dependsOn(tasks.remapJar)

tasks.clean {
    delete(layout.buildDirectory.dir("intermediates"))
    delete(layout.buildDirectory.dir("libs"))
}

tasks.withType<Copy> {
    exclude("**/*.temp.jar")
}
