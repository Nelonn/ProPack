plugins {
    `java-library`
    id("io.papermc.paperweight.userdev")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

dependencies {
    // https://papermc.io/repo/service/rest/repository/browse/maven-public/io/papermc/paper/dev-bundle/
    paperweight.paperDevBundle("1.20.4-R0.1-20231223.015033-23")
    "compileOnly"(files("../../../libs/flint-path-0.0.1.jar"))
    "compileOnly"(project(":propack-api"))
    "compileOnly"(project(":propack-core"))
    "compileOnly"(project(":propack-bukkit"))
}

tasks.named("assemble") {
    dependsOn("reobfJar")
}
