package it.univaq.grr.fitslot.domain
/** Legge i permessi del login esistente, senza gestire una nuova sessione. */
interface PermessiGestione {
    fun puoGestire(): Boolean
    fun richiediGestore() {
        check(puoGestire()) { "Operazione riservata al gestore. Effettua l'accesso con un account gestore." }
    }
}
