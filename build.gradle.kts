plugins {
    kotlin("jvm") version "2.0.21"
    antlr
    application
}

group = "io.github.jwyoon1220"
version = "1.0.0"

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("io.github.jwyoon1220.cbugger.MainKt")
}

repositories {
    mavenCentral()
}

dependencies {
    antlr("org.antlr:antlr4:4.13.2")
    implementation("org.antlr:antlr4-runtime:4.13.2")
    implementation("org.agrona:agrona:1.21.2")
    implementation("it.unimi.dsi:fastutil:8.5.15")
    implementation("org.apache.commons:commons-lang3:3.14.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.generateGrammarSource {
    maxHeapSize = "64m"
    arguments = arguments + listOf("-visitor", "-package", "io.github.jwyoon1220.cbugger.parser")
    outputDirectory = file("${layout.buildDirectory.get()}/generated-src/antlr/main/io/github/jwyoon1220/cbugger/parser")
}

tasks.compileKotlin {
    dependsOn(tasks.generateGrammarSource)
}

sourceSets.main {
    java.srcDir("${layout.buildDirectory.get()}/generated-src/antlr/main")
    antlr.srcDir("src/main/antlr")
}

tasks.test {
    useJUnitPlatform()
}
