package it.univaq.grr.fitslot.data.local.entity

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey


@Entity(tableName = "corso",
    indices = [Index("name")])
data class CorsoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0, // se 0

    var name: String,

    var salaAssegnataId: Long,

    var fasciaOrariaId: Long)