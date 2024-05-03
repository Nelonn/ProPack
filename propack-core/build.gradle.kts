import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    id("com.github.johnrengelman.shadow")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))

repositories {
    mavenCentral()
    maven("https://repo.eclipse.org/content/groups/releases/") // JGit
}

var adventureVersion = "4.16.0"

dependencies {
    "implementation"(project(":propack-api"))
    "compileOnly"(files("../libs/flint-path-0.0.1.jar"))
    "implementation"(files("../libs/bestvecs-0.0.1.jar"))

    "compileOnly"("org.slf4j:slf4j-api:2.0.7")

    "compileOnly"("net.kyori:adventure-api:$adventureVersion")
    "compileOnly"("net.kyori:adventure-text-minimessage:$adventureVersion")
    "compileOnly"("net.kyori:adventure-text-serializer-plain:$adventureVersion")
    "compileOnly"("net.kyori:adventure-text-serializer-legacy:$adventureVersion")
    "compileOnly"("net.kyori:adventure-text-serializer-gson:$adventureVersion")

    "compileOnly"("com.google.code.gson:gson:2.10.1")
    "compileOnly"("com.google.guava:guava:33.0.0-jre")
    "compileOnly"("commons-io:commons-io:2.11.0")
    "compileOnly"("org.apache.commons:commons-lang3:3.12.0")

    "compileOnly"("org.jetbrains:annotations:24.1.0")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.named<Copy>("processResources") {
    filteringCharset = "UTF-8"
}

tasks.named<ShadowJar>("shadowJar") {
    dependsOn(project.project(":propack-api").tasks.named("build"))
    archiveClassifier.set("")
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}

tasks.withType<Javadoc> {
    options {
        this as StandardJavadocDocletOptions
        addBooleanOption("Xdoclint:none", true)
        addStringOption("Xmaxwarns", "1")
    }
}

java {
    withSourcesJar()
    withJavadocJar()
}
