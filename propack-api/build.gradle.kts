plugins {
    `java-library`
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))

repositories {
    mavenCentral()
}

dependencies {
    "compileOnly"(files("../libs/flint-path-0.0.1.jar"))
    "compileOnly"("net.kyori:adventure-api:4.12.0")
    "compileOnly"("org.jetbrains:annotations:23.1.0")
    "compileOnly"("org.slf4j:slf4j-api:2.0.7")
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

