package com.bindglam.dearu

import com.bindglam.dearu.gui.MailboxGui
import com.bindglam.dearu.manager.Context
import com.bindglam.dearu.manager.DatabaseManager
import com.bindglam.dearu.manager.MailboxManager
import com.bindglam.dearu.manager.MailboxManagerImpl
import com.mojang.brigadier.Command
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.handler.LifecycleEventHandler
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.io.File


class DearUPlugin : JavaPlugin(), DearU {
    private lateinit var dearUConfig: DearUConfiguration

    private val managers = listOf(DatabaseManager, MailboxManagerImpl)

    override fun onEnable() {
        DearUProvider.register(this)

        dearUConfig = DearUConfiguration(File(dataFolder, "config.yml"))
            .also { it.load() }

        registerCommands()

        managers.forEach { it.start(Context(this, dearUConfig)) }
    }

    override fun onDisable() {
        DearUProvider.unregister()

        managers.forEach { it.end(Context(this, dearUConfig)) }
    }

    private fun registerCommands() {
        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS, LifecycleEventHandler { commands ->
            fun mailbox(name: String) {
                commands.registrar().register(Commands.literal(name)
                    .executes { ctx ->
                        val executor = ctx.source.executor ?: ctx.source.sender
                        if(executor !is Player) {
                            executor.sendMessage(Component.text("이 명령어는 플레이어만 사용할 수 있습니다.").color(NamedTextColor.RED))
                            return@executes Command.SINGLE_SUCCESS
                        }

                        executor.openInventory(MailboxGui(this, MailboxManagerImpl.getMailbox(executor.uniqueId)).inventory)
                        return@executes Command.SINGLE_SUCCESS
                    }
                    .build())
            }
            mailbox("mailbox")
            dearUConfig.commands.mailbox.aliases.value().forEach { mailbox(it) }
        })
    }

    override fun mailboxManager(): MailboxManager = MailboxManagerImpl
}