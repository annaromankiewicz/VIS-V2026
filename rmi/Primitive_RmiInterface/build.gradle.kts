/* 
 * ***** RMI Primitive INTERFACE *****
 */
plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    java
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {

}

val jarName = "Primitive_RmiInterface"

tasks.jar {
    archiveBaseName.set(jarName)
    doLast {
        copy {
            println("*** copying jar to general libs folder ... ")
            val bDir = layout.buildDirectory.get().toString()
            val fromS: String = "${bDir}\\libs\\${jarName}.jar"
            val intoS: String = "${rootProject.projectDir.absolutePath}\\libs"  // move to parent libs directory
            from(fromS)
            into(intoS)
        }
    }
}

// extend clean task by removing the copied jar archive from the libs directory
tasks.clean {
    val fN: String = "${rootProject.projectDir.absolutePath}\\libs\\${jarName}.jar\""
    delete(files(fN))
}
