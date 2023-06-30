import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "com.cobb"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    jvm {
        jvmToolchain(11)
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(files(
                    "libs/jnativehook-2.2.2.jar",
                    "libs/aip-java-sdk.4.16.16.jar",
                    "libs/json-20160810.jar",
                    "slf4j-api-1.7.25.jar",
                    "slf4j-simple-1.7.25.jar",
                ))
            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "voice-monitor"
            packageVersion = "1.0.0"
        }
    }
}
