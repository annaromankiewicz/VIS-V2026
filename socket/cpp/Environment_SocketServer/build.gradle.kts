import org.apache.tools.ant.taskdefs.condition.Os

plugins {
    id("cpp-application")
}

// Use Cygwin GCC only on Windows, and only if it is installed
// (otherwise Gradle falls back to Visual C++ / the system compiler)
if (Os.isFamily(Os.FAMILY_WINDOWS) && file("C:/cygwin64/bin").exists()) {
    extensions.configure<NativeToolChainRegistry> {
        register<Gcc>("cygwin") {
            path(file("C:/cygwin64/bin"))
        }
    }
}

// ************************************************************
// ENTER Name of the sub project ...
// ************************************************************
val subProjectName: String = "Environment_SocketServer"
// ************************************************************
//
// ************************************************************

application {
    when {
        Os.isFamily(Os.FAMILY_WINDOWS) -> {
            targetMachines.set(listOf(machines.windows)) // baut für Windows Host-Architektur
        }

        Os.isFamily(Os.FAMILY_MAC) -> {
            targetMachines.set(listOf(machines.macOS)) // baut für macOS Host-Architektur
        }

        Os.isFamily(Os.FAMILY_UNIX) -> {
            targetMachines.set(listOf(machines.linux)) // baut für linux Host-Architektur
        }
        else -> {
            throw GradleException(":cpp:${subProjectName} run-target -> unknown OS family encountered")
        }
    }
}

tasks.register<Exec>("run") {
    // sicherstellen, dass erst gebaut wird
    dependsOn("build")
    group = "application"
    standardInput = System.`in`    // enable commandline input

    val exeFile: String = when {
        Os.isFamily(Os.FAMILY_WINDOWS) -> "$subProjectName.exe"
        Os.isFamily(Os.FAMILY_MAC) -> subProjectName
        Os.isFamily(Os.FAMILY_UNIX) -> subProjectName
        else -> throw GradleException(":cpp:${subProjectName} run-target -> unknown OS family encountered")
    }

    // Pfad zum Executable bestimmen (im build/exe/... Ordner)
    val exe = layout.buildDirectory.file("exe/main/debug/$exeFile")

    // Übergabeparameter beim Programmstart definieren ...
    args = listOf("4949")

    // Kommando setzen
    executable = exe.get().asFile.absolutePath
}

// task to stop a running server on the port
tasks.register("kill", Exec::class) {
    group = "application"
    val port = 4949
    when {
        // /c (not /k): cmd must exit after the command, otherwise Gradle hangs
        Os.isFamily(Os.FAMILY_WINDOWS) -> {
            commandLine("cmd", "/c", "for /f \"tokens=5\" %a in ('netstat -aon ^| findstr :${port} ^| findstr LISTENING') do taskkill /F /PID %a")
            isIgnoreExitValue = true
        }
        Os.isFamily(Os.FAMILY_MAC) -> {
            commandLine("bash", "-c", "lsof -nti:${port} | xargs kill -9")
        }
        Os.isFamily(Os.FAMILY_UNIX) -> {
            commandLine("bash", "-c", "lsof -nti:${port} | xargs kill -9")
        }
        else -> { throw GradleException(":cpp:${subProjectName} kill-target -> unknown OS family encountered")}
    }
}

tasks["run"].dependsOn("kill")   // CAREFUL: only suitable for the server side!
tasks["clean"].dependsOn("kill") // CAREFUL: only suitable for the server side!