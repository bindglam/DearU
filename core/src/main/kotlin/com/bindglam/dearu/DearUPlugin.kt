package com.bindglam.dearu

import com.mojang.brigadier.Command
import com.bindglam.dearu.gui.MailboxGui
import com.bindglam.dearu.mail.Mail
import com.bindglam.dearu.mail.MailSender
import com.bindglam.dearu.mail.PackageMail
import com.bindglam.dearu.manager.Context
import com.bindglam.dearu.manager.DatabaseManager
import com.bindglam.dearu.manager.MailboxManager
import com.bindglam.dearu.manager.MailboxManagerImpl
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
        this.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS, LifecycleEventHandler { commands ->
            commands.registrar().register(Commands.literal("mailbox")
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

            commands.registrar().register(Commands.literal("testmailbox")
                .executes { ctx ->
                    val executor = ctx.source.executor ?: ctx.source.sender
                    if(executor !is Player) {
                        executor.sendMessage(Component.text("이 명령어는 플레이어만 사용할 수 있습니다.").color(NamedTextColor.RED))
                        return@executes Command.SINGLE_SUCCESS
                    }

                    val mailbox = MailboxManagerImpl.getMailbox(executor.uniqueId)
                    mailbox.putMail(Mail.single(MailSender.server(), ItemStack.of(Material.BROWN_DYE), "안녕씨빨아"))
                        .thenRun { executor.sendMessage("test") }
                    mailbox.putMail(Mail.packaged(
                        MailSender.server(),
                        PackageMail.bodyBuilder()
                            .name("테스트 패키지")
                            .content(ItemStack.of(Material.BROWN_DYE))
                            .content(ItemStack.of(Material.DIAMOND_SWORD))
                            .content(ItemStack.of(Material.DIAMOND_PICKAXE))
                            .content(ItemStack.of(Material.COOKED_BEEF).apply { amount = 64 })
                            .build(),
                        "안녕하세요 쌍년아")
                    ).thenRun { executor.sendMessage("test2") }

                    return@executes Command.SINGLE_SUCCESS
                }
                .build())
        })
    }

    override fun mailboxManager(): MailboxManager = MailboxManagerImpl
}