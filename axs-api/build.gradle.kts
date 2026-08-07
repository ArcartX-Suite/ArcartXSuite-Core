plugins {
    id("java")
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://jitpack.io")
    maven("https://repo.rosewooddev.io/repository/public/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:24.1.0")
    compileOnly("com.zaxxer:HikariCP:5.1.0")
    compileOnly(files("../libs/Mythic-Dist-5.6.1-SNAPSHOT.jar"))
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("org.black_ixx:playerpoints:3.3.5")
    compileOnly(files("../libs/XConomy-Paper-2.26.3.jar"))
    compileOnly(files("../libs/Rondo-1.0.0-SNAPSHOT.jar"))
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")
}

tasks.compileJava {
    options.encoding = "UTF-8"
    options.release = 17
}

tasks.compileTestJava {
    options.encoding = "UTF-8"
    options.release = 17
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    archiveBaseName.set("axs-api")
}
