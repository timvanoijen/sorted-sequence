plugins {
    kotlin("jvm") version "2.1.20"
    id("org.jlleitschuh.gradle.ktlint") version "11.3.1"
    id("org.jetbrains.dokka") version "2.0.0"
    id("com.vanniktech.maven.publish") version "0.33.0"
}

group = "io.github.timvanoijen.kotlin"
version = "0.0.2-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates(group.toString(), "sorted-sequence", version.toString())

    pom {
        name.set("Kotlin Sorted Seuences")
        description.set("A Kotlin library for sorted sequences.")
        inceptionYear.set("2025")
        url.set("https://github.com/timvanoijen/sorted-sequence/")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("timvanoijen")
                name.set("Tim van Oijen")
                url.set("https://github.com/timvanoijen/")
            }
        }
        scm {
            url.set("https://github.com/timvanoijen/sorted-sequence/")
            connection.set("scm:git:git://github.com/timvanoijen/sorted-sequence.git")
            developerConnection.set("scm:git:ssh://git@github.com/timvanoijen/sorted-sequence.git")
        }
    }
}
