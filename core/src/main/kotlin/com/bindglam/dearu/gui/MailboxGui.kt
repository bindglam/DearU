package com.bindglam.dearu.gui

import com.bindglam.dearu.Mailbox
import com.bindglam.dearu.manager.LanguageManager
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
import java.util.Calendar


class MailboxGui(private val plugin: JavaPlugin, private val mailbox: Mailbox) : InventoryHolder, Listener {
    companion object {
        private const val ITEMS_PER_PAGE = 9*4

        private val BLANK_SLOT = ItemStack.of(Material.GRAY_STAINED_GLASS_PANE).apply { editMeta { meta -> meta.isHideTooltip = true } }

        private val MAIL_ID_KEY = NamespacedKey("mailbox", "mail_id")
    }

    private val inventory = Bukkit.createInventory(this, 9*6, LanguageManager.lang().get("gui_mailbox_title"))

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

        inventory.setItem(9*2+4, ItemStack.of(Material.CLOCK).apply { editMeta { meta ->
            meta.displayName(LanguageManager.lang().get("gui_mailbox_loading_icon_name").decoration(TextDecoration.ITALIC, false))
            meta.lore(listOf(LanguageManager.lang().get("gui_mailbox_loading_icon_description").decoration(TextDecoration.ITALIC, false)))
        } })
    }

    private fun main() {
        loading()

        mailbox.mails(ITEMS_PER_PAGE, page * ITEMS_PER_PAGE).thenAccept { mails ->
            inventory.setItem(9*2+4, null)
            inventory.setItem(9*5, ItemStack.of(Material.ARROW).apply { editMeta { meta ->
                meta.displayName(LanguageManager.lang().get("gui_mailbox_previous_page_button_name").decoration(TextDecoration.ITALIC, false))
                meta.lore(listOf(LanguageManager.lang().get("gui_mailbox_previous_page_button_description", "page" to page+1).decoration(TextDecoration.ITALIC, false)))
            } })
            inventory.setItem(9*5+8, ItemStack.of(Material.ARROW).apply { editMeta { meta ->
                meta.displayName(LanguageManager.lang().get("gui_mailbox_next_page_button_name").decoration(TextDecoration.ITALIC, false))
                meta.lore(listOf(LanguageManager.lang().get("gui_mailbox_next_page_button_description", "page" to page+1).decoration(TextDecoration.ITALIC, false)))
            } })

            for(i in 0..<ITEMS_PER_PAGE) {
                val mail = mails[i]
                val expiration = Calendar.getInstance().apply { time = mail.mail().expiration() }

                inventory.setItem(9+i, mail.mail().body().apply {
                    editMeta { meta ->
                        val lore = ArrayList(meta.lore() ?: listOf())
                        lore.add(Component.empty())
                        lore.add(Component.empty())
                        lore.add(Component.text("\"${mail.mail().comment()}\"").color(NamedTextColor.GRAY).decorate(TextDecoration.ITALIC))
                        lore.add(Component.empty())
                        lore.add(Component.empty())
                        lore.add(Component.text("FROM. ${mail.mail().sender().displayName()}").color(NamedTextColor.GRAY).decorate(TextDecoration.ITALIC))
                        lore.add(LanguageManager.lang().get("gui_mailbox_mail_item_description_expiration",
                            "expiration_year" to expiration.get(Calendar.YEAR),
                            "expiration_month" to expiration.get(Calendar.MONTH)+1,
                            "expiration_day" to expiration.get(Calendar.DAY_OF_MONTH),
                            "expiration_hours" to expiration.get(Calendar.HOUR_OF_DAY),
                            "expiration_minutes" to expiration.get(Calendar.MINUTE),
                            "expiration_seconds" to expiration.get(Calendar.SECOND)
                        ).decorate(TextDecoration.ITALIC))
                        lore.add(Component.empty())
                        lore.add(LanguageManager.lang().get("gui_mailbox_mail_item_description_left_click_to_receive").decoration(TextDecoration.ITALIC, false))
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

                loading()
                mailbox.mail(clickedItem.persistentDataContainer.get(MAIL_ID_KEY, PersistentDataType.INTEGER)!!).thenAccept { mail ->
                    if(mail.mail().giveItem(player)) {
                        player.playSound(player, Sound.ENTITY_ITEM_PICKUP, 1f, 1.2f)

                        mailbox.removeMail(mail.id()).thenRun { main() }
                    } else {
                        player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1f, 1f)

                        player.sendMessage(LanguageManager.lang().get("gui_mailbox_failed_to_give_mail_item"))

                        main()
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