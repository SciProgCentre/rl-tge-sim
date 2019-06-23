import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins{
    kotlin("jvm") version "1.3.40"
    application
}


group = "ru.mipt.npm.sky"
version = "1.0-SNAPSHOT"

application {
    mainClassName = "${group}.MainKt"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.40")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.2.1")
    implementation("org.apache.commons:commons-math3:3.6.1")
    compile("commons-cli","commons-cli",  "1.4")
}


tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}