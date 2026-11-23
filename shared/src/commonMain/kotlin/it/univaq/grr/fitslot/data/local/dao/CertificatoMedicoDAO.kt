package it.univaq.grr.fitslot.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import it.univaq.grr.fitslot.data.local.entity.UserEntity
import it.univaq.grr.fitslot.data.local.entity.CertificatoMedicoEntity

@Dao
interface CertificatoMedicoDAO {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(certificatoMedico: CertificatoMedicoEntity): Long

    @Update
    suspend fun update(certificatoMedico: CertificatoMedicoEntity)

    // get certificatoMedico tramite user ID
    // Confronta cronologicamente sia le date ISO sia i millisecondi del DatePicker.
    @Query("""SELECT * FROM certificato_medico WHERE user_id = :userId
        ORDER BY CASE WHEN instr(scadenza, '-') > 0 THEN julianday(scadenza)
        ELSE julianday(CAST(scadenza AS INTEGER) / 1000, 'unixepoch') END DESC, id DESC LIMIT 1""")
    suspend fun getByUserId(userId: Long): CertificatoMedicoEntity?

    // get certificatoMedico by ID
    @Query("SELECT * FROM certificato_medico WHERE id = :id")
    suspend fun getById(id: Long): CertificatoMedicoEntity?

    @Query("SELECT * FROM user WHERE ruolo = 'ISCRITTO' ORDER BY cognome, nome")
    suspend fun iscritti(): List<UserEntity>
}