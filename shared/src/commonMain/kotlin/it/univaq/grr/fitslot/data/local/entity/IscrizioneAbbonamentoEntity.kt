package it.univaq.grr.fitslot.data.local.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index

/** Collega l'iscritto al piano e protegge dall'eliminazione del piano in uso. */
@Entity(
    tableName = "iscrizioni_abbonamenti",
    primaryKeys = ["utenteId", "abbonamentoId"],
    indices = [Index("abbonamentoId")],
    foreignKeys = [
        ForeignKey(entity = UserEntity::class, parentColumns = ["id"],
            childColumns = ["utenteId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = AbbonamentoEntity::class, parentColumns = ["id"],
            childColumns = ["abbonamentoId"], onDelete = ForeignKey.RESTRICT)
    ]
)
data class IscrizioneAbbonamentoEntity(val utenteId: Long, val abbonamentoId: Long)
