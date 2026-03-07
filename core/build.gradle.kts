import xyz.jpenilla.resourcefactory.paper.PaperPluginYaml

plugins {
    id("paper-conventions")
    alias(libs.plugins.resourceFactory.paper)
}

dependencies {
    implementation(project(":api"))
    compileOnly("com.github.bindglam:ConfigLib:1.0.0")
    compileOnly("com.github.bindglam:DatabaseLib:1.0.4")
    compileOnly("net.milkbowl.vault:VaultUnlockedAPI:2.16")
    compileOnly("org.semver4j:semver4j:6.0.0")
}

paperPluginYaml {
    name = rootProject.name
    version = rootProject.version.toString()
    main = "$group.DearUPlugin"
    loader = "$group.DearUPluginLoader"
    apiVersion = "1.21"
    author = "bindglam"
    foliaSupported = true
}