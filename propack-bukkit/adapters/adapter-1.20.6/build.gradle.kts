import buildlogic.getVersion
import buildlogic.stringyLibs

plugins {
    `java-library`
    id("io.papermc.paperweight.userdev")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

dependencies {
    // https://papermc.io/repo/service/rest/repository/browse/maven-public/io/papermc/paper/dev-bundle/
    paperweight.paperDevBundle("1.20.6-R0.1-20240617.120221-121")
    "compileOnly"(files("../../../libs/flint-path-0.0.1.jar"))
    "compileOnly"(project(":propack-api"))
    "compileOnly"(project(":propack-core"))
    "implementation"(project(":propack-bukkit"))
    constraints {
        "remapper"("net.fabricmc:tiny-remapper:[${stringyLibs.getVersion("minimumTinyRemapper")},)") {
            because("Need remapper to support Java 21")
        }
    }
}

tasks.withType<JavaCompile> {
    options.release.set(21)
    options.encoding = "UTF-8"
}

tasks.named("assemble") {
    dependsOn("reobfJar")
}
