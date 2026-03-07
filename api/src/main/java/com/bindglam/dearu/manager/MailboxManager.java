package com.bindglam.dearu.manager;

import com.bindglam.dearu.Mailbox;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface MailboxManager {
    @NotNull Mailbox getMailbox(@NotNull UUID owner);

    default @NotNull Mailbox getMailbox(@NotNull Player player) {
        return getMailbox(player.getUniqueId());
    }
}
