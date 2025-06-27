import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm

plugins {
    kotlin("jvm") version "2.1.20"
    id("org.jlleitschuh.gradle.ktlint") version "11.3.1"
    id("org.jetbrains.dokka") version "2.0.0"
    id("com.vanniktech.maven.publish") version "0.33.0"
}

group = "io.github.timvanoijen.kotlin"
version = file("version").readText().trim()

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

    configure(
        KotlinJvm(
            javadocJar = JavadocJar.Dokka("dokkaHtml"),
            sourcesJar = true
        )
    )

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

tasks.register("incrementMinorVersion") {
    doLast {
        val versionFile = file("version")
        val currentVersion = versionFile.readText().trim()
        val versionParts = currentVersion.split(".")
        val major = versionParts[0]
        val minor = versionParts[1].toInt()
        val newVersion = "$major.${minor + 1}.0-SNAPSHOT"
        versionFile.writeText(newVersion)
    }
}
