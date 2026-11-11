package it.univaq.grr.fitslot.data.local

import androidx.room3.*
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import it.univaq.grr.fitslot.data.local.converter.Converters
import it.univaq.grr.fitslot.data.local.dao.*
import it.univaq.grr.fitslot.data.local.entity.*
import kotlinx.coroutines.Dispatchers

/** Registra le tabelle delle US integrate senza modificare il funzionamento del login. */
@Database(
    entities = [SalaEntity::class, UserEntity::class, CorsoEntity::class, FasciaOrariaEntity::class,
        AbbonamentoEntity::class, PianoAPuntiEntity::class,
        IscrizioneAbbonamentoEntity::class, IscrizionePianoAPuntiEntity::class],
    version = 1,
    exportSchema = true
)
@ColumnTypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun salaDao(): SalaDao
    abstract fun userDao(): UserDao
    abstract fun corsoDao(): CorsoDao
    abstract fun catalogoDao(): CatalogoDao

    companion object {
        private val instance: AppDatabase by lazy {
            Room.databaseBuilder<AppDatabase>(
                name = "fitslot.db",
                factory = AppDatabaseConstructor::initialize
            )
                .setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(Dispatchers.IO)
                .build()
        }

        fun create(): AppDatabase = instance
    }
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
