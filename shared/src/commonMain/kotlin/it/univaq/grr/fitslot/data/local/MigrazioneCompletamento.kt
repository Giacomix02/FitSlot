package it.univaq.grr.fitslot.data.local

import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

// I branch del team hanno usato sia lo schema 1 sia il 2.
val MIGRAZIONE_COMPLETAMENTO_1_3 = object : Migration(1, 3) {
    override suspend fun migrate(connection: SQLiteConnection) { connection.completaTabelle() }
}
val MIGRAZIONE_COMPLETAMENTO_2_3 = object : Migration(2, 3) {
    override suspend fun migrate(connection: SQLiteConnection) { connection.completaTabelle() }
}

private fun SQLiteConnection.completaTabelle() {
    execSQL("""CREATE TABLE IF NOT EXISTS certificato_medico (
        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, path TEXT NOT NULL,
        scadenza TEXT NOT NULL, user_id INTEGER NOT NULL,
        FOREIGN KEY(user_id) REFERENCES user(id) ON UPDATE NO ACTION ON DELETE CASCADE)""")
    execSQL("CREATE INDEX IF NOT EXISTS index_certificato_medico_user_id ON certificato_medico(user_id)")
    execSQL("""CREATE TABLE IF NOT EXISTS sale_abbonamenti (
        abbonamentoId INTEGER NOT NULL, salaId INTEGER NOT NULL, PRIMARY KEY(abbonamentoId, salaId),
        FOREIGN KEY(abbonamentoId) REFERENCES abbonamenti(id) ON UPDATE NO ACTION ON DELETE CASCADE,
        FOREIGN KEY(salaId) REFERENCES sale(id) ON UPDATE NO ACTION ON DELETE RESTRICT)""")
    execSQL("CREATE INDEX IF NOT EXISTS index_sale_abbonamenti_salaId ON sale_abbonamenti(salaId)")
    execSQL("""CREATE TABLE IF NOT EXISTS corsi_abbonamenti (
        abbonamentoId INTEGER NOT NULL, corsoId INTEGER NOT NULL, PRIMARY KEY(abbonamentoId, corsoId),
        FOREIGN KEY(abbonamentoId) REFERENCES abbonamenti(id) ON UPDATE NO ACTION ON DELETE CASCADE,
        FOREIGN KEY(corsoId) REFERENCES corso(id) ON UPDATE NO ACTION ON DELETE RESTRICT)""")
    execSQL("CREATE INDEX IF NOT EXISTS index_corsi_abbonamenti_corsoId ON corsi_abbonamenti(corsoId)")
}
