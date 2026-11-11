package it.univaq.grr.fitslot.data.local.entity

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(tableName = "fascia_oraria", indices = [Index(value = ["orarioInizio", "orarioFine" ], unique = true)])
data class FasciaOrariaEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var orarioInizio: String, var orarioFine: String)

