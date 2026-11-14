plugins {
    `java-library`
}

dependencies {
    compileOnly(project(":plugin"))
    compileOnly("org.spigotmc:spigot-api:1.20.5-R0.1-SNAPSHOT")
}
