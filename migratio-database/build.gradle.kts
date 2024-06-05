plugins {
    id("java")
}

group = "io.tofpu.migratio"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    api(project(":migratio-core"))
    implementation("org.slf4j:slf4j-api:2.0.13")
    testImplementation("ch.qos.logback:logback-core:1.3.14")
    testImplementation("ch.qos.logback:logback-classic:1.3.14")

    testImplementation("org.xerial:sqlite-jdbc:3.46.0.0")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}