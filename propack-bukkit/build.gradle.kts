import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.papermc.paperweight.userdev.attribute.Obfuscation

plugins {
    `java-library`
    id("com.github.johnrengelman.shadow")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

val adapters = configurations.create("adapters") {
    description = "Adapters to include in the JAR"
    isCanBeConsumed = false
    isCanBeResolved = true
    shouldResolveConsistentlyWith(configurations["runtimeClasspath"])
    attributes {
        attribute(Obfuscation.OBFUSCATION_ATTRIBUTE,
            if ((project.findProperty("propack.obf.none") as String?).toBoolean()) {
                objects.named(Obfuscation.NONE)
            } else {
                objects.named(Obfuscation.OBFUSCATED)
            }
        )
    }
}

repositories {
    mavenCentral()
    maven("https://repo.eclipse.org/content/groups/releases/") // JGit
    maven("https://repo.codemc.io/repository/maven-public/") // NBTAPI

    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/") // PlaceholderAPI
    maven("https://jitpack.io") // Oraxen

    maven("https://hub.spigotmc.org/nexus/content/groups/public") // SpigotAPI
    maven("https://papermc.io/repo/repository/maven-public/") // PaperAPI
}

var adventureVersion = "4.15.0"

dependencies {
    "implementation"(project(":propack-api"))
    "implementation"(project(":propack-core"))
    "implementation"(project(":propack-builder-java"))
    "implementation"(files("../libs/commandlib-0.0.1.jar"))
    "implementation"(files("../libs/configlib-0.0.1.jar"))
    "compileOnly"(files("../libs/flint-path-0.0.1.jar"))

    "compileOnly"("org.spigotmc:spigot-api:1.17.1-R0.1-SNAPSHOT")
    //"compileOnly"("io.papermc.paper:paper-api:1.17.1-R0.1-SNAPSHOT")

    "compileOnly"("net.kyori:adventure-api:$adventureVersion")
    "compileOnly"("net.kyori:adventure-text-minimessage:$adventureVersion")
    "compileOnly"("net.kyori:adventure-text-serializer-plain:$adventureVersion")
    "compileOnly"("net.kyori:adventure-text-serializer-legacy:$adventureVersion")
    "compileOnly"("net.kyori:adventure-text-serializer-gson:$adventureVersion")
    "compileOnly"("net.kyori:adventure-platform-bukkit:4.3.2")

    "compileOnly"("me.clip:placeholderapi:2.11.2")
    //"compileOnly"("com.github.oraxen:oraxen:-SNAPSHOT")

    "compileOnly"("org.slf4j:slf4j-api:2.0.7")

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

var shadedPackage = "me.nelonn.propack.shaded"

tasks.named<ShadowJar>("shadowJar") {
    dependsOn(project.project(":propack-core").tasks.named("assemble"))
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
    exclude("plugin.properties") // JGit
    relocate("me.nelonn.bestvecs", "$shadedPackage.bestvecs")
    relocate("me.nelonn.commandlib", "$shadedPackage.commandlib")
    relocate("me.nelonn.configlib", "$shadedPackage.configlib")
    relocate("com.github.retrooper.packetevents", "$shadedPackage.packetevents.api")
    relocate("io.github.retrooper.packetevents", "$shadedPackage.packetevents.impl")
    //relocate("com.google.gson", "$shadedPackage.gson")
    relocate("org.apache.commons", "$shadedPackage.commons")
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
