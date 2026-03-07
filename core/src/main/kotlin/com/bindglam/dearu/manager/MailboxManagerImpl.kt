package com.bindglam.dearu.manager

import com.bindglam.dearu.Mailbox
import com.bindglam.dearu.MailboxImpl
import java.util.UUID

object MailboxManagerImpl : Managerial, MailboxManager {
    const val TABLE_NAME = "mailboxes"

    override fun start(context: Context) {
        DatabaseManager.sqlDatabase.getResource { connection ->
            MailboxImpl.createTable(connection)
        }
    }

    override fun end(context: Context) {
    }

    override fun getMailbox(owner: UUID): Mailbox = MailboxImpl(owner)
}