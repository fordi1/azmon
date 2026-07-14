buildscript {
  repositories {
    maven("https://maven.aliyun.com/repository/google")
    maven("https://maven.aliyun.com/repository/central")
    google()
    mavenCentral()
  }
  dependencies {
    classpath("com.android.tools.build:gradle:8.10.1")
    classpath("com.google.devtools.ksp:symbol-processing-gradle-plugin:2.3.5")
  }
}

plugins {
  id("org.jetbrains.kotlin.android") version "2.2.10" apply false
  id("org.jetbrains.kotlin.plugin.compose") version "2.2.10" apply false
}
