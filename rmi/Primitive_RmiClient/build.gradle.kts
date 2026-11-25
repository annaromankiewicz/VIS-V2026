/* 
 * ***** RMI Primitive CLIENT *****
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
	// implementation(project(":rmi:Primitive_RmiInterface"))
    implementation(files("${rootProject.projectDir.absolutePath}/libs/Primitive_RmiInterface.jar"))
}

application {
	// Define the main class for the application.
	mainClass.set("at.fhooe.sail.vis.rmi.Primitive_RmiClient")
}