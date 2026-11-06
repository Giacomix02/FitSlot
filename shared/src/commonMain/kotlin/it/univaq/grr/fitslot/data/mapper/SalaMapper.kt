package it.univaq.grr.fitslot.data.mapper

import it.univaq.grr.fitslot.data.local.entity.SalaEntity
import it.univaq.grr.fitslot.domain.models.Sala

/** Converte il mod di dominio nella rappresentaione
utilizzata da Room per salvare una sala. */

fun Sala.toEntity(): SalaEntity{
    return SalaEntity(
        id = this.id,
        nome = this.nome,
        capienza = this.capienza
    )
}
