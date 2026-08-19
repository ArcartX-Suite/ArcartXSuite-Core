plugins {
    id("base")
    id("com.gradleup.shadow") version "8.3.5" apply false
}

subprojects {
    repositories {
        mavenCentral()
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.md-5.net/repository/public/")
    }
}
