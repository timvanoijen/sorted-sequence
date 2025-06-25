plugins {
    kotlin("jvm") version "2.1.20"
    `maven-publish`
    id("org.jlleitschuh.gradle.ktlint") version "11.3.1"
    id("org.jetbrains.dokka") version "2.0.0"
    signing
}

group = "nl.timocode.kotlin-utils"
version = "1.0-SNAPSHOT"

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

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name.set("Kotlin Sorted Sequences")
                description.set("Kotlin library to work with sorted sequences. This library provides a number of utility functions to make working with sorted sequences easier, such as grouping, merging, and joining sorted sequences.")
                url.set("https://github.com/timvanoijen/sorted-sequence")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        id.set("timvanoijen")
                        name.set("Tim van Oijen")
                        email.set("timvanoijen@gmail.com")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/timvanoijen/sorted-sequence.git")
                    developerConnection.set("scm:git:ssh://github.com/timvanoijen/sorted-sequence.git")
                    url.set("https://github.com/timvanoijen/sorted-sequence")
                }
            }
        }
    }

    repositories {
        maven {
            name = "OSSRH"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = System.getenv("USERNAME") ?: ""
                password = System.getenv("PASSWORD") ?: ""
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}

