package com.bindglam.dearu.manager

import com.bindglam.database.Database
import com.bindglam.database.MySQLDatabase
import com.bindglam.database.SQLiteDatabase
import java.io.File
import java.sql.Connection
import java.sql.SQLException

object DatabaseManager : Managerial {
    private lateinit var _sqlDatabase: Database<Connection, SQLException>
    val sqlDatabase: Database<Connection, SQLException>
        get() = _sqlDatabase

    override fun start(context: Context) {
        _sqlDatabase = when(context.config.getString("database.sql.type")) {
            "SQLITE" -> SQLiteDatabase(File(context.plugin.dataFolder, "database.db"),
                context.config.getBoolean("database.sql.SQLITE.auto-commit"), context.config.getInt("database.sql.SQLITE.valid-timeout"))
            "MYSQL" -> MySQLDatabase(context.config.getString("database.sql.MYSQL.host"), context.config.getString("database.sql.MYSQL.database"), context.config.getString("database.sql.MYSQL.username"), context.config.getString("database.sql.MYSQL.password"),
                context.config.getInt("database.sql.MYSQL.max-pool-size"))
            else -> error("Unknown database type")
        }.also { it.start() }
    }

    override fun end(context: Context) {
        _sqlDatabase.stop()
    }
}