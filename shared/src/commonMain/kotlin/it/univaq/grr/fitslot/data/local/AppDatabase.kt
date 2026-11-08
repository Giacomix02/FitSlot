package it.univaq.grr.fitslot.data.local

import androidx.room3.*
import it.univaq.grr.fitslot.data.local.converter.Converters
import it.univaq.grr.fitslot.data.local.dao.*
import it.univaq.grr.fitslot.data.local.entity.*

/** Definisce le tabelle condivise da sale, corsi e login. */
@Database(
    entities = [SalaEntity::class, UserEntity::class, CorsoEntity::class, FasciaOrariaEntity::class],
    version = 2,
    exportSchema = true
)
@ColumnTypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun salaDao(): SalaDao
    abstract fun userDao(): UserDao
    abstract fun corsoDao(): CorsoDao
}

// Room genera l'implementazione specifica per la piattaforma.
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
