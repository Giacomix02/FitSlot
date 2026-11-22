package it.univaq.grr.fitslot.domain.repository

import it.univaq.grr.fitslot.domain.models.Abbonamento
import it.univaq.grr.fitslot.domain.models.PianoAPunti
import it.univaq.grr.fitslot.domain.models.Sala
import it.univaq.grr.fitslot.domain.models.CorsoIncluso
import kotlinx.coroutines.flow.Flow

/** Operazioni sui piani sottoscrivibili, separate dalle singole iscrizioni. */
interface CatalogoRepository {
    fun osservaAbbonamenti(): Flow<List<Abbonamento>>
    fun osservaPianiAPunti(): Flow<List<PianoAPunti>>
    suspend fun salvaAbbonamento(abbonamento: Abbonamento): Long
    suspend fun salvaPianoAPunti(piano: PianoAPunti): Long
    suspend fun eliminaAbbonamento(id: Long)
    suspend fun eliminaPianoAPunti(id: Long)
    suspend fun associaSala(abbonamentoId: Long, salaId: Long)
    suspend fun saleIncluse(abbonamentoId: Long): List<Sala>
    suspend fun associaSale(abbonamentoId: Long, saleId: List<Long>)
    suspend fun corsi(): List<CorsoIncluso>
    suspend fun associaCorsi(abbonamentoId: Long, corsiId: List<Long>)
    suspend fun corsiInclusi(abbonamentoId: Long): List<CorsoIncluso>
}
