package com.bindglam.dearu

import io.papermc.paper.plugin.loader.PluginClasspathBuilder
import io.papermc.paper.plugin.loader.PluginLoader
import io.papermc.paper.plugin.loader.library.ClassPathLibrary
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.repository.RemoteRepository

@Suppress("UnstableApiUsage")
class DearUPluginLoader : PluginLoader {
    override fun classloader(classpathBuilder: PluginClasspathBuilder) {
        classpathBuilder.addLibrary(mavenCentral())
        classpathBuilder.addLibrary(jitpack())
    }

    private fun mavenCentral(): ClassPathLibrary {
        val resolver = MavenLibraryResolver()

        resolver.addRepository(RemoteRepository.Builder("central", "default", MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR).build())

        //resolver.addDependency(Dependency(DefaultArtifact("com.zaxxer:HikariCP:7.0.2"), null))
        resolver.addDependency(Dependency(DefaultArtifact("org.semver4j:semver4j:6.0.0"), null))
        resolver.addDependency(Dependency(DefaultArtifact("com.alibaba.fastjson2:fastjson2:2.0.61"), null))

        return resolver
    }

    private fun jitpack(): ClassPathLibrary {
        val resolver = MavenLibraryResolver()

        resolver.addRepository(RemoteRepository.Builder("jitpack", "default", "https://jitpack.io").build())

        resolver.addDependency(Dependency(DefaultArtifact("com.github.bindglam:ConfigLib:1.0.0"), null))
        resolver.addDependency(Dependency(DefaultArtifact("com.github.bindglam:DatabaseLib:1.0.4"), null))

        return resolver
    }
}