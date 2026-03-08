package com.bindglam.dearu.utils

import com.destroystokyo.paper.profile.ProfileProperty
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.util.*


class ItemBuilder private constructor(private val itemStack: ItemStack) {
    companion object {
        fun of(material: Material) = ItemBuilder(ItemStack.of(material))

        fun playerHead(textures: String) = ItemBuilder(ItemStack.of(Material.PLAYER_HEAD).apply { editMeta(SkullMeta::class.java) { meta ->
            val profile = Bukkit.createProfile(UUID.randomUUID())
            profile.setProperty(ProfileProperty("textures", textures))
            meta.playerProfile = profile
        } })
    }

    fun displayName(name: Component): ItemBuilder {
        itemStack.editMeta { meta ->
            meta.displayName(name)
        }
        return this
    }

    fun lore(lore: List<Component>): ItemBuilder {
        itemStack.editMeta { meta ->
            meta.lore(lore)
        }
        return this
    }

    fun lore(vararg lore: Component): ItemBuilder {
        return lore(lore.toList())
    }

    fun hideTooltip(): ItemBuilder {
        itemStack.editMeta { meta ->
            meta.isHideTooltip = true
        }
        return this
    }

    fun build() = itemStack
}