// Maven の親 pom(pom.xml)と同じ設定を、Gradle に写したもの

allprojects {
    group = "com.acrobot.chestshop"
    version = providers.gradleProperty("chestshopVersion").get()

    repositories {
        mavenCentral()
        maven("https://hub.spigotmc.org/nexus/content/groups/public/")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://maven.enginehub.org/repo/")
        maven("https://repo.codemc.org/repository/maven-public/")
        maven("https://ci.ender.zone/plugin/repository/everything/")
        maven("https://nexus.hc.to/content/repositories/pub_releases/")
        maven("https://repo.minebench.de/")
        // install_dependency_to_repo.sh で入れた、手元にしかない依存(repo/)
        maven(uri("$rootDir/repo/"))
        maven("https://jitpack.io")
        maven("https://ci.nyaacat.com/maven/")
        maven("https://raw.githubusercontent.com/FabioZumbi12/RedProtect/mvn-repo/")
    }
}

subprojects {
    apply(plugin = "java-library")

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        // Java 8 向けにビルドする(Maven の maven.compiler.source / target = 8 と同じ)
        options.release = 8
    }
}
