import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    id("com.github.johnrengelman.shadow")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))

repositories {
    mavenCentral()
}

var adventureVersion = "4.17.0"

dependencies {
    implementation(project(":propack-api"))
    implementation(project(":propack-core"))
    implementation(project(":propack-builder-java"))
    implementation(files("../libs/flint-path-0.0.1.jar"))

    // Logging
    implementation("org.slf4j:slf4j-api:2.0.7")
    implementation("ch.qos.logback:logback-classic:1.4.12")
    // Log4J Support
    implementation("org.apache.logging.log4j:log4j-api:2.19.0")
    implementation("org.apache.logging.log4j:log4j-to-slf4j:2.19.0")

    implementation("net.kyori:adventure-api:$adventureVersion")
    implementation("net.kyori:adventure-text-minimessage:$adventureVersion")
    implementation("net.kyori:adventure-text-serializer-plain:$adventureVersion")
    implementation("net.kyori:adventure-text-serializer-legacy:$adventureVersion")
    implementation("net.kyori:adventure-text-serializer-gson:$adventureVersion")

    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.google.guava:guava:33.0.0-jre")
    implementation("commons-io:commons-io:2.14.0")
    implementation("org.apache.commons:commons-lang3:3.12.0")

    implementation("org.jetbrains:annotations:24.1.0")

    implementation("info.picocli:picocli:4.7.0")
    annotationProcessor("info.picocli:picocli-codegen:4.7.0")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-Aproject=${project.group}/${project.name}")
}

tasks {
    processResources {
        filteringCharset = "UTF-8"
    }

    shadowJar {
        dependsOn(project.project(":propack-core").tasks.named("assemble"))
        dependsOn(project.project(":propack-builder-java").tasks.named("assemble"))
        manifest {
            attributes["Main-Class"] = "me.nelonn.propack.cli.CLI"
            attributes["Multi-Release"] = "true"
        }
        archiveClassifier.set("")
    }

    assemble {
        dependsOn("shadowJar")
    }
}
