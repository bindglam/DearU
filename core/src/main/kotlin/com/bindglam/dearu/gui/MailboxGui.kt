package com.bindglam.dearu.gui

import com.bindglam.dearu.Mailbox
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin


class MailboxGui(private val plugin: JavaPlugin, private val mailbox: Mailbox) : InventoryHolder, Listener {
    companion object {
        private const val ITEMS_PER_PAGE = 9*4

        private val LOADING_ICON = ItemStack.of(Material.CLOCK).apply { editMeta { meta ->
            meta.displayName(Component.text("로딩 중...").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false))
            meta.lore(listOf(Component.text("잠시만 기다려주세요!").color(NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false)))
        } }
        private val BLANK_SLOT = ItemStack.of(Material.GRAY_STAINED_GLASS_PANE).apply { editMeta { meta -> meta.isHideTooltip = true } }
        private val PREVIOUS_PAGE_BUTTON = ItemStack.of(Material.ARROW).apply { editMeta { meta ->
            meta.displayName(Component.text("이전 페이지").color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false))
        } }
        private val NEXT_PAGE_BUTTON = ItemStack.of(Material.ARROW).apply { editMeta { meta ->
            meta.displayName(Component.text("다음 페이지").color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false))
        } }

        private val MAIL_ID_KEY = NamespacedKey("mailbox", "mail_id")
    }

    private val inventory = Bukkit.createInventory(this, 9*6, Component.text("우편함"))

    private var page = 0

    init {
        main()

        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    private fun frame() {
        for(i in 0..8) {
            inventory.setItem(i, BLANK_SLOT)
            inventory.setItem(9*5+i, BLANK_SLOT)
        }
    }

    private fun loading() {
        inventory.clear()

        frame()

        inventory.setItem(9*2+4, LOADING_ICON)
    }

    private fun main() {
        loading()

        mailbox.mails(ITEMS_PER_PAGE, page * ITEMS_PER_PAGE).thenAccept { mails ->
            inventory.setItem(9*2+4, null)
            inventory.setItem(9*5, PREVIOUS_PAGE_BUTTON.clone().apply { editMeta { meta -> meta.lore(listOf(Component.text("현재 페이지 : ${page+1}쪽").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false))) } })
            inventory.setItem(9*5+8, NEXT_PAGE_BUTTON.clone().apply { editMeta { meta -> meta.lore(listOf(Component.text("현재 페이지 : ${page+1}쪽").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false))) } })

            for(i in 0..<ITEMS_PER_PAGE) {
                val mail = mails[i]

                inventory.setItem(9+i, mail.mail().body().apply {
                    editMeta { meta ->
                        val lore = ArrayList(meta.lore() ?: listOf())
                        lore.add(Component.empty())
                        lore.add(Component.empty())
                        lore.add(Component.text("\"${mail.mail().comment()}\"").color(NamedTextColor.GRAY).decorate(TextDecoration.ITALIC))
                        lore.add(Component.empty())
                        lore.add(Component.empty())
                        lore.add(Component.text("FROM. ${mail.mail().sender().displayName()}").color(NamedTextColor.GRAY).decorate(TextDecoration.ITALIC))
                        lore.add(Component.empty())
                        lore.add(Component.text(">> 왼쪽 클릭으로 받기").color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false).decorate(TextDecoration.BOLD))
                        meta.lore(lore)
                    }
                    editPersistentDataContainer { pdc -> pdc.set(MAIL_ID_KEY, PersistentDataType.INTEGER, mail.id()) }
                })
            }
        }
    }

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        if(event.inventory.getHolder(false) != this) return

        event.isCancelled = true

        val player = event.whoClicked as Player
        val slot = event.rawSlot
        val clickedItem = event.currentItem ?: return

        when(slot) {
            9*5 -> {
                if(clickedItem.type != Material.ARROW) return

                page = 0.coerceAtLeast(page - 1)

                player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1.5f)
                main()
            }

            9*5+8 -> {
                if(clickedItem.type != Material.ARROW) return

                page++

                player.playSound(player, Sound.UI_BUTTON_CLICK, 1f, 1.5f)
                main()
            }

            else -> {
                if(!clickedItem.persistentDataContainer.has(MAIL_ID_KEY)) return

                player.playSound(player, Sound.ENTITY_ITEM_PICKUP, 1f, 1.2f)

                loading()
                mailbox.mail(clickedItem.persistentDataContainer.get(MAIL_ID_KEY, PersistentDataType.INTEGER)!!).thenAccept { mail ->
                    if(mail.mail().giveItem(player)) {
                        mailbox.removeMail(mail.id()).thenRun { main() }
                    } else {
                        player.sendMessage(Component.text("아이템을 지급하는데 실패했습니다. 다시 시도해주세요!").color(NamedTextColor.RED))
                    }
                }
            }
        }
    }

    @EventHandler
    fun onClose(event: InventoryCloseEvent) {
        if(event.inventory.getHolder(false) != this) return

        val player = event.player as Player

        player.scheduler.runDelayed(plugin, { _ -> player.updateInventory() }, null, 1L)
        HandlerList.unregisterAll(this)
    }

    override fun getInventory(): Inventory = inventory
}