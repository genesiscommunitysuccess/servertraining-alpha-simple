ext.set("localDaogenVersion", "ALPHA")

plugins {
    `maven-publish`
}

subprojects {
    apply(plugin = "org.gradle.maven-publish")

    dependencies {
        implementation("com.h2database:h2:2.2.224")
    }
    tasks {
        withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
            kotlinOptions {
                freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=all")
                jvmTarget = "17"
            }
        }
        test {
            systemProperty("DbLayer", "SQL")
            systemProperty("DbHost", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
            systemProperty("DbQuotedIdentifiers", "true")
            useJUnitPlatform()

            // Add exports and opens so ChronicleQueue can continue working in JDK 17.
            // More info in: https://chronicle.software/chronicle-support-java-17/
            jvmArgs = jvmArgs!! + listOf(
                "--add-exports=java.base/jdk.internal.ref=ALL-UNNAMED",
                "--add-exports=java.base/sun.nio.ch=ALL-UNNAMED",
                "--add-exports=jdk.unsupported/sun.misc=ALL-UNNAMED",
                "--add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED",
                "--add-opens=jdk.compiler/com.sun.tools.javac=ALL-UNNAMED",
                "--add-opens=java.base/java.lang=ALL-UNNAMED",
                "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED",
                "--add-opens=java.base/java.io=ALL-UNNAMED",
                "--add-opens=java.base/java.util=ALL-UNNAMED",
                "--add-opens=java.base/java.nio=ALL-UNNAMED" // this one is opened for LMDB
            )
        }
    }
}

tasks {
    assemble {
        for(subproject in subprojects){
            dependsOn(subproject.tasks.named("assemble"))
        }
    }
    build {
        for(subproject in subprojects){
            dependsOn(subproject.tasks.named("build"))
        }
    }
    clean {
        for(subproject in subprojects){
            dependsOn(subproject.tasks.named("clean"))
        }
    }
}

allprojects {

    group = "global.genesis"
    version = "1.0.0-SNAPSHOT"


    kotlin {
        jvmToolchain {
            (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(17))
        }
    }
    tasks.withType<Jar> {
        duplicatesStrategy = DuplicatesStrategy.WARN
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    repositories {
        mavenLocal {
            // VERY IMPORTANT!!! EXCLUDE AGRONA AS IT IS A POM DEPENDENCY AND DOES NOT PLAY NICELY WITH MAVEN LOCAL!
            content {
                excludeGroup("org.agrona")
            }
        }
        mavenCentral()
        maven {
            val repoUrl = if(properties["clientSpecific"] == "true") {
                "https://genesisglobal.jfrog.io/genesisglobal/libs-release-client"
            } else {
                "https://genesisglobal.jfrog.io/genesisglobal/dev-repo"
            }
            url = uri(repoUrl)
            credentials {
                username = properties["genesisArtifactoryUser"].toString()
                password = properties["genesisArtifactoryPassword"].toString()
            }
        }
    }

    publishing {
        publications.create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

