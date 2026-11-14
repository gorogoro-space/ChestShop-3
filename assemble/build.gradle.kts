import java.time.Instant
import java.time.temporal.ChronoUnit
import java.time.format.DateTimeFormatter

plugins {
    id("com.gradleup.shadow") version "9.6.1"
}

// プラグイン本体と全アダプタ、取り込むライブラリを 1 つの jar(ChestShop.jar)にまとめる(Maven の assemble モジュールに相当)
evaluationDependsOn(":plugin")

val adapterProjects = rootProject.subprojects.filter { it.path.startsWith(":adapter-") }
// プラグイン本体の shade(取り込むライブラリ)を、ここで解決する
val shade by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

val buildType = System.getenv("BUILD_NUMBER")?.let { "jenkins" } ?: "manual"
val builtAt = DateTimeFormatter.ISO_INSTANT.format(Instant.now().truncatedTo(ChronoUnit.SECONDS))
val pluginVersion = "${project.version} " +
        (System.getenv("BUILD_NUMBER")?.let { "(build $it)" } ?: "(compiled at $builtAt)")

dependencies {
    implementation(project(":plugin"))
    shade(project(path = ":plugin", configuration = "shade"))
    adapterProjects.forEach { implementation(it) }
}

val processPluginResources = tasks.register<Copy>("processPluginResources") {
    from(project(":plugin").file("src/main/resources"))
    from(rootProject.file("LICENSE"))
    from(rootProject.file("README.md"))
    from(rootProject.file("SECURITY.md"))
    into(layout.buildDirectory.dir("generated-resources"))
    // ビルドごとに時刻が変わるので、毎回作り直す(Maven と同じ)
    inputs.property("pluginVersion", pluginVersion)
    // plugin.yml の \${bukkit.plugin.version} だけを置き換える
    filesMatching("plugin.yml") {
        filter { it.replace("\${bukkit.plugin.version}", pluginVersion) }
    }
}

tasks.shadowJar {
    archiveFileName = "ChestShop.jar"
    destinationDirectory = rootProject.layout.buildDirectory.dir("libs")

    configurations = listOf(project.configurations.runtimeClasspath.get(), shade)
    from(processPluginResources)

    dependencies {
        include(project(":plugin"))
        adapterProjects.forEach { include(project(it.path)) }
        include(dependency("de.themoep:.*"))
        include(dependency("de.themoep.utils:.*"))
        include(dependency("net.kyori:.*"))
        include(dependency("org.bstats:.*"))
        include(dependency("com.j256.ormlite:.*"))
        include(dependency("javax.persistence:.*"))
    }

    relocate("de.themoep.utils.lang", "com.Acrobot.ChestShop.Libs.Lang")
    relocate("de.themoep.minedown.adventure", "com.Acrobot.ChestShop.Libs.MineDown")
    relocate("net.kyori", "com.Acrobot.ChestShop.Libs.Kyori")
    relocate("org.bstats", "com.Acrobot.ChestShop.Metrics.BStats")
    relocate("net.gravitydevelopment.updater", "com.Acrobot.ChestShop.Updater")
    relocate("com.j256.ormlite", "com.Acrobot.ChestShop.Libs.ORMlite")
    relocate("javax.persistence", "com.Acrobot.ChestShop.Libs.javax.persistence")

    manifest {
        attributes(
            "Distribution-Type" to buildType,
            "Built-At" to builtAt,
            "Build-Jdk" to System.getProperty("java.runtime.version"),
            "paperweight-mappings-namespace" to "mojang"
        )
    }
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
}

// 通常の jar は使わない(完成品は shadowJar が作る ChestShop.jar)
tasks.jar {
    enabled = false
}
