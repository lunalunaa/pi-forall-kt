import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.20"
    application
}

group = "me.luna"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    val arrowVersion = "0.13.2"
    implementation("io.arrow-kt:arrow-core:$arrowVersion")
    implementation("com.github.h0tk3y.betterParse:better-parse:0.4.2")
    testImplementation(kotlin("test"))
    implementation(group = "org.jline", name = "jline", version = "3.1.3")

}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClassName = "MainKt"
}