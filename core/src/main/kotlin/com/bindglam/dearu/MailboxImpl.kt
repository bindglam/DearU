package com.bindglam.dearu

import com.alibaba.fastjson2.JSON
import com.bindglam.dearu.mail.Mail
import com.bindglam.dearu.mail.PackageMail
import com.bindglam.dearu.mail.SingleMail
import com.bindglam.dearu.manager.DatabaseManager
import com.bindglam.dearu.manager.MailboxManagerImpl
import java.sql.Connection
import java.util.UUID
import java.util.concurrent.CompletableFuture

class MailboxImpl(private val owner: UUID) : Mailbox {
    companion object {
        fun createTable(connection: Connection) {
            connection.createStatement().use { statement ->
                statement.execute("CREATE TABLE IF NOT EXISTS ${MailboxManagerImpl.TABLE_NAME}" +
                        "(id INTEGER PRIMARY KEY /*!40101 AUTO_INCREMENT */, owner VARCHAR(36), data JSON, createdAt TIMESTAMP)")
                try {
                    statement.execute("CREATE INDEX idx_createdAt ON ${MailboxManagerImpl.TABLE_NAME}(createdAt)")
                } catch (_: Exception) {
                }
            }
        }
    }

    override fun owner() = owner

    override fun mail(id: Int): CompletableFuture<Mailbox.IdentifiedMail> = CompletableFuture.supplyAsync {
        var mail: Mailbox.IdentifiedMail? = null

        DatabaseManager.sqlDatabase.getResource { connection ->
            connection.prepareStatement("SELECT * FROM ${MailboxManagerImpl.TABLE_NAME} WHERE id = ?").use { statement ->
                statement.setInt(1, id)

                statement.executeQuery().use { result ->
                    if(result.next()) {
                        val id = result.getInt("id")
                        val json = JSON.parseObject(result.getString("data"))

                        mail = Mailbox.IdentifiedMail(id, when(json.getString("type")) {
                            "single" -> Mail.deserialize(json, SingleMail::class.java)
                            "package" -> Mail.deserialize(json, PackageMail::class.java)
                            else -> error("Unknown mail type")
                        })
                    }
                }
            }
        }

        return@supplyAsync mail
    }

    override fun mails(limit: Int, offset: Int): CompletableFuture<List<Mailbox.IdentifiedMail>> = CompletableFuture.supplyAsync {
        val mails = arrayListOf<Mailbox.IdentifiedMail>()

        DatabaseManager.sqlDatabase.getResource { connection ->
            connection.prepareStatement("SELECT * FROM ${MailboxManagerImpl.TABLE_NAME} WHERE owner = ? ORDER BY createdAt DESC LIMIT $limit OFFSET $offset").use { statement ->
                statement.setString(1, owner.toString())

                statement.executeQuery().use { result ->
                    while (result.next()) {
                        val id = result.getInt("id")
                        val json = JSON.parseObject(result.getString("data"))

                        val mail = when(json.getString("type")) {
                            "single" -> Mail.deserialize(json, SingleMail::class.java)
                            "package" -> Mail.deserialize(json, PackageMail::class.java)
                            else -> error("Unknown mail type")
                        }

                        mails.add(Mailbox.IdentifiedMail(id, mail))
                    }
                }
            }
        }

        return@supplyAsync mails
    }

    override fun putMail(mail: Mail): CompletableFuture<Void> = CompletableFuture.runAsync {
        DatabaseManager.sqlDatabase.getResource { connection ->
            connection.prepareStatement("INSERT INTO ${MailboxManagerImpl.TABLE_NAME} (owner, data, createdAt) VALUES (?, ?, ?)").use { statement ->
                statement.setString(1, owner.toString())
                statement.setString(2, mail.serialize().toString())
                statement.setTimestamp(3, mail.createdAt())
                statement.executeUpdate()
            }
        }
    }

    override fun removeMail(id: Int): CompletableFuture<Void> = CompletableFuture.runAsync {
        DatabaseManager.sqlDatabase.getResource { connection ->
            connection.prepareStatement("DELETE FROM ${MailboxManagerImpl.TABLE_NAME} WHERE id = ?").use { statement ->
                statement.setInt(1, id)
                statement.executeUpdate()
            }
        }
    }
}