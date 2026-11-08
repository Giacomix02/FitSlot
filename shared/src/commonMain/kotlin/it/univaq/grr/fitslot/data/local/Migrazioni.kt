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


/** Aggiunge i cataloghi e le associazioni agli iscritti senza alterare i dati esistenti. */
val MIGRAZIONE_2_3 = object : Migration(2, 3) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS abbonamenti (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, nome TEXT NOT NULL, nomeNormalizzato TEXT NOT NULL, prezzoCentesimi INTEGER NOT NULL, durataGiorni INTEGER NOT NULL)")
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_abbonamenti_nomeNormalizzato ON abbonamenti(nomeNormalizzato)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS piani_a_punti (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, nome TEXT NOT NULL, nomeNormalizzato TEXT NOT NULL, prezzoCentesimi INTEGER NOT NULL, ingressiInclusi INTEGER NOT NULL)")
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_piani_a_punti_nomeNormalizzato ON piani_a_punti(nomeNormalizzato)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS iscrizioni_abbonamenti (utenteId INTEGER NOT NULL, abbonamentoId INTEGER NOT NULL, PRIMARY KEY(utenteId, abbonamentoId), FOREIGN KEY(utenteId) REFERENCES user(id) ON UPDATE NO ACTION ON DELETE RESTRICT, FOREIGN KEY(abbonamentoId) REFERENCES abbonamenti(id) ON UPDATE NO ACTION ON DELETE RESTRICT)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS index_iscrizioni_abbonamenti_abbonamentoId ON iscrizioni_abbonamenti(abbonamentoId)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS iscrizioni_piani_a_punti (utenteId INTEGER NOT NULL, pianoId INTEGER NOT NULL, PRIMARY KEY(utenteId, pianoId), FOREIGN KEY(utenteId) REFERENCES user(id) ON UPDATE NO ACTION ON DELETE RESTRICT, FOREIGN KEY(pianoId) REFERENCES piani_a_punti(id) ON UPDATE NO ACTION ON DELETE RESTRICT)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS index_iscrizioni_piani_a_punti_pianoId ON iscrizioni_piani_a_punti(pianoId)")
    }
}
