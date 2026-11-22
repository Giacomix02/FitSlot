package it.univaq.grr.fitslot.domain

import it.univaq.grr.fitslot.data.local.LocalStorage
import it.univaq.grr.fitslot.domain.models.Ruolo
import it.univaq.grr.fitslot.domain.models.User

object PermessiReception {
    fun puoControllare(): Boolean =
        (LocalStorage.getAll()["user"] as? User)?.ruolo == Ruolo.RECEPIONIST

    fun richiediReception() {
        check(puoControllare()) { "Accesso riservato alla reception." }
    }
}
