import org.cadixdev.gradle.licenser.LicenseExtension
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    id("org.cadixdev.licenser")
    id("com.github.johnrengelman.shadow")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))

repositories {
    mavenCentral()
    maven("https://repo.eclipse.org/content/groups/releases/") // JGit
}

dependencies {
    "implementation"(project(":propack-api"))
    "implementation"(files("../libs/flint-path-0.0.1.jar"))
    "implementation"(files("../libs/vorbis-java-1.0.0-beta.jar"))

    "compileOnly"("org.eclipse.jgit:org.eclipse.jgit:6.4.0.202211300538-r")

    "compileOnly"("org.apache.logging.log4j:log4j-api:2.19.0")

    "compileOnly"("net.kyori:adventure-api:4.12.0")
    "compileOnly"("net.kyori:adventure-text-minimessage:4.12.0")
    "compileOnly"("net.kyori:adventure-text-serializer-plain:4.12.0")
    "compileOnly"("net.kyori:adventure-text-serializer-legacy:4.12.0")
    "compileOnly"("net.kyori:adventure-text-serializer-gson:4.12.0")

    "compileOnly"("com.google.code.gson:gson:2.10.1")
    "compileOnly"("com.google.guava:guava:31.1-jre")
    "compileOnly"("commons-io:commons-io:2.11.0")
    "compileOnly"("org.apache.commons:commons-lang3:3.12.0")

    "compileOnly"("org.jetbrains:annotations:23.1.0")
}

tasks.named<JavaCompile>("compileJava") {
    options.encoding = "UTF-8"
    dependsOn(tasks.named("clean"))
}

tasks.named<Copy>("processResources") {
    filteringCharset = "UTF-8"
}

tasks.named<ShadowJar>("shadowJar") {
    dependsOn(project.project(":propack-api").tasks.named("build"))
    relocate("org.xiph", "me.nelonn.propack.core.ogg")
    archiveClassifier.set("")
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}

java {
    withSourcesJar()
    withJavadocJar()
}

configure<LicenseExtension> {
    header(rootProject.file("HEADER.txt"))
    include("**/*.java")
}
