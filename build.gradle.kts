plugins {
    id("base")
}

subprojects {
    repositories {
        mavenCentral()
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
}
