import xyz.jpenilla.runpaper.task.RunServer

plugins {
    id("standard-conventions")
    alias(libs.plugins.runTask.paper)
    alias(libs.plugins.shadow)
}

dependencies {
    implementation(project(":core"))
}

val groupString = group.toString()
val versionString = version.toString()
val mcVersionString = property("minecraft_version").toString()

val runServerAction = Action<RunServer> {
    version(mcVersionString)

    downloadPlugins {
    }
}

runPaper.folia.registerTask(op = runServerAction)

tasks {
    runServer {
        runServerAction.execute(this)
    }

    jar {
        finalizedBy(shadowJar)
    }

    shadowJar {
        archiveClassifier = ""

        dependencies {
            exclude(dependency("org.jetbrains:annotations:13.0")); exclude(dependency("org.jetbrains:annotations:23.0.0")); exclude(dependency("org.jetbrains:annotations:26.0.2"))
        }

        fun prefix(pattern: String) {
            relocate(pattern, "$groupString.shaded.$pattern")
        }
        prefix("kotlin")
    }
}