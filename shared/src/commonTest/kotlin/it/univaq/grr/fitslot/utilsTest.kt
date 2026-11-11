package it.univaq.grr.fitslot

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import it.univaq.grr.fitslot.data.local.AppDatabase

fun creaDbInMemoria(): AppDatabase {
    return Room.inMemoryDatabaseBuilder<AppDatabase>().setDriver(BundledSQLiteDriver()).build()
}