import org.cadixdev.gradle.licenser.LicenseExtension

plugins {
    `java-library`
    id("org.cadixdev.licenser")
    id("io.papermc.paperweight.userdev")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

dependencies {
    // https://papermc.io/repo/service/rest/repository/browse/maven-public/io/papermc/paper/dev-bundle/
    paperweight.paperDevBundle("1.18.2-R0.1-20220920.010157-167")
    "compileOnly"(files("../../../libs/lib-flint-path-0.0.1.jar"))
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
