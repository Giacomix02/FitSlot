package it.univaq.grr.fitslot.domain.repository

import it.univaq.grr.fitslot.domain.models.Sala

/** Definisce le operazioni sulle sale disponibili alla logica dell'app.
 * I dettagli del databse sono gestiti dall'implemetazione. */
interface SalaRepository {
    //Salva una nuova sala e restituisce l'id assegnato.
    suspend fun inserisci(sala: Sala): Long
}