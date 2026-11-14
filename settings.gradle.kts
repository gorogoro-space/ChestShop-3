rootProject.name = "ChestShop"

include(":plugin")

// アダプタは、Minecraft(Spigot / Paper)のバージョンごとの差を吸収する。Maven のモジュール名(chestshop-spigot-1.20 など)に合わせる
val adapters = mapOf(
    ":adapter-paper-1.13.2" to "adapter/paper/1.13.2",
    ":adapter-paper-1.15.2" to "adapter/paper/1.15.2",
    ":adapter-spigot-1.14" to "adapter/spigot/1.14",
    ":adapter-spigot-1.15.2" to "adapter/spigot/1.15.2",
    ":adapter-spigot-1.17" to "adapter/spigot/1.17",
    ":adapter-spigot-1.20" to "adapter/spigot/1.20",
    ":adapter-spigot-1.20.5" to "adapter/spigot/1.20.5"
)
adapters.forEach { (name, dir) ->
    include(name)
    project(name).projectDir = file(dir)
}

include(":assemble")
