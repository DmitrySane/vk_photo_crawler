plugins {
    application
    kotlin("jvm") version "1.4.10"
}

group = "ru.serobyan"
version = "0.1-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    jcenter()
    gradlePluginPortal()
    maven(url = "https://kotlin.bintray.com/kotlinx")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.10")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("org.kodein.di:kodein-di:7.0.0")
    implementation("net.lightbody.bmp:browsermob-core:2.1.5")
    implementation("org.jsoup:jsoup:1.13.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.7")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("com.zaxxer:HikariCP:3.4.5")
    implementation("org.jetbrains.exposed:exposed-core:0.26.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.26.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.26.1")
    implementation("org.seleniumhq.selenium:selenium-java:3.141.59")
    implementation("org.xerial:sqlite-jdbc:3.31.1")
    implementation("com.zaxxer:HikariCP:3.4.2")
    implementation("io.ktor:ktor-client-core:1.3.2")
    implementation("io.ktor:ktor-client-apache:1.3.2")
    implementation("commons-io:commons-io:2.7")
    implementation("commons-cli:commons-cli:1.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.0")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:4.3.0")
    testImplementation("io.kotest:kotest-assertions-core-jvm:4.3.0")
    testImplementation("io.kotest:kotest-property-jvm:4.3.0")
}

application {
    mainClassName = "ru.serobyan.vk_photo_crawler.MainKt"
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}