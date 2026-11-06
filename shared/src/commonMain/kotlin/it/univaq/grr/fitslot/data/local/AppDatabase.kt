package it.univaq.grr.fitslot.data.local

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import it.univaq.grr.fitslot.data.local.dao.SalaDao
import it.univaq.grr.fitslot.data.local.entity.SalaEntity

/** Definisce le tabelle del databse e i DAO disponibili. */

@Database(
    entities = [SalaEntity::class],
    version = 1,
    exportSchema = true
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    //permette di ottenere il DAO per le operazioni sulle sale.
    abstract fun salaDao(): SalaDao
}

/** L'implementazione specifiac per la piattaforma viene generata da room. */
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor :
        RoomDatabaseConstructor<AppDatabase>