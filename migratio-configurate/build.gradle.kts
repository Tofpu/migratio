plugins {
    id("java-library")
}

group = "io.tofpu.migratio"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.spongepowered:configurate-core:4.1.2")
    implementation(project(":migratio-config"))

    testImplementation("org.spongepowered:configurate-yaml:4.1.2")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}