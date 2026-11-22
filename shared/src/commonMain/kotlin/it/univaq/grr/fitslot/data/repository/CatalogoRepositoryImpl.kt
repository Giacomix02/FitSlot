package it.univaq.grr.fitslot.data.repository

import it.univaq.grr.fitslot.data.local.dao.CatalogoDao
import it.univaq.grr.fitslot.domain.PermessiGestione
import it.univaq.grr.fitslot.data.mapper.toDomain
import it.univaq.grr.fitslot.data.mapper.toEntity
import it.univaq.grr.fitslot.domain.models.Abbonamento
import it.univaq.grr.fitslot.domain.models.PianoAPunti
import it.univaq.grr.fitslot.data.local.entity.CorsoAbbonamentoEntity
import it.univaq.grr.fitslot.data.local.entity.SalaAbbonamentoEntity
import it.univaq.grr.fitslot.domain.repository.CatalogoRepository
import kotlinx.coroutines.flow.map

/** Gestisce il catalogo dei piani attraverso il DAO. */
class CatalogoRepositoryImpl(
    private val dao: CatalogoDao,
    private val permessi: PermessiGestione = PermessiGestioneDaLocalStorage
) : CatalogoRepository {
    override fun osservaAbbonamenti() = dao.osservaAbbonamenti().map { lista -> lista.map { it.toDomain() } }
    override fun osservaPianiAPunti() = dao.osservaPianiAPunti().map { lista -> lista.map { it.toDomain() } }

    override suspend fun salvaAbbonamento(abbonamento: Abbonamento): Long {
        permessi.richiediGestore()
        valida(abbonamento.nome, abbonamento.prezzoCentesimi, abbonamento.durataGiorni)
        return dao.salvaAbbonamento(abbonamento.toEntity())
    }

    override suspend fun salvaPianoAPunti(piano: PianoAPunti): Long {
        permessi.richiediGestore()
        valida(piano.nome, piano.prezzoCentesimi, piano.ingressiInclusi)
        return dao.salvaPianoAPunti(piano.toEntity())
    }

    override suspend fun eliminaAbbonamento(id: Long) {
        permessi.richiediGestore()
        dao.eliminaAbbonamento(id)
    }

    override suspend fun eliminaPianoAPunti(id: Long) {
        permessi.richiediGestore()
        dao.eliminaPianoAPunti(id)
    }

    override suspend fun associaSala(abbonamentoId: Long, salaId: Long) {
        permessi.richiediGestore()
        val risultato = dao.associaSala(SalaAbbonamentoEntity(abbonamentoId, salaId))
        check(risultato != -1L) { "La sala è già inclusa in questo abbonamento." }
    }

    override suspend fun saleIncluse(abbonamentoId: Long) =
        dao.saleIncluse(abbonamentoId).map { it.toDomain() }

    override suspend fun associaSale(abbonamentoId: Long, saleId: List<Long>) {
        permessi.richiediGestore()
        require(saleId.isNotEmpty()) { "Seleziona almeno una sala." }
        dao.associaSale(saleId.map { SalaAbbonamentoEntity(abbonamentoId, it) })
    }

    override suspend fun corsi() = dao.corsi()
    override suspend fun corsiInclusi(abbonamentoId: Long) = dao.corsiInclusi(abbonamentoId)
    override suspend fun associaCorsi(abbonamentoId: Long, corsiId: List<Long>) {
        permessi.richiediGestore()
        require(corsiId.isNotEmpty()) { "Seleziona almeno un corso." }
        dao.associaCorsi(corsiId.map { CorsoAbbonamentoEntity(abbonamentoId, it) })
    }

    private fun valida(nome: String, prezzo: Long, quantita: Int) {
        require(nome.isNotBlank()) { "Inserisci il nome del piano." }
        require(prezzo >= 0) { "Il prezzo non può essere negativo." }
        require(quantita > 0) { "Durata o ingressi devono essere maggiori di zero." }
    }
}
