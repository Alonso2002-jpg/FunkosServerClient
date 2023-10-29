plugins {
    id("java")
    jacoco
    //shadowjar
    id("com.github.johnrengelman.shadow") version "7.1.2"

}

group = "org.develop"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    // version para compilar y ejecutar en Java 11, subir a 17
    sourceCompatibility = "17"
    targetCompatibility = "17"
}

dependencies {
    implementation("io.projectreactor:reactor-core:3.5.10")
    implementation("io.r2dbc:r2dbc-h2:1.0.0.RELEASE")
    implementation("io.r2dbc:r2dbc-pool:1.0.0.RELEASE")
    implementation("com.google.code.gson:gson:2.10.1")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-junit-jupiter:5.5.0")
    testImplementation("org.mockito:mockito-core:5.5.0")
    implementation("org.projectlombok:lombok:1.18.28")
    testImplementation("org.projectlombok:lombok:1.18.28")
    annotationProcessor("org.projectlombok:lombok:1.18.28")
    implementation("ch.qos.logback:logback-classic:1.4.11")
    implementation("com.auth0:java-jwt:4.2.1")
    implementation("org.mindrot:jbcrypt:0.4")
}

tasks.test {
    useJUnitPlatform()
}


tasks.shadowJar{
    manifest{
        attributes["Main-Class"] = "org.develop.main.Server"
    }

    dependsOn(tasks.test)
}

//tasks.jar {
//    manifest {
//        attributes["Main-Class"] = "org.develop.main.Server"
//    }
//    configurations["compileClasspath"].forEach { file: File ->
//        from(zipTree(file.absoluteFile))
//    }
//    duplicatesStrategy = DuplicatesStrategy.INCLUDE
//}