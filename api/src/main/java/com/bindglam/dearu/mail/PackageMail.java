package com.bindglam.dearu.mail;

import com.alibaba.fastjson2.JSONObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.util.*;

public record PackageMail(
        @NotNull MailSender sender,
        @NotNull Body packageBody,
        @Nullable String comment,
        @NotNull Timestamp createdAt,
        @Nullable List<String> allowedServers
) implements Mail {
    @Override
    public @NotNull ItemStack body() {
        ItemStack body = ItemStack.of(Material.CHEST);
        body.editMeta(meta -> {
            meta.displayName(Component.text(packageBody().name()).color(NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false).decorate(TextDecoration.BOLD));
            meta.lore(packageBody().contents().stream()
                    .map(content ->
                            Component.text(" - ").append(content.displayName()).color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false))
                    .toList());
        });
        return body;
    }

    @Override
    public boolean giveItem(@NotNull Player player) {
        var inventory = player.getInventory();
        var storageContents = inventory.getStorageContents();

        int emptySlots = 0;
        for(var content : storageContents) {
            if(content == null || content.isEmpty()) {
                emptySlots++;
            }
        }

        if(emptySlots < packageBody.contents().size())
            return false;

        packageBody.contents().forEach(inventory::addItem);
        return true;
    }

    @ApiStatus.Internal
    @Override
    public @NotNull JSONObject serialize() {
        JSONObject json = new JSONObject();
        json.put("type", "package");
        if(sender instanceof MailSender.Player(UUID uuid))
            json.put("sender", uuid.toString());
        json.put("body", packageBody.serialize());
        if(comment != null)
            json.put("comment", comment);
        json.put("createdAt", createdAt.getTime());
        if(allowedServers != null)
            json.put("allowedServers", allowedServers);
        return json;
    }

    @SuppressWarnings("unused")
    @ApiStatus.Internal
    public static @NotNull PackageMail deserialize(@NotNull JSONObject json) {
        MailSender sender = MailSender.server();
        if(json.containsKey("sender"))
            sender = MailSender.player(UUID.fromString(json.getString("sender")));
        Body packageBody = Body.deserialize(json.getJSONObject("body"));
        String comment = null;
        if(json.containsKey("comment"))
            comment = json.getString("comment");
        Timestamp createdAt = new Timestamp(json.getLong("createdAt"));
        List<String> allowedServers = null;
        if(json.containsKey("allowedServers"))
            allowedServers = json.getJSONArray("allowedServers").stream().map(Objects::toString).toList();
        return new PackageMail(sender, packageBody, comment, createdAt, allowedServers);
    }

    public static Body.Builder bodyBuilder() {
        return new Body.Builder();
    }

    public record Body(
            @NotNull String name,
            @NotNull List<ItemStack> contents
    ) {
        @ApiStatus.Internal
        public @NotNull JSONObject serialize() {
            JSONObject json = new JSONObject();
            json.put("name", name);
            json.put("contents", Base64.getEncoder().encodeToString(ItemStack.serializeItemsAsBytes(contents)));
            return json;
        }

        @ApiStatus.Internal
        public static @NotNull Body deserialize(@NotNull JSONObject json) {
            String name = json.getString("name");
            ItemStack[] contents = ItemStack.deserializeItemsFromBytes(Base64.getDecoder().decode(json.getString("contents")));
            return new Body(name, Arrays.stream(contents).toList());
        }

        public static final class Builder {
            private String name;
            private final List<ItemStack> contents = new ArrayList<>();

            private Builder() {
            }

            public Builder name(String name) {
                this.name = name;
                return this;
            }

            public Builder content(ItemStack content) {
                this.contents.add(content);
                return this;
            }

            public Body build() {
                return new Body(name, contents);
            }
        }
    }
}
