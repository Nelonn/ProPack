plugins {
    `java-library`
    id("com.github.johnrengelman.shadow")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

repositories {
    // All repos inherited from :propack-bukkit:base

    mavenCentral()
    maven("https://repo.eclipse.org/content/groups/releases/") // JGit
    maven("https://repo.codemc.io/repository/maven-public/") // NBTAPI

    maven("https://repo.dmulloy2.net/repository/public/") // ProtocolLib
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // PlaceholderAPI
    maven("https://jitpack.io") // Oraxen

    maven("https://hub.spigotmc.org/nexus/content/groups/public") // SpigotAPI
    maven("https://papermc.io/repo/repository/maven-public/") // PaperAPI

}

dependencies {
    implementation(project(":propack-bukkit:base"))
    project.project(":propack-bukkit:adapters").subprojects.forEach {
        implementation(project(it.path))
    }
}

var shadedPackage = "me.nelonn.propack.shaded"

tasks {
    shadowJar {
        exclude("GradleStart**")
        exclude(".cache")
        exclude("LICENSE*")
        exclude("META-INF/maven/**")
        exclude("about.html")
        exclude("plugin.properties") // JGit
        relocate("me.nelonn.bestvecs", "$shadedPackage.bestvecs")
        relocate("me.nelonn.commandlib", "$shadedPackage.commandlib")
        relocate("me.nelonn.configlib", "$shadedPackage.configlib")
        relocate("com.github.retrooper.packetevents", "$shadedPackage.packetevents.api")
        relocate("io.github.retrooper.packetevents", "$shadedPackage.packetevents.impl")
        //relocate("com.google.gson", "$shadedPackage.gson")
        relocate("org.apache.commons", "$shadedPackage.commons")
        archiveClassifier.set("")
    }

    assemble {
        dependsOn("shadowJar")
    }
}
