package com.bindglam.dearu.manager

import com.bindglam.dearu.Mailbox
import com.bindglam.dearu.MailboxImpl
import com.bindglam.dearu.mail.Mail
import org.bukkit.Bukkit
import java.util.UUID
import java.util.concurrent.TimeUnit

object MailboxManagerImpl : Managerial, MailboxManager {
    const val TABLE_NAME = "mailboxes"

    override fun start(context: Context) {
        DatabaseManager.sqlDatabase.getResource { connection ->
            MailboxImpl.createTable(connection)
        }

        Bukkit.getAsyncScheduler().runAtFixedRate(context.plugin, { _ ->
            DatabaseManager.sqlDatabase.getResource { connection ->
                val expiryDaysAgo = java.sql.Timestamp(System.currentTimeMillis() - (Mail.EXPIRY_DAYS * 24 * 60 * 60 * 1000))
                connection.prepareStatement("DELETE FROM $TABLE_NAME WHERE createdAt < ?").use { statement ->
                    statement.setTimestamp(1, expiryDaysAgo)
                    statement.executeUpdate()
                }
            }
        }, 1L, 1L, TimeUnit.MINUTES)
    }

    override fun end(context: Context) {
    }

    override fun getMailbox(owner: UUID): Mailbox = MailboxImpl(owner)
}