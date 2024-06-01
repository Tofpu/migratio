plugins {
    id("maven-publish")
}

subprojects {
    group = "io.tofpu.migratio"
    version = "1.0-SNAPSHOT"

    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    publishing {
        publications {
            create<MavenPublication>("maven") {
                artifactId = project.name

                from(components["java"])
            }
        }
    }
}
