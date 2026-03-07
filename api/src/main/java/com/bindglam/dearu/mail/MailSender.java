package com.bindglam.dearu.mail;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public sealed interface MailSender {
    @NotNull String displayName();

    record Player(@NotNull UUID uuid) implements MailSender {
        @Override
        public @NotNull String displayName() {
            return Bukkit.getOfflinePlayer(uuid).getName(); // TODO : optimize
        }
    }

    record Server() implements MailSender {
        @Override
        public @NotNull String displayName() {
            return "서버";
        }
    }


    static @NotNull MailSender player(@NotNull UUID uuid) {
        return new Player(uuid);
    }

    static @NotNull MailSender server() {
        return new Server();
    }
}
