package com.bindglam.dearu.manager

import com.bindglam.dearu.DearUConfiguration
import com.bindglam.dearu.language.Language
import java.io.File

object LanguageManager : Managerial {
    private val builtInLanguages = listOf("english", "korean")
    private val langsFolder = File("plugins/DearU/langs")

    private val langs = hashMapOf<String, Language>()

    private lateinit var config: DearUConfiguration

    override fun start(context: Context) {
        this.config = context.config

        if(!langsFolder.exists())
            langsFolder.mkdirs()

        builtInLanguages.forEach { name ->
            val file = File(langsFolder, "$name.yml")
            if(file.exists()) return@forEach
            file.createNewFile()

            context.plugin.getResource("langs/$name.yml")?.copyTo(file.outputStream())
        }

        langsFolder.listFiles().forEach { file ->
            val lang = Language(file.nameWithoutExtension).also { it.load(file) }
            langs[lang.name] = lang
        }
    }

    override fun end(context: Context) {
        langs.clear()
    }

    fun lang() = langs[this.config.language.value()]!!
}