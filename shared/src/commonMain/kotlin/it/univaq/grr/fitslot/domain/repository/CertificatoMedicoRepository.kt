package it.univaq.grr.fitslot.domain.repository

import it.univaq.grr.fitslot.domain.models.CertificatoMedico
import it.univaq.grr.fitslot.domain.models.User

interface CertificatoMedicoRepository {

    suspend fun getCertificatoMedicoByUser(user: User) : CertificatoMedico?

    suspend fun addCertificatoMedico(certificato: CertificatoMedico): Long

    suspend fun modifyCertificatoMedico(certificato: CertificatoMedico)

    suspend fun iscritti(): List<User>
    suspend fun certificati(scaduti: Boolean? = null, dal: String = "", al: String = "",
                           oggi: String = it.univaq.grr.fitslot.utils.dataOggi()): List<CertificatoMedico>
}