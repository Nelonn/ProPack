plugins {
    `java-library`
    id("io.papermc.paperweight.userdev")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.REOBF_PRODUCTION

dependencies {
    // https://repo.papermc.io/service/rest/repository/browse/maven-public/io/papermc/paper/dev-bundle/
    paperweight.paperDevBundle("1.20.6-R0.1-20241030.191541-126")
    compileOnly(files("../../../libs/flint-path-0.0.1.jar"))
    compileOnly(project(":propack-api"))
    compileOnly(project(":propack-core"))
    compileOnly(project(":propack-bukkit:base"))
}

tasks.withType<JavaCompile> {
    options.release.set(21)
    options.encoding = "UTF-8"
}

tasks.named("assemble") {
    dependsOn("reobfJar")
}
