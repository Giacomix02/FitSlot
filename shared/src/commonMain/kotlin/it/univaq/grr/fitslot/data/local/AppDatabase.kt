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
    //dichiarare le entità che fanno parte del database
    entities = [
//        UserEntity::class,
        SalaEntity::class, CorsoEntity::class, FasciaOrariaEntity::class],
    version = 1,
    exportSchema = true
)

//@ColumnTypeConverters(NomeConverter::class) //Inserire solo se si hanno dei converter personalizzati, altrimenti non serve
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
//    abstract fun userDao(): UserDao

    abstract fun salaDao(): SalaDao
    abstract fun corsoDao(): CorsoDao

    /*
    companion object is a special type of object that is associated with a class rather than
    an instance of the class. It's similar to static members in other programming languages,
    but with some added functionality.
     */

    companion object {
        fun create(name: String = "fitslot.db"): AppDatabase =
            Room.databaseBuilder<AppDatabase>(
                name = name,
                factory = AppDatabaseConstructor::initialize
            )
                .setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(Dispatchers.IO)
                .build()
    }
}

// Room genera l'implementazione "actual" su ogni piattaforma
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}