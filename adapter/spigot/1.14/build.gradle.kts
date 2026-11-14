plugins {
    `java-library`
}

dependencies {
    compileOnly(project(":plugin"))
    compileOnly("org.spigotmc:spigot-api:1.14-R0.1-SNAPSHOT")
}
