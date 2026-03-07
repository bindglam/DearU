package com.bindglam.dearu.mail;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public sealed interface MailSender {
    @NotNull String displayName();

    record Player(@NotNull UUID uuid) implements MailSender {
        @Override
        public @NotNull String displayName() {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            return offlinePlayer.getName() != null ? offlinePlayer.getName() : "Unknown"; // TODO : optimize
        }
    }

    record Server() implements MailSender {
        @Override
        public @NotNull String displayName() {
            return "Server";
        }
    }


    static @NotNull MailSender player(@NotNull UUID uuid) {
        return new Player(uuid);
    }

    static @NotNull MailSender server() {
        return new Server();
    }
}
