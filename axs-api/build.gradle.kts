plugins {
    id("java")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:24.1.0")
    compileOnly("com.zaxxer:HikariCP:5.1.0")
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
