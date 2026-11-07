package it.univaq.grr.fitslot.data.local
import androidx.room3.ColumnTypeConverters
import androidx.room3.ConstructedBy
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import androidx.room3.Database
import it.univaq.grr.fitslot.data.local.entity.UserEntity

@Database(
    //dichiarare le entità che fanno parte del database
    entities = [UserEntity::class],
    version = 1,
    exportSchema = true
)

//@ColumnTypeConverters(NomeConverter::class) //Inserire solo se si hanno dei converter personalizzati, altrimenti non serve
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    // Definire qui i DAO come abstract fun
}

// Room genera l'implementazione "actual" su ogni piattaforma
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}