package com.bindglam.dearu.utils

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class ItemBuilder private constructor(private val itemStack: ItemStack) {
    companion object {
        fun of(material: Material) = ItemBuilder(ItemStack.of(material))
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