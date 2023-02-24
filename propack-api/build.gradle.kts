import org.cadixdev.gradle.licenser.LicenseExtension

plugins {
    `java-library`
    id("org.cadixdev.licenser")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))

repositories {
    mavenCentral()
}

dependencies {
    "compileOnly"(files("../libs/flint-path-0.0.1.jar"))
    "compileOnly"("net.kyori:adventure-api:4.12.0")
    "compileOnly"("org.jetbrains:annotations:23.1.0")
}

java {
    withSourcesJar()
    withJavadocJar()
}

configure<LicenseExtension> {
    header(rootProject.file("HEADER.txt"))
    include("**/*.java")
}
