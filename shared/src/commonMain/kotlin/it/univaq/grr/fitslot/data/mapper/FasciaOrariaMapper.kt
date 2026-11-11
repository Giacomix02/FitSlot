package it.univaq.grr.fitslot.data.mapper

import it.univaq.grr.fitslot.data.local.entity.FasciaOrariaEntity
import it.univaq.grr.fitslot.domain.models.FasciaOraria
import java.time.LocalDateTime

fun FasciaOrariaEntity.toDomain(): FasciaOraria{
    val orarioInizio = LocalDateTime.parse(this.orarioInizio)
    val orarioFine = LocalDateTime.parse(this.orarioFine)
    return FasciaOraria(this.id, orarioInizio, orarioFine)

}

fun FasciaOraria.toEntity(): FasciaOrariaEntity{
    return FasciaOrariaEntity(this.id, this.orarioInizio.toString(), this.orarioFine.toString())
}