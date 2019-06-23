import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins{
    kotlin("jvm") version "1.3.40"
    application
    idea
}


group = "ru.mipt.npm.sky"
version = "1.0-SNAPSHOT"

application {
    mainClassName = "${group}.MainKt"
}

repositories {
    mavenCentral()
    jcenter()
    maven("https://kotlin.bintray.com/kotlinx")
    maven("https://dl.bintray.com/mipt-npm/scientifik/")
    maven("https://dl.bintray.com/mipt-npm/dataforge")
    maven("https://dl.bintray.com/kotlin/ktor/")
    maven("http://npm.mipt.ru:8081/artifactory/gradle-dev")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.40")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.2.1")
    implementation("org.apache.commons:commons-math3:3.6.1")
    compile("commons-cli","commons-cli",  "1.4")
    compile("scientifik","plotlykt-core", "0.1.1")
    compile("scientifik","plotlykt-server", "0.1.2-dev-1")
    testImplementation(
        "junit:junit:4.12"
    )
}


tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}