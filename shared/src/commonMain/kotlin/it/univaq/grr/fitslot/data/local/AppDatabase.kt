package it.univaq.grr.fitslot.data.local
import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import it.univaq.grr.fitslot.data.local.dao.UserDao
import it.univaq.grr.fitslot.data.local.entity.UserEntity
import kotlinx.coroutines.Dispatchers

@Database(
    //dichiarare le entità che fanno parte del database
    entities = [UserEntity::class],
    version = 1,
    exportSchema = true
)

//@ColumnTypeConverters(NomeConverter::class) //Inserire solo se si hanno dei converter personalizzati, altrimenti non serve
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    /*
    companion object is a special type of object that is associated with a class rather than
    an instance of the class. It's similar to static members in other programming languages,
    but with some added functionality.
     */

    companion object {
        fun create(): AppDatabase =
            Room.databaseBuilder<AppDatabase>(
                name = "fitslot.db",
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