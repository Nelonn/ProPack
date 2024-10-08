plugins {
    `java-library`
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))

repositories {
    mavenCentral()
}

var adventureVersion = "4.15.0"

dependencies {
    compileOnly(files("../libs/flint-path-0.0.1.jar"))
    compileOnly("net.kyori:adventure-api:$adventureVersion")
    compileOnly("org.jetbrains:annotations:24.1.0")
    compileOnly("org.slf4j:slf4j-api:2.0.7")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
    options {
        this as StandardJavadocDocletOptions
        addBooleanOption("Xdoclint:none", true)
        addStringOption("Xmaxwarns", "1")
    }
}

java {
    withSourcesJar()
    withJavadocJar()
}

