import java.io.ByteArrayOutputStream

plugins {
    `java-library`
    id("com.github.johnrengelman.shadow")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))

repositories {
    mavenCentral()
}

var adventureVersion = "4.17.0"

dependencies {
    implementation(project(":propack-api"))
    implementation(project(":propack-core"))
    compileOnly(files("../libs/flint-path-0.0.1.jar"))
    implementation(files("../libs/bestvecs-0.0.1.jar"))

    compileOnly("org.slf4j:slf4j-api:2.0.7")

    compileOnly("net.kyori:adventure-api:$adventureVersion")
    compileOnly("net.kyori:adventure-text-minimessage:$adventureVersion")
    compileOnly("net.kyori:adventure-text-serializer-plain:$adventureVersion")
    compileOnly("net.kyori:adventure-text-serializer-legacy:$adventureVersion")
    compileOnly("net.kyori:adventure-text-serializer-gson:$adventureVersion")

    compileOnly("com.google.code.gson:gson:2.10.1")
    compileOnly("com.google.guava:guava:33.0.0-jre")
    compileOnly("commons-io:commons-io:2.14.0")
    compileOnly("org.apache.commons:commons-lang3:3.12.0")

    compileOnly("org.jetbrains:annotations:24.1.0")
}

tasks.register("buildGo") {
    doLast {
        val goPath = ByteArrayOutputStream()
        exec {
            commandLine = if (System.getProperty("os.name").lowercase().contains("windows")) {
                listOf("where", "go") // Windows
            } else {
                listOf("which", "go") // Linux/macOS
            }
            standardOutput = goPath
        }

        val goExecutablePath = goPath.toString().trim()

        if (goExecutablePath.isEmpty()) {
            throw GradleException("Go executable not found. Please ensure Go is installed and available in your PATH.")
        }

        val architectures = arrayOf(
            "linux_386", "linux_amd64", "linux_arm64",
            "darwin_amd64", "darwin_arm64",
            "windows_amd64", "windows_arm64"
        )

        architectures.forEach { arch ->
            val os = arch.split("_")[0]
            val archName = arch.split("_")[1]
            val outputExtension = if (os == "windows") ".exe" else ""
            exec {
                workingDir = rootProject.file("propack-builder")
                environment("CGO_ENABLED", "0")
                environment("GOOS", os)
                environment("GOARCH", archName)
                commandLine(goExecutablePath, "build", "-ldflags", "-s -w", "-o", "bin/$arch$outputExtension", "cmd/propack-builder/main.go")
            }
        }
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks {
    processResources {
        filteringCharset = "UTF-8"
    }

    shadowJar {
        dependsOn(project.project(":propack-core").tasks.named("assemble"))
        dependsOn("buildGo")

        archiveClassifier.set("")

        from(rootProject.file("propack-builder/bin")) {
            include("*")
            into("propack-builder")
        }
    }

    assemble {
        dependsOn("shadowJar")
    }
}
