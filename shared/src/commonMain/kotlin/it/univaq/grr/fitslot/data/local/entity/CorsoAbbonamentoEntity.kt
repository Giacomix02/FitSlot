package it.univaq.grr.fitslot.data.local.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index

// Ogni coppia abbonamento-corso può essere presente una sola volta.
@Entity(
    tableName = "corsi_abbonamenti",
    primaryKeys = ["abbonamentoId", "corsoId"],
    indices = [Index("corsoId")],
    foreignKeys = [
        ForeignKey(entity = AbbonamentoEntity::class, parentColumns = ["id"],
            childColumns = ["abbonamentoId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = CorsoEntity::class, parentColumns = ["id"],
            childColumns = ["corsoId"], onDelete = ForeignKey.RESTRICT)
    ]
)
data class CorsoAbbonamentoEntity(val abbonamentoId: Long, val corsoId: Long)
