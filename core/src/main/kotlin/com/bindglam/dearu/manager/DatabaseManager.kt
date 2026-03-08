package com.bindglam.dearu.manager

import com.bindglam.database.Database
import com.bindglam.database.MySQLDatabase
import com.bindglam.database.SQLiteDatabase
import com.bindglam.dearu.DearUConfiguration
import java.io.File
import java.sql.Connection
import java.sql.SQLException

object DatabaseManager : Managerial {
    private lateinit var _sqlDatabase: Database<Connection, SQLException>
    val sqlDatabase: Database<Connection, SQLException>
        get() = _sqlDatabase

    override fun start(context: Context) {
        _sqlDatabase = context.config.database.sql.type.value().provider(context).also { it.start() }
    }

    override fun end(context: Context) {
        _sqlDatabase.stop()
    }

    enum class SQLDatabaseType(val provider: (Context) -> Database<Connection, SQLException>) {
        SQLITE({ ctx -> SQLiteDatabase(File(ctx.plugin.dataFolder, "database.db"), true, ctx.config.database.sql.sqlite.validTimeout.value()) }),
        MYSQL({ ctx -> MySQLDatabase(ctx.config.database.sql.mysql.host.value(), ctx.config.database.sql.mysql.database.value(), ctx.config.database.sql.mysql.username.value(), ctx.config.database.sql.mysql.password.value(), ctx.config.database.sql.mysql.maxPoolSize.value()) })
    }
}