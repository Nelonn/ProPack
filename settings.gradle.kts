pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

rootProject.name = "propack"

include("propack-api")

include("propack-core")

include("propack-cli")

include("propack-bukkit")

listOf("1.17.1", "1.18.2", "1.19", "1.19.3", "1.19.4").forEach {
    include("propack-bukkit:adapters:adapter-$it")
}
