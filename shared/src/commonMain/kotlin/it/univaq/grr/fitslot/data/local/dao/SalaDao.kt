package it.univaq.grr.fitslot.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import it.univaq.grr.fitslot.data.local.entity.SalaEntity

/** Definisce le operazioni di accesso ai dati delle sale.
 * Room genera l'implementazione di questa interfaccia. */

@Dao
interface SalaDao {
    /** Inserisce una sala e restituisce l'identificativo generato. */

    @Insert
    suspend fun inserisci(sala: SalaEntity): Long
}