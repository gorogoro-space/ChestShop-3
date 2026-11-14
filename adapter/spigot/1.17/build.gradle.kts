plugins {
    `java-library`
}

dependencies {
    compileOnly(project(":plugin"))
    compileOnly("org.spigotmc:spigot-api:1.17-R0.1-SNAPSHOT")
}
