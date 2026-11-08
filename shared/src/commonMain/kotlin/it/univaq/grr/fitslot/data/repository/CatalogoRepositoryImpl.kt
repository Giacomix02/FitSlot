package it.univaq.grr.fitslot.data.repository

import it.univaq.grr.fitslot.data.local.dao.CatalogoDao
import it.univaq.grr.fitslot.data.mapper.toDomain
import it.univaq.grr.fitslot.data.mapper.toEntity
import it.univaq.grr.fitslot.domain.SessioneUtente
import it.univaq.grr.fitslot.domain.models.Abbonamento
import it.univaq.grr.fitslot.domain.models.PianoAPunti
import it.univaq.grr.fitslot.domain.repository.CatalogoRepository
import kotlinx.coroutines.flow.map

/** Condivide l'accesso ai piani e verifica il ruolo prima delle modifiche. */
class CatalogoRepositoryImpl(
    private val dao: CatalogoDao,
    private val sessione: SessioneUtente
) : CatalogoRepository {
    override fun osservaAbbonamenti() = dao.osservaAbbonamenti().map { lista -> lista.map { it.toDomain() } }
    override fun osservaPianiAPunti() = dao.osservaPianiAPunti().map { lista -> lista.map { it.toDomain() } }

    override suspend fun salvaAbbonamento(abbonamento: Abbonamento): Long {
        sessione.richiediGestore()
        valida(abbonamento.nome, abbonamento.prezzoCentesimi, abbonamento.durataGiorni)
        return dao.salvaAbbonamento(abbonamento.toEntity())
    }

    override suspend fun salvaPianoAPunti(piano: PianoAPunti): Long {
        sessione.richiediGestore()
        valida(piano.nome, piano.prezzoCentesimi, piano.ingressiInclusi)
        return dao.salvaPianoAPunti(piano.toEntity())
    }

    override suspend fun eliminaAbbonamento(id: Long) {
        sessione.richiediGestore()
        dao.eliminaAbbonamento(id)
    }

    override suspend fun eliminaPianoAPunti(id: Long) {
        sessione.richiediGestore()
        dao.eliminaPianoAPunti(id)
    }

    private fun valida(nome: String, prezzo: Long, quantita: Int) {
        require(nome.isNotBlank()) { "Inserisci il nome del piano." }
        require(prezzo >= 0) { "Il prezzo non può essere negativo." }
        require(quantita > 0) { "Durata o ingressi devono essere maggiori di zero." }
    }
}
