import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java-library")
    id("maven-publish")
    id("com.gradleup.shadow") version "9.4.0"
}

group = "dev.digitality"
description = "DigitalNPC"
version = "1.0.0"

java.toolchain {
    languageVersion = JavaLanguageVersion.of(11)
    vendor = JvmVendorSpec.ADOPTIUM
}

repositories {
    mavenCentral()

    maven {
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }

    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
    }

    maven {
        url = uri("https://repo.codemc.org/repository/maven-public/")
    }

    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")

    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("com.github.retrooper:packetevents-spigot:2.11.2")
    implementation("com.google.code.gson:gson:2.13.2")
    implementation("com.squareup.okhttp3:okhttp:5.3.0")

    compileOnly("com.github.decentsoftware-eu:decentholograms:2.9.9")

    compileOnly("org.projectlombok:lombok:1.18.44")
    annotationProcessor("org.projectlombok:lombok:1.18.44")
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    withType<ShadowJar> {
        minimize()

        relocate("com.github.retrooper", "dev.digitality.digitalnpc.api.packetevents")
        relocate("io.github.retrooper", "dev.digitality.digitalnpc.api.packetevents")
        relocate("net.kyori", "dev.digitality.digitalnpc.api.kyori")
        relocate("com.google.gson", "dev.digitality.digitalnpc.api.gson")

        relocate("okhttp3", "dev.digitality.digitalnpc.api.okhttp3")

        archiveFileName = "${project.name}.jar"
        archiveClassifier = null
    }

    register("sourceJar", Jar::class) {
        archiveClassifier = "sources"

        from(sourceSets.main.get().allSource)
    }
}

publishing {
    repositories {
        maven {
            url = uri("https://maven.digitality.dev/releases")

            credentials {
                username = (project.findProperty("digitalityRepo.username") ?: System.getenv("DIGITALITY_REPO_USERNAME")) as String?
                password = (project.findProperty("digitalityRepo.password") ?: System.getenv("DIGITALITY_REPO_PASSWORD")) as String?
            }

            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }

    publications {
        create<MavenPublication>("shadow") {
            groupId = project.group as String?
            artifactId = project.name.lowercase()
            version = project.version as String?

            artifact(tasks["sourceJar"])

            from(components["shadow"])
        }
    }
}
