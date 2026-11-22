package it.univaq.grr.fitslot.data.local

import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

// Parte dallo schema attuale di main: aggiunge solo la nuova tabella.
val MIGRAZIONE_SALE_ABBONAMENTI = object : Migration(1, 2) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL("""
            CREATE TABLE IF NOT EXISTS sale_abbonamenti (
                abbonamentoId INTEGER NOT NULL,
                salaId INTEGER NOT NULL,
                PRIMARY KEY(abbonamentoId, salaId),
                FOREIGN KEY(abbonamentoId) REFERENCES abbonamenti(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(salaId) REFERENCES sale(id) ON UPDATE NO ACTION ON DELETE RESTRICT
            )
        """.trimIndent())
        connection.execSQL("CREATE INDEX IF NOT EXISTS index_sale_abbonamenti_salaId ON sale_abbonamenti(salaId)")
    }
}
