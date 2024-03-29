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

listOf("1.17.1", "1.18.2", "1.19.4", "1.20.4").forEach {
    include("propack-bukkit:adapters:adapter-$it")
}

include("propack-bukkit:flint-path-lib")
