package it.univaq.grr.fitslot.data.local.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
/** Rappresenta una sala della palestra nella tablla sale
 * Ogni sala ha un nome e una capienza intesa come numero di
 * persone che può ospitare. */

@Entity(tableName = "sale")
data class SalaEntity(
    /** L'identificativo viene generato al salvataggio.
     * Il valore 0 indica una nuova sala senza id */

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nome: String,
    val capienza: Int
)