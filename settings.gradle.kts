rootProject.name = "CobblemonRaidDens"

pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev/")
        maven("https://maven.minecraftforge.net/")
        gradlePluginPortal()
    }
}

listOf(
    "common",
    "neoforge",
    "fabric",
    "fabricdatagen"
).forEach { include(it)}