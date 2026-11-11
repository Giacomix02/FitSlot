package it.univaq.grr.fitslot.data.local

import androidx.room3.*
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import it.univaq.grr.fitslot.data.local.converter.Converters
import it.univaq.grr.fitslot.data.local.dao.*
import it.univaq.grr.fitslot.data.local.entity.*
import it.univaq.grr.fitslot.domain.models.Ruolo
import it.univaq.grr.fitslot.utils.PasswordSecurity
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
                .addCallback(object : RoomDatabase.Callback() {
                    override suspend fun onOpen(connection: SQLiteConnection) {
                        super.onOpen(connection)
                        connection.seedDefaultAdmin()
                    }
                })
                .build()
        }

        fun create(): AppDatabase = instance
    }
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

private fun SQLiteConnection.seedDefaultAdmin() {
    val adminEmail = "admin"
    val plainPassword = "admin"
    val salt = PasswordSecurity.generateSalt()
    val hashedPassword = PasswordSecurity.hashPassword(plainPassword, salt)
    val ruolo = Ruolo.GESTORE.name

    execSQL(
        """
        INSERT OR IGNORE INTO user (
            email, password, salt, username, nome, cognome,
            data_nascita, telefono, indirizzo, citta, email_contatto, ruolo
        ) VALUES (
            '$adminEmail',
            '$hashedPassword',
            '$salt',
            'admin',
            'Amministratore',
            'FitSlot',
            '1990-01-01',
            '0000000000',
            'Via Roma 1',
            'L''Aquila',
            '$adminEmail',
            '$ruolo'
        );
        """.trimIndent()
    )
}