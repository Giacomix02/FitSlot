package it.univaq.grr.fitslot.data.local.entity

import androidx.annotation.NonNull
import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey


@Entity(
    tableName = "certificato_medico",
    indices = [Index("user_id")],
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["id"],
        childColumns = ["user_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class CertificatoMedicoEntity (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    var path: String,
    var scadenza: String,
    @ColumnInfo(name="user_id") @NonNull
    var userId : Long,
)