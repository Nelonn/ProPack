import org.cadixdev.gradle.licenser.LicenseExtension

plugins {
    `java-library`
    id("org.cadixdev.licenser")
    id("io.papermc.paperweight.userdev")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

dependencies {
    // https://papermc.io/repo/service/rest/repository/browse/maven-public/io/papermc/paper/dev-bundle/
    paperDevBundle("1.17.1-R0.1-20220414.034903-210")
    "compileOnly"(files("../../../libs/flint-path-0.0.1.jar"))
    "compileOnly"(project(":propack-api"))
    "compileOnly"(project(":propack-core"))
    "compileOnly"(project(":propack-bukkit"))
}

tasks.named("assemble") {
    dependsOn("reobfJar")
}

configure<LicenseExtension> {
    header(rootProject.file("HEADER.txt"))
    include("**/*.java")
}
