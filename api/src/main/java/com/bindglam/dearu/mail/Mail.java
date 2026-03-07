package com.bindglam.dearu.mail;

import com.alibaba.fastjson2.JSONObject;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Calendar;

public sealed interface Mail permits PackageMail, SingleMail {
    int EXPIRY_DAYS = 3;

    @NotNull MailSender sender();

    @NotNull ItemStack body();

    @NotNull String comment();

    @NotNull Timestamp createdAt();

    boolean giveItem(@NotNull Player player);

    default Timestamp expiration() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(createdAt());
        calendar.add(Calendar.DAY_OF_MONTH, EXPIRY_DAYS);
        return new Timestamp(calendar.getTimeInMillis());
    }

    @ApiStatus.Internal
    @NotNull JSONObject serialize();


    static @NotNull Mail single(MailSender sender, ItemStack body, @Nullable String comment) {
        return new SingleMail(sender, body, comment == null ? "" : comment, new Timestamp(System.currentTimeMillis()));
    }

    static @NotNull Mail packaged(MailSender sender, PackageMail.Body body, @Nullable String comment) {
        return new PackageMail(sender, body, comment == null ? "" : comment, new Timestamp(System.currentTimeMillis()));
    }

    @ApiStatus.Internal
    static @NotNull Mail deserialize(@NotNull JSONObject json, Class<? extends Mail> clazz) {
        try {
            Method method = clazz.getDeclaredMethod("deserialize", JSONObject.class);
            method.setAccessible(true);
            return (Mail) method.invoke(null, json);
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
