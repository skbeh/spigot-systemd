java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20-R0.1-SNAPSHOT")
    compileOnly("com.velocitypowered:velocity-api:3.1.1")
    annotationProcessor("com.velocitypowered:velocity-api:3.1.1")
    implementation("org.slf4j:slf4j-nop:2.0.7")
    implementation("info.faljse:SDNotify:1.5-SNAPSHOT")
}

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}
