import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    id("com.github.johnrengelman.shadow")
}

group = rootProject.group
version = "0.0.1"

java.toolchain.languageVersion.set(JavaLanguageVersion.of(16))

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/groups/public") // SpigotAPI
}

dependencies {
    "implementation"(files("../libs/flint-path-0.0.1.jar"))

    "compileOnly"("org.spigotmc:spigot-api:1.17.1-R0.1-SNAPSHOT")
}

tasks.named<JavaCompile>("compileJava") {
    options.encoding = "UTF-8"
}

tasks.named<Copy>("processResources") {
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand("version" to version)
    }
}

tasks.named<ShadowJar>("shadowJar") {
    archiveFileName.set("flint-path-$version.jar.library")
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}
