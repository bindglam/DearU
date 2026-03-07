plugins {
    id("java-library")
    kotlin("jvm")
}

group = "com.bindglam.dearu"
version = property("plugin_version").toString()

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
    maven("https://repo.codemc.io/repository/creatorfromhell/")
}

dependencies {
    compileOnly("com.alibaba.fastjson2:fastjson2:2.0.61")
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

kotlin {
    jvmToolchain(21)
}