package com.bindglam.dearu

import com.bindglam.dearu.gui.MailboxGui
import com.bindglam.dearu.mail.Mail
import com.bindglam.dearu.mail.MailSender
import com.bindglam.dearu.manager.Context
import com.bindglam.dearu.manager.DatabaseManager
import com.bindglam.dearu.manager.LanguageManager
import com.bindglam.dearu.manager.MailboxManager
import com.bindglam.dearu.manager.MailboxManagerImpl
import com.bindglam.dearu.utils.UpdateChecker
import com.mojang.brigadier.Command
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.handler.LifecycleEventHandler
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.io.File


class DearUPlugin : JavaPlugin(), DearU {
    private lateinit var _dearUConfig: DearUConfiguration
    val dearUConfig: DearUConfiguration
        get() = _dearUConfig

    private val managers = listOf(DatabaseManager, LanguageManager, MailboxManagerImpl)

    override fun onEnable() {
        DearUProvider.register(this)

        _dearUConfig = DearUConfiguration(File(dataFolder, "config.yml"))
            .also { it.load() }

        registerCommands()

        server.asyncScheduler.runNow(this) {
            managers.forEach { it.start(Context(this, _dearUConfig)) }
        }

        fun checkUpdate() {
            val checker = UpdateChecker("bindglam", "DearU")

            if(checker.check(pluginMeta.version)) {
                logger.info("A new version of DearU is available!")
                logger.info("https://github.com/bindglam/DearU/releases")
            } else {
                logger.info("You are using the latest version of DearU!")
            }
        }
        server.asyncScheduler.runNow(this) { _ -> checkUpdate() }
    }

    override fun onDisable() {
        DearUProvider.unregister()

        managers.forEach { it.end(Context(this, _dearUConfig)) }
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
            _dearUConfig.commands.mailbox.aliases.value().forEach { mailbox(it) }

            commands.registrar().register(Commands.literal("testmailbox")
                .executes { ctx ->
                    val executor = ctx.source.executor ?: ctx.source.sender
                    if(executor !is Player) {
                        executor.sendMessage(Component.text("이 명령어는 플레이어만 사용할 수 있습니다.").color(NamedTextColor.RED))
                        return@executes Command.SINGLE_SUCCESS
                    }

                    val mailbox = MailboxManagerImpl.getMailbox(executor.uniqueId)
                    mailbox.putMail(Mail.single(MailSender.server(), ItemStack.of(Material.EMERALD), null, listOf("test")))
                    return@executes Command.SINGLE_SUCCESS
                }
                .build())
        })
    }

    override fun mailboxManager(): MailboxManager = MailboxManagerImpl
}