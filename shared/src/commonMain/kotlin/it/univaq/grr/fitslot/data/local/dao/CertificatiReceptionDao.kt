package it.univaq.grr.fitslot.data.local.dao

import androidx.room3.Dao
import androidx.room3.Query
import it.univaq.grr.fitslot.domain.models.IscrittoCertificato

@Dao
interface CertificatiReceptionDao {
    // Se sono stati caricati più certificati, si considera l'ultimo inserito.
    @Query("""
        SELECT u.id, u.nome, u.cognome, u.email, c.scadenza
        FROM user u LEFT JOIN certificato_medico c ON c.id = (
            SELECT id FROM certificato_medico WHERE user_id = u.id ORDER BY id DESC LIMIT 1
        )
        WHERE u.ruolo = 'ISCRITTO' ORDER BY u.cognome, u.nome
    """)
    suspend fun iscritti(): List<IscrittoCertificato>

}
