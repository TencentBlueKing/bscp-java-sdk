plugins {
    kotlin("jvm") version "1.8.0"
    application
}

group = "com.tencent.bscp.kotlin-example"
version = "1.0"
repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("ch.qos.logback:logback-classic:1.3.14")
    implementation("com.tencent.bk.sdk:bscp-java-sdk:1.1")
    implementation("org.yaml:snakeyaml:2.0")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

application {
    mainClass.set("MainKt")
}
