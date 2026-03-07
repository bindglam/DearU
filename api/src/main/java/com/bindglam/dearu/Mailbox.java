package com.bindglam.dearu;

import com.bindglam.dearu.mail.Mail;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Mailbox {
    @NotNull UUID owner();

    @NotNull CompletableFuture<IdentifiedMail> mail(int id);

    @NotNull CompletableFuture<List<IdentifiedMail>> mails(int limit, int offset);

    default @NotNull CompletableFuture<List<IdentifiedMail>> mails() {
        return mails(99, 0);
    }

    @NotNull CompletableFuture<Void> putMail(@NotNull Mail mail);

    @NotNull CompletableFuture<Void> removeMail(int id);

    record IdentifiedMail(int id, Mail mail) {
    }
}
