package com.bindglam.dearu

import com.bindglam.config.Configuration
import com.bindglam.config.complex.EnumField
import com.bindglam.dearu.manager.DatabaseManager
import java.io.File

class DearUConfiguration(file: File) : Configuration(file) {
    val database = Database()
    inner class Database {
        val sql = SQL()
        inner class SQL {
            val type = createExtendedComplexField { EnumField("database.sql.type", DatabaseManager.SQLDatabaseType.SQLITE, DatabaseManager.SQLDatabaseType::class.java) }!!

            val sqlite = SQLite()
            inner class SQLite {
                val autoCommit = createPrimitiveField("database.sql.SQLITE.auto-commit", true)!!
                val validTimeout = createPrimitiveField("database.sql.SQLITE.valid-timeout", 60000)!!
            }

            val mysql = MySQL()
            inner class MySQL {
                val host = createPrimitiveField("database.sql.MYSQL.host", "127.0.0.1")!!
                val database = createPrimitiveField("database.sql.MYSQL.database", "minecraft")!!
                val username = createPrimitiveField("database.sql.MYSQL.username", "root")!!
                val password = createPrimitiveField("database.sql.MYSQL.password", "1234")!!
                val maxPoolSize = createPrimitiveField("database.sql.MYSQL.max-pool-size", 10)!!
            }
        }
    }

    val commands = Commands()
    inner class Commands {
        val mailbox = Mailbox()
        inner class Mailbox {
            val aliases = createPrimitiveField("commands.mailbox.aliases", listOf<String>())!!
        }
    }
}