package it.univaq.grr.fitslot.data.local

import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/** Aggiunge le tabelle del team senza cancellare le sale già salvate. */
val MIGRAZIONE_1_2 = object : Migration(1, 2) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS sale (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, nome TEXT NOT NULL, capienza INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS user (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, email TEXT NOT NULL, password TEXT NOT NULL, salt TEXT NOT NULL, username TEXT NOT NULL, nome TEXT NOT NULL, cognome TEXT NOT NULL, data_nascita TEXT NOT NULL, telefono TEXT NOT NULL, indirizzo TEXT NOT NULL, citta TEXT NOT NULL, email_contatto TEXT NOT NULL, ruolo TEXT NOT NULL)")
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_user_email ON user(email)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS corso (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, salaAssegnataId INTEGER NOT NULL, fasciaOrariaId INTEGER NOT NULL)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS index_corso_name ON corso(name)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS fascia_oraria (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, orarioInizio TEXT NOT NULL, orarioFine TEXT NOT NULL, durata INTEGER NOT NULL)")
    }
}
