/* 
 * ***** RMI Primitive SERVER *****
 */
plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
	// define dependency by project
	implementation(project(":rmi:Primitive_RmiInterface"))
}

// sicherstellen, dass lib zuerst gebaut wird
tasks.named("compileJava") {
    dependsOn(":rmi:Primitive_RmiInterface:build")
}

application {
	// Define the main class for the application.
	mainClass.set("at.fhooe.sail.vis.rmi.Primitive_RmiServer")
}