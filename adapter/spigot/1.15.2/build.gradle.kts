plugins {
    `java-library`
}

dependencies {
    compileOnly(project(":plugin"))
    compileOnly("org.spigotmc:spigot-api:1.15.2-R0.1-SNAPSHOT")
}
