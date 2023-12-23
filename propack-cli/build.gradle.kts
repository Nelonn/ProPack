import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    id("com.github.johnrengelman.shadow")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))

repositories {
    mavenCentral()
}

dependencies {
    "implementation"(project(":propack-api"))
    "implementation"(project(":propack-core"))
    "implementation"(project(":propack-builder-java"))
    "implementation"(files("../libs/flint-path-0.0.1.jar"))

    "implementation"("org.eclipse.jgit:org.eclipse.jgit:6.4.0.202211300538-r")

    // Logging
    "implementation"("org.slf4j:slf4j-api:2.0.7")
    "implementation"("ch.qos.logback:logback-classic:1.4.11")
    // Log4J Support
    "implementation"("org.apache.logging.log4j:log4j-api:2.19.0")
    "implementation"("org.apache.logging.log4j:log4j-to-slf4j:2.19.0")

    "implementation"("net.kyori:adventure-api:4.12.0")
    "implementation"("net.kyori:adventure-text-minimessage:4.12.0")
    "implementation"("net.kyori:adventure-text-serializer-plain:4.12.0")
    "implementation"("net.kyori:adventure-text-serializer-legacy:4.12.0")
    "implementation"("net.kyori:adventure-text-serializer-gson:4.12.0")

    "implementation"("com.google.code.gson:gson:2.10.1")
    "implementation"("com.google.guava:guava:31.1-jre")
    "implementation"("commons-io:commons-io:2.11.0")
    "implementation"("org.apache.commons:commons-lang3:3.12.0")

    "implementation"("org.jetbrains:annotations:23.1.0")

    "implementation"("info.picocli:picocli:4.7.0")
    "annotationProcessor"("info.picocli:picocli-codegen:4.7.0")
}

tasks.named<JavaCompile>("compileJava") {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-Aproject=${project.group}/${project.name}")
    dependsOn(tasks.named("clean"))
}

tasks.named<Copy>("processResources") {
    filteringCharset = "UTF-8"
}

tasks.named<ShadowJar>("shadowJar") {
    dependsOn(project.project(":propack-core").tasks.named("build"))
    manifest {
        attributes["Main-Class"] = "me.nelonn.propack.cli.CLI"
        attributes["Multi-Release"] = "true"
    }
    archiveClassifier.set("")
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}
