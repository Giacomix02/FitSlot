package it.univaq.grr.fitslot.data.local.entity

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

/** Il nome normalizzato impedisce duplicati anche con maiuscole diverse. */
@Entity(tableName = "abbonamenti", indices = [Index(value = ["nomeNormalizzato"], unique = true)])
data class AbbonamentoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,
    val nomeNormalizzato: String,
    val prezzoCentesimi: Long,
    val durataGiorni: Int
)
