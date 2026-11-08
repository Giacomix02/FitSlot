package it.univaq.grr.fitslot.data.local.pojo

import androidx.room3.Embedded
import androidx.room3.Relation
import it.univaq.grr.fitslot.data.local.entity.CorsoEntity
import it.univaq.grr.fitslot.data.local.entity.FasciaOrariaEntity
import it.univaq.grr.fitslot.data.local.entity.SalaEntity
import it.univaq.grr.fitslot.domain.models.FasciaOraria

class CorsoWithSalaAndFasciaOraria {
    @Embedded
    var corso: CorsoEntity? = null

    @Relation(entity = SalaEntity::class, parentColumns = ["id"], entityColumns = ["salaAssegnataId"])
    var sala: SalaEntity? = null

    @Relation(entity = FasciaOrariaEntity::class, parentColumns = ["id"], entityColumns = ["fasciaOrariaId"])
    var fasciaOraria: FasciaOraria? = null
}