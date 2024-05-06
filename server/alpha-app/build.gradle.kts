dependencies {
    compileOnly(genesis("script-dependencies"))
    implementation("global.genesis:file-server-gpal:7.1.2")
    genesisGeneratedCode(withTestDependency = true)
    testImplementation("global.genesis:genesis-dbtest")
    testImplementation("global.genesis:genesis-testsupport")
    testImplementation("global.genesis:genesis-dataserver2")
    testImplementation("global.genesis:genesis-pal-dataserver")
    testImplementation("global.genesis:genesis-pal-eventhandler")
    api("org.apache.camel:camel-ftp")
    api("org.apache.camel:camel-core")
}

description = "alpha-app"

sourceSets {
    main {
        resources {
            srcDirs("src/main/resources", "src/main/genesis")
        }
    }
}

tasks {
    copyDependencies {
        enabled = false
    }
    test {
        systemProperty("DbLayer", "SQL")
        systemProperty("DbHost", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
        systemProperty("DbQuotedIdentifiers", "true")
    }
}
