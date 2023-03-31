import org.cadixdev.gradle.licenser.LicenseExtension
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.papermc.paperweight.userdev.attribute.Obfuscation

plugins {
    `java-library`
    id("org.cadixdev.licenser")
    id("com.github.johnrengelman.shadow")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

val adapters = configurations.create("adapters") {
    description = "Adapters to include in the JAR"
    isCanBeConsumed = false
    isCanBeResolved = true
    shouldResolveConsistentlyWith(configurations["runtimeClasspath"])
    attributes {
        attribute(Obfuscation.OBFUSCATION_ATTRIBUTE, objects.named(Obfuscation.OBFUSCATED))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.eclipse.org/content/groups/releases/") // JGit
    maven("https://repo.codemc.io/repository/maven-public/") // NBTAPI
    maven("https://repo.dmulloy2.net/repository/public/") // ProtocolLib
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // PlaceholderAPI
    maven("https://jitpack.io") // Oraxen

    maven("https://hub.spigotmc.org/nexus/content/groups/public")
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    "implementation"(project(":propack-api"))
    "implementation"(project(":propack-core")) {
        exclude(group = "net.kyori")
    }
    "compileOnly"(files("../libs/lib-flint-path-0.0.1.jar"))

    /*"compileOnly"("org.spigotmc:spigot-api:1.17-R0.1-SNAPSHOT") {
        exclude("junit", "junit")
    }*/
    "compileOnly"("io.papermc.paper:paper-api:1.17.1-R0.1-SNAPSHOT") {
        exclude("org.slf4j", "slf4j-api")
        exclude("junit", "junit")
        exclude(group = "net.kyori")
    }

    "implementation"("net.kyori:adventure-api:4.12.0")
    "implementation"("net.kyori:adventure-text-minimessage:4.12.0")
    "implementation"("net.kyori:adventure-text-serializer-plain:4.12.0")
    "implementation"("net.kyori:adventure-text-serializer-legacy:4.12.0")
    "implementation"("net.kyori:adventure-text-serializer-gson:4.12.0")
    "implementation"("net.kyori:adventure-platform-api:4.2.0")
    "implementation"("net.kyori:adventure-platform-bukkit:4.2.0")

    "compileOnly"("com.comphenix.protocol:ProtocolLib:4.7.0")
    "compileOnly"("me.clip:placeholderapi:2.11.2")
    //"compileOnly"("com.github.oraxen:oraxen:-SNAPSHOT")

    "implementation"("org.eclipse.jgit:org.eclipse.jgit:6.4.0.202211300538-r")
    "implementation"("commons-io:commons-io:2.11.0")
    "implementation"("org.apache.commons:commons-lang3:3.12.0")

    "compileOnly"("org.jetbrains:annotations:23.1.0")

    project.project(":propack-bukkit:adapters").subprojects.forEach {
        "adapters"(project(it.path))
    }
}

tasks.named<JavaCompile>("compileJava") {
    options.encoding = "UTF-8"
    dependsOn(tasks.named("clean"))
}

tasks.named<Copy>("processResources") {
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand("version" to version)
    }
}

tasks.named<ShadowJar>("shadowJar") {
    dependsOn(project.project(":propack-core").tasks.named("build"))
    dependsOn(project.project(":propack-bukkit:adapters").subprojects.map { it.tasks.named("assemble") })
    from(Callable {
        adapters.resolve()
            .map { f ->
                zipTree(f).matching {
                    exclude("META-INF/")
                }
            }
    })
    exclude("GradleStart**")
    exclude(".cache")
    exclude("LICENSE*")
    exclude("META-INF/maven/**")
    exclude("about.html")
    relocate("net.kyori", "me.nelonn.propack.shaded.kyori") {
        exclude("net.kyori.adventure.key.*")
    }
    exclude("net/kyori/adventure/key/**") // problems with different classes
    archiveClassifier.set("")
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
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

configure<LicenseExtension> {
    header(rootProject.file("HEADER.txt"))
    include("**/*.java")
}
