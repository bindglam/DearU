package com.bindglam.dearu.mail;

import com.alibaba.fastjson2.JSONObject;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.util.Base64;
import java.util.UUID;

public record SingleMail(
        @NotNull MailSender sender,
        @NotNull ItemStack body,
        @NotNull String comment,
        @NotNull Timestamp createdAt
) implements Mail {
    @Override
    public @NotNull ItemStack body() {
        return body.clone();
    }

    @Override
    public boolean giveItem(@NotNull Player player) {
        // TODO : 남은 인벤토리 공간 확인
        player.getInventory().addItem(body);
        return true;
    }

    @ApiStatus.Internal
    @Override
    public @NotNull JSONObject serialize() {
        JSONObject json = new JSONObject();
        json.put("type", "single");
        if(sender instanceof MailSender.Player(UUID uuid))
            json.put("sender", uuid.toString());
        json.put("body", Base64.getEncoder().encodeToString(body.serializeAsBytes()));
        json.put("comment", comment);
        json.put("createdAt", createdAt.getTime());
        return json;
    }

    @ApiStatus.Internal
    public static @NotNull SingleMail deserialize(@NotNull JSONObject json) {
        MailSender sender = MailSender.server();
        if(json.containsKey("sender"))
            sender = MailSender.player(UUID.fromString(json.getString("sender")));
        ItemStack body = ItemStack.deserializeBytes(Base64.getDecoder().decode(json.getString("body")));
        String comment = json.getString("comment");
        Timestamp createdAt = new Timestamp(json.getLong("createdAt"));
        return new SingleMail(sender, body, comment, createdAt);
    }
}
