package it.univaq.grr.fitslot.domain.repository

import it.univaq.grr.fitslot.domain.models.Abbonamento
import it.univaq.grr.fitslot.domain.models.PianoAPunti
import it.univaq.grr.fitslot.domain.models.Sala
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
}
