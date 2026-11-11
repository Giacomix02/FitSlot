package it.univaq.grr.fitslot.data.mapper

import it.univaq.grr.fitslot.data.local.entity.AbbonamentoEntity
import it.univaq.grr.fitslot.data.local.entity.PianoAPuntiEntity
import it.univaq.grr.fitslot.domain.models.Abbonamento
import it.univaq.grr.fitslot.domain.models.PianoAPunti

fun AbbonamentoEntity.toDomain() = Abbonamento(id, nome, prezzoCentesimi, durataGiorni)
fun PianoAPuntiEntity.toDomain() = PianoAPunti(id, nome, prezzoCentesimi, ingressiInclusi)

// La chiave di confronto non cambia il nome visualizzato all'utente.
fun Abbonamento.toEntity() = AbbonamentoEntity(
    id, nome.trim(), nome.trim().lowercase(), prezzoCentesimi, durataGiorni)
fun PianoAPunti.toEntity() = PianoAPuntiEntity(
    id, nome.trim(), nome.trim().lowercase(), prezzoCentesimi, ingressiInclusi)
