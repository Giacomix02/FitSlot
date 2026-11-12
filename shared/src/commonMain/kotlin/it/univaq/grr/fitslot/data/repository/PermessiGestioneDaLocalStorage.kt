package it.univaq.grr.fitslot.data.repository
import it.univaq.grr.fitslot.data.local.LocalStorage
import it.univaq.grr.fitslot.domain.PermessiGestione
import it.univaq.grr.fitslot.domain.models.Ruolo
import it.univaq.grr.fitslot.domain.models.User
/** Non modifica l'utente salvato dal login del team. */
object PermessiGestioneDaLocalStorage : PermessiGestione {
    override fun puoGestire(): Boolean =
        (LocalStorage.getAll()["user"] as? User)?.ruolo == Ruolo.GESTORE
}
