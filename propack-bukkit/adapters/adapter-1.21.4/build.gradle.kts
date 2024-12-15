plugins {
    `java-library`
    id("io.papermc.paperweight.userdev")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

dependencies {
    // https://papermc.io/repo/service/rest/repository/browse/maven-public/io/papermc/paper/dev-bundle/
    paperweight.paperDevBundle("1.21.4-R0.1-20241215.095037-18")
    compileOnly(files("../../../libs/flint-path-0.0.1.jar"))
    compileOnly(project(":propack-api"))
    compileOnly(project(":propack-core"))
    compileOnly(project(":propack-bukkit:base"))
}

tasks.withType<JavaCompile> {
    options.release.set(21)
    options.encoding = "UTF-8"
}
