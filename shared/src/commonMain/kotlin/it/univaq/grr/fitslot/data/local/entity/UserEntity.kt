package it.univaq.grr.fitslot.data.local.entity

import androidx.annotation.NonNull
import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import it.univaq.grr.fitslot.domain.models.Ruolo


@Entity(
    tableName = "user",
    indices = [
        Index(value = ["email"], unique = true)
    ]
)
data class UserEntity (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @NonNull
    val email: String,
    @NonNull
    val password: String,
    val salt: String,
    @NonNull
    val username: String,
    @NonNull
    val nome: String,
    @NonNull
    val cognome: String,
    @NonNull
    @ColumnInfo(name = "data_nascita")
    val dataNascita: String,
    @NonNull
    val telefono: String,
    @NonNull
    val indirizzo: String,
    @NonNull
    val citta: String,
    @NonNull
    @ColumnInfo(name = "email_contatto")
    val emailContatto: String,
    val ruolo: Ruolo
)