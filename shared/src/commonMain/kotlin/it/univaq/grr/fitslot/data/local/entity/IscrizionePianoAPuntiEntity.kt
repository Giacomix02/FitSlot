package it.univaq.grr.fitslot.data.local.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index

/** Collega l'iscritto al piano e protegge dall'eliminazione del piano in uso. */
@Entity(
    tableName = "iscrizioni_piani_a_punti",
    primaryKeys = ["utenteId", "pianoId"],
    indices = [Index("pianoId")],
    foreignKeys = [
        ForeignKey(entity = UserEntity::class, parentColumns = ["id"],
            childColumns = ["utenteId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = PianoAPuntiEntity::class, parentColumns = ["id"],
            childColumns = ["pianoId"], onDelete = ForeignKey.RESTRICT)
    ]
)
data class IscrizionePianoAPuntiEntity(val utenteId: Long, val pianoId: Long)
