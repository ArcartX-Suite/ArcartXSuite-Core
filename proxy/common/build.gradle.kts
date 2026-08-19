plugins {
    id("java")
}

dependencies {
    compileOnly("org.jetbrains:annotations:24.1.0")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("org.yaml:snakeyaml:2.2")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.xerial:sqlite-jdbc:3.46.0.0")
    implementation("com.mysql:mysql-connector-j:8.4.0")
}

tasks.compileJava {
    options.encoding = "UTF-8"
    options.release = 17
}

tasks.jar {
    archiveBaseName.set("ArcartXSuite-Proxy-Common")
    archiveClassifier.set("")
    duplicatesStrategy = org.gradle.api.file.DuplicatesStrategy.INCLUDE
}

tasks.build {
    dependsOn(tasks.jar)
}
