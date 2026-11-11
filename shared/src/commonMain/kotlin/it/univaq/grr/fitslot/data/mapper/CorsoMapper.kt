package it.univaq.grr.fitslot.data.mapper

import it.univaq.grr.fitslot.data.local.entity.CorsoEntity
import it.univaq.grr.fitslot.data.local.entity.FasciaOrariaEntity
import it.univaq.grr.fitslot.data.local.entity.SalaEntity
import it.univaq.grr.fitslot.domain.models.Corso
import it.univaq.grr.fitslot.domain.models.FasciaOraria
import it.univaq.grr.fitslot.domain.models.Sala

fun CorsoEntity.toDomain(): Corso{
    val salaAssegnata: SalaEntity = SalaEntity(-1, "dummyNome", capienza = 10) //TODO inserire chiamata funzione fetch della sala tramite id
    val fasciaOraria: FasciaOraria = FasciaOrariaEntity(-1, "2027/12/1 14:30", "2027/12/1 16:30") //TODO inserire chiamata funzione fetch della fascia tramite id
        .toDomain()
    return Corso(
        this.id,
        this.name,
        Sala(salaAssegnata.id, salaAssegnata.nome, salaAssegnata.capienza),
        FasciaOraria(0, fasciaOraria.orarioInizio, fasciaOraria.orarioFine)
    )

}

fun Corso.toEntity(): CorsoEntity{
    return CorsoEntity(this.id, this.name, this.salaAssegnata.id, this.fasciaOraria.id)
}