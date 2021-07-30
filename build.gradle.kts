import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.21"
    application
}


group = "ru.mipt.npm.sky"
version = "1.0.2"

application {
    mainClass.set("ru.mipt.npm.reactor.MainKt")
}

repositories {
    mavenCentral()
}

dependencies {
    //implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
    implementation("org.apache.commons:commons-math3:3.6.1")
    implementation("commons-cli", "commons-cli", "1.4")
    implementation("space.kscience", "plotlykt-server", "0.4.2")
    testImplementation("junit:junit:4.12")
}


tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
    }
}