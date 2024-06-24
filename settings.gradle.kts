pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

rootProject.name = "propack"

include("propack-api")

include("propack-core")

include("propack-builder-java")

include("propack-cli")

include("propack-bukkit")

listOf("1.20.4", "1.20.6", "1.21").forEach {
    include("propack-bukkit:adapters:adapter-$it")
}

include("propack-bukkit:flint-path-lib")
