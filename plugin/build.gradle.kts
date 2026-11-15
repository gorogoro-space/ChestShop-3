import org.gradle.api.attributes.java.TargetJvmVersion

plugins {
    `java-library`
}

// 完成した jar に取り込むライブラリ(Maven の shade の includes と同じもの)
val shade by configurations.creating
// compileOnlyApi にして、アダプタからも取り込むライブラリ(adventure など)を使えるようにする(Maven の provided の推移的な解決と同じ)
configurations.compileOnlyApi { extendsFrom(shade) }
configurations.testImplementation { extendsFrom(configurations.compileOnly.get()) }

// PlotSquared など、Java 21 向けの jar にも対してコンパイルできるようにする(Maven は Java の版を見ずに解決していた)
configurations.matching { it.isCanBeResolved }.configureEach {
    attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 21)
}

dependencies {
    // Spigot API(既定は 1.13.2。Maven の spigot_version_latest プロファイルに相当するものは、-PspigotVersion=... で指定する)
    compileOnly("org.spigotmc:spigot-api:${providers.gradleProperty("spigotVersion").getOrElse("1.13.2-R0.1-SNAPSHOT")}")
    testImplementation(platform("org.junit:junit-bom:5.13.3"))

    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("org.junit.platform:junit-platform-launcher")
    // 取引履歴のテストで、実際の SQLite を使う(Spigot が本番で持っているものと同じ版)
    testRuntimeOnly("org.xerial:sqlite-jdbc:3.46.1.0") { isTransitive = false }
    testRuntimeOnly("org.slf4j:slf4j-api:2.0.16")
    shade("com.j256.ormlite:ormlite-jdbc:6.1")
    shade("de.themoep.utils:lang-bukkit:1.3-SNAPSHOT")
    shade("de.themoep:minedown-adventure:1.7.6-SNAPSHOT")
    shade("net.kyori:adventure-platform-bukkit:4.4.2-SNAPSHOT")
    shade("net.kyori:adventure-text-serializer-gson:4.26.1") {
        exclude(group = "com.google.code.gson")
    }
    compileOnly("org.apache.logging.log4j:log4j-core:2.17.2")
    shade("org.bstats:bstats-bukkit:3.0.1")
    compileOnly("net.milkbowl.vault:Vault:1.6.6") {
        isTransitive = false
    }
    compileOnly("com.herocraftonline.heroes:Heroes:1.5.5") {
        isTransitive = false
    }
    compileOnly("fr.xephi:authme:5.6.0-SNAPSHOT") {
        isTransitive = false
    }
    compileOnly("com.griefcraft.lwc:LWCX:2.2.5") {
        isTransitive = false
    }
    compileOnly("com.daemitus.deadbolt:deadbolt:2.2") {
        isTransitive = false
    }
    shade("javax.persistence:persistence-api:1.0")
    compileOnly("com.google.code.findbugs:jsr305:3.0.2") {
        isTransitive = false
    }
    compileOnly("com.sk89q.worldedit:worldedit-core:7.0.0-SNAPSHOT") {
        exclude(group = "org.yaml", module = "snakeyaml")
    }
    compileOnly("com.sk89q.worldguard:worldguard-legacy:7.0.0-SNAPSHOT") {
        exclude(group = "org.bukkit", module = "bukkit")
    }
    compileOnly("com.github.TechFortress:GriefPrevention:16.12.0") {
        isTransitive = false
    }
    compileOnly("com.github.jojodmo:ItemBridge:b0054538c1") {
        isTransitive = false
    }
    compileOnly("br.net.fabiozumbi12.RedProtect:RedProtect-Spigot:7.7.3") {
        isTransitive = false
    }
    compileOnly("br.net.fabiozumbi12.RedProtect:RedProtect-Core:7.7.3") {
        isTransitive = false
    }
    compileOnly("com.webkonsept.bukkit.simplechestlock:simplechestlock:1.2.1") {
        isTransitive = false
    }
    compileOnly("org.yi.acru.bukkit.lockette:lockette:1.8.14") {
        isTransitive = false
    }
    compileOnly("nl.rutgerkok:blocklocker:1.9") {
        isTransitive = false
    }
    compileOnly("com.bekvon.bukkit:residence:4.6.1.4") {
        isTransitive = false
    }
    compileOnly("de.themoep.showitem:api:1.6.3") {
        isTransitive = false
    }
    compileOnly("net.tnemc:Reserve:0.1.5.4") {
        isTransitive = false
    }
    compileOnly("me.crafter.mc:lockettepro:2.10-SNAPSHOT") {
        isTransitive = false
    }
    compileOnly("com.intellectualsites.plotsquared:plotsquared-bukkit:7.5.12")
}

tasks.test {
    useJUnitPlatform()
}
