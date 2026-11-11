package it.univaq.grr.fitslot.data.local
//import it.univaq.grr.fitslot.data.local.entity.UserEntity
import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import it.univaq.grr.fitslot.data.local.dao.CorsoDao
import it.univaq.grr.fitslot.data.local.dao.SalaDao
import it.univaq.grr.fitslot.data.local.entity.CorsoEntity
import it.univaq.grr.fitslot.data.local.entity.FasciaOrariaEntity
import it.univaq.grr.fitslot.data.local.entity.SalaEntity
import kotlinx.coroutines.Dispatchers

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
                name = name,
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