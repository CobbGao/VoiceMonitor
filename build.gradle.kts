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
        jvmToolchain(17)
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(files(
                    "libs/jnativehook-2.2.2.jar",

                    "libs/commons-codec-1.6.jar",
                    "libs/commons-io-2.4.jar",
                    "libs/commons-io-2.4_jb51.rar",
                    "libs/gson-2.3.1.jar",
                    "libs/netty-all-4.1.15.Final.jar",
                    "libs/okhttp-3.13.1.jar",
                    "libs/okio-1.15.0-20180330.135339-9.jar",
                ))
                implementation("com.baidu.aip:java-sdk:4.16.16")
            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Exe)
            packageName = "voice-monitor"
            packageVersion = "1.0.0"
        }
    }
}
