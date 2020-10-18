import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins{
    kotlin("jvm") version "1.4.10"
    application
}


group = "ru.mipt.npm.sky"
version = "1.0-SNAPSHOT"

application {
    mainClassName = "ru.mipt.npm.reactor.MainKt"
}

repositories {
    mavenCentral()
    jcenter()
    maven("https://kotlin.bintray.com/kotlinx")
    maven("https://dl.bintray.com/mipt-npm/kscience")
    maven("https://dl.bintray.com/mipt-npm/dataforge")
    maven("https://dl.bintray.com/mipt-npm/dev")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    implementation("org.apache.commons:commons-math3:3.6.1")
    compile("commons-cli","commons-cli",  "1.4")
    compile("kscience.plotlykt","plotlykt-server", "0.2.0")
    testImplementation(
        "junit:junit:4.12"
    )
}


tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}