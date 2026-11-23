package it.univaq.grr.fitslot.data.local.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index

// Ogni coppia abbonamento-sala può essere presente una sola volta.
@Entity(
    tableName = "sale_abbonamenti",
    primaryKeys = ["abbonamentoId", "salaId"],
    indices = [Index("salaId")],
    foreignKeys = [
        ForeignKey(entity = AbbonamentoEntity::class, parentColumns = ["id"],
            childColumns = ["abbonamentoId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = SalaEntity::class, parentColumns = ["id"],
            childColumns = ["salaId"], onDelete = ForeignKey.RESTRICT)
    ]
)
data class SalaAbbonamentoEntity(val abbonamentoId: Long, val salaId: Long)
