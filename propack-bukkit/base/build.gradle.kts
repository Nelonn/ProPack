plugins {
    `java-library`
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

repositories {
    mavenCentral()
    maven("https://repo.eclipse.org/content/groups/releases/") // JGit
    maven("https://repo.codemc.io/repository/maven-public/") // NBTAPI

    maven("https://repo.dmulloy2.net/repository/public/") // ProtocolLib
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // PlaceholderAPI
    maven("https://jitpack.io") // Oraxen

    maven("https://hub.spigotmc.org/nexus/content/groups/public") // SpigotAPI
    maven("https://papermc.io/repo/repository/maven-public/") // PaperAPI
}

var adventureVersion = "4.17.0"

dependencies {
    implementation(project(":propack-api"))
    implementation(project(":propack-core"))
    implementation(project(":propack-builder-java"))
    implementation(files("../../libs/commandlib-0.0.1.jar"))
    implementation(files("../../libs/configlib-0.0.1.jar"))
    compileOnly(files("../../libs/flint-path-0.0.1.jar"))

    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")

    //compileOnly("net.kyori:adventure-api:$adventureVersion")
    //compileOnly("net.kyori:adventure-text-minimessage:$adventureVersion")
    //compileOnly("net.kyori:adventure-text-serializer-plain:$adventureVersion")
    //compileOnly("net.kyori:adventure-text-serializer-legacy:$adventureVersion")
    //compileOnly("net.kyori:adventure-text-serializer-gson:$adventureVersion")
    compileOnly("net.kyori:adventure-platform-bukkit:4.3.3")

    compileOnly("com.comphenix.protocol:ProtocolLib:5.3.0-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")
    //compileOnly("com.github.oraxen:oraxen:-SNAPSHOT")

    compileOnly("org.slf4j:slf4j-api:2.0.7")

    implementation("commons-io:commons-io:2.11.0")
    implementation("org.apache.commons:commons-lang3:3.12.0")

    compileOnly("org.jetbrains:annotations:24.1.0")
}

tasks.withType<JavaCompile> {
    options.release.set(21)
    options.encoding = "UTF-8"
}

tasks {
    compileJava {
        dependsOn("clean")
    }

    processResources {
        filteringCharset = "UTF-8"
        filesMatching("paper-plugin.yml") {
            expand("version" to version)
        }
    }
}

tasks.withType<Javadoc> {
    options {
        this as StandardJavadocDocletOptions
        addBooleanOption("Xdoclint:none", true)
        addStringOption("Xmaxwarns", "1")
    }
}

java {
    withSourcesJar()
    withJavadocJar()
}
