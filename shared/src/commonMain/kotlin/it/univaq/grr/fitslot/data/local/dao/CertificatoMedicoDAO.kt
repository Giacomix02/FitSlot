package it.univaq.grr.fitslot.data.local.dao

import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import it.univaq.grr.fitslot.data.local.entity.CertificatoMedicoEntity

interface CertificatoMedicoDAO {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(certificatoMedico: CertificatoMedicoEntity): Long

    @Update
    suspend fun update(certificatoMedico: CertificatoMedicoEntity)

    // get certificatoMedico tramite user ID
    @Query("SELECT * FROM certificato_medico WHERE user_id = :userId")
    suspend fun getByUserId(userId: Long): CertificatoMedicoEntity?

    // get certificatoMedico by ID
    @Query("SELECT * FROM certificato_medico WHERE id = :id")
    suspend fun getById(id: Long): CertificatoMedicoEntity?

}