plugins {
    // Pure interface/data library -> no main class, so no 'application' (and no 'run' task)
    `java-library`
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // https://mvnrepository.com/artifact/jakarta.xml.ws/jakarta.xml.ws-api
    // implementation("jakarta.xml.ws:jakarta.xml.ws-api:4.0.2")
}

val jarName = "EnvironmentI"

tasks.jar {
    archiveBaseName.set(jarName)
    doLast {
        copy {
            println("*** copying jar to general libs folder ... ")
            val bDir: String = layout.buildDirectory.get().toString()
            val fromS: String = "${bDir}\\libs\\${jarName}.jar"
            val intoS: String = "${rootProject.projectDir.absolutePath}\\libs"  // move to parent libs directory
            // println("from ${fromS}")
            // println("into ${intoS}")
            from(fromS)
            into(intoS)
        }
    }
}

tasks.clean {
    val fN: String = "${rootProject.projectDir.absolutePath}\\libs\\${jarName}.jar"
    // println("deleting ... ${fN}")
    delete(files(fN))
}
