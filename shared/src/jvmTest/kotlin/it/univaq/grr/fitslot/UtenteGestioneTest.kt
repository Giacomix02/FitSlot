package it.univaq.grr.fitslot
import it.univaq.grr.fitslot.data.local.LocalStorage
import it.univaq.grr.fitslot.data.local.entity.UserEntity
import it.univaq.grr.fitslot.data.mapper.toDomain
import it.univaq.grr.fitslot.domain.models.Ruolo

/** Dato di prova isolato: usa lo stesso contratto LocalStorage del login esistente. */
fun impostaUtenteGestioneTest(ruolo: Ruolo? = Ruolo.GESTORE) {
    LocalStorage.deleteAll()
    if (ruolo != null) {
        val user = UserEntity(id = 1, email = "gestione@test.it", password = "hash-test",
            salt = "salt-test", username = "gestione", nome = "Gestore", cognome = "",
            dataNascita = "", telefono = "", indirizzo = "", citta = "",
            emailContatto = "gestione@test.it", ruolo = ruolo)
        LocalStorage.addLocalStorage("user", user.toDomain())
    }
}
