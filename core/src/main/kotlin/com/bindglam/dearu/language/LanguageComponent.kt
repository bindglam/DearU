package com.bindglam.dearu.language

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

class LanguageComponent(private val raw: String) {
    fun component(vararg args: Pair<String, Any>): Component {
        var result = raw

        args.forEach { pair -> result = result.replace("{${pair.first}}", pair.second.toString()) }

        return MiniMessage.miniMessage().deserialize(result)
    }
}