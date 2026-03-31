plugins {
    java
    application
}

group = "org.vertex"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.json:json:20231013")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass.set("org.vertex.Main")
}

tasks.test {
    useJUnitPlatform()
}