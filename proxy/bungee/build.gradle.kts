plugins {
    id("java")
    id("com.gradleup.shadow")
}

dependencies {
    implementation(project(":proxy:common"))
    compileOnly("net.md-5:bungeecord-api:1.20-R0.2")
    compileOnly("org.jetbrains:annotations:24.1.0")
}

tasks.compileJava {
    options.encoding = "UTF-8"
    options.release = 17
}

tasks.jar {
    archiveBaseName.set("ArcartXSuite-Proxy-Bungee")
    archiveClassifier.set("")
    duplicatesStrategy = org.gradle.api.file.DuplicatesStrategy.INCLUDE
}

tasks.shadowJar {
    archiveBaseName.set("ArcartXSuite-Proxy-Bungee")
    archiveClassifier.set("")
    duplicatesStrategy = org.gradle.api.file.DuplicatesStrategy.INCLUDE

    // Common 自己的类保持原包名，不 relocate，确保 Bungee 代码中的 import 不受影响
    // 仅对第三方库做 relocate，避免与代理端已有依赖冲突
    relocate("com.google.gson", "xuanmo.arcartxsuite.proxy.internal.gson")
    relocate("com.zaxxer.hikari", "xuanmo.arcartxsuite.proxy.internal.hikari")
    relocate("org.sqlite", "xuanmo.arcartxsuite.proxy.internal.sqlite")
    relocate("com.mysql", "xuanmo.arcartxsuite.proxy.internal.mysql")

    // compileOnly 依赖（BungeeCord API、注解）由代理端提供，默认不打包
}

val publishProxyJar by tasks.registering {
    dependsOn(tasks.jar)
    dependsOn(tasks.shadowJar)
    val src = tasks.shadowJar.get().archiveFile.get().asFile
    val dst = rootProject.layout.buildDirectory.dir("ArcartXSuite/proxy").get().file(src.name).asFile
    inputs.file(src)
    outputs.file(dst)
    doLast {
        dst.parentFile.mkdirs()
        src.copyTo(dst, overwrite = true)
    }
}

tasks.build {
    dependsOn(publishProxyJar)
}
