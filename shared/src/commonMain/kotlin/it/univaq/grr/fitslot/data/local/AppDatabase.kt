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
    version = 3,
    exportSchema = true
)
@ColumnTypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun salaDao(): SalaDao
    abstract fun userDao(): UserDao
    abstract fun corsoDao(): CorsoDao
    abstract fun catalogoDao(): CatalogoDao

    /*
    companion object is a special type of object that is associated with a class rather than
    an instance of the class. It's similar to static members in other programming languages,
    but with some added functionality.
     */
    companion object {
        // Conserva l'API create() usata dal collega e allinea solo percorso e migrazioni.
        fun create(name: String = percorsoDatabasePredefinito()): AppDatabase =
            Room.databaseBuilder<AppDatabase>(name = name, factory = AppDatabaseConstructor::initialize)
                .addMigrations(MIGRAZIONE_1_2, MIGRAZIONE_2_3)
                .setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(Dispatchers.IO)
                .build()
    }
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
