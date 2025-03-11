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

include("propack-bukkit:base")

listOf("1.21.4").forEach {
    include("propack-bukkit:adapters:adapter-$it")
}
