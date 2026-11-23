package it.univaq.grr.fitslot.domain

import it.univaq.grr.fitslot.domain.models.Ruolo
/** Legge i permessi del login esistente, senza gestire una nuova sessione. */
interface PermessiGestione {
    fun puoGestire(ruolo: Ruolo = Ruolo.GESTORE): Boolean
    fun richiediRuolo(ruolo: Ruolo) {
        check(puoGestire(ruolo)) { "Accesso riservato al ruolo ${ruolo.value}." }
    }
    fun richiediGestore() { richiediRuolo(Ruolo.GESTORE) }
}
