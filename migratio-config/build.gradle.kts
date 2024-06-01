plugins {
    id("java-library")
}

group = "io.tofpu.migratio"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    api(project(":migratio-core"))
    implementation("com.google.guava:guava:33.2.1-jre")

    testImplementation("com.github.Carleslc.Simple-YAML:Simple-Yaml:1.8.4")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}