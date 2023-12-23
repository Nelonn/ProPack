plugins {
    `java-library`
    id("com.github.johnrengelman.shadow")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))

repositories {
    mavenCentral()
}

var adventureVersion = "4.15.0"

dependencies {
    "implementation"(project(":propack-api"))
    "implementation"(project(":propack-core"))
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

    "compileOnly"("org.jetbrains:annotations:23.1.0")
}
