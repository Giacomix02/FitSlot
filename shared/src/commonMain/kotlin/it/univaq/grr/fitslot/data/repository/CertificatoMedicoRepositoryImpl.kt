package it.univaq.grr.fitslot.data.repository

import it.univaq.grr.fitslot.data.local.dao.CertificatoMedicoDAO
import it.univaq.grr.fitslot.data.mapper.toEntity
import it.univaq.grr.fitslot.domain.models.CertificatoMedico
import it.univaq.grr.fitslot.domain.models.User
import it.univaq.grr.fitslot.domain.repository.CertificatoMedicoRepository

class CertificatoMedicoRepositoryImpl(
    private val certificatoMedicoDAO: CertificatoMedicoDAO
) : CertificatoMedicoRepository {

    override suspend fun getCertificatoMedicoByUser(user: User): CertificatoMedico {
        TODO("Not yet implemented")
    }

    override suspend fun addCertificatoMedico(certificato: CertificatoMedico) {
        certificatoMedicoDAO.insert(certificato.toEntity())

    }

    override suspend fun modifyCertificatoMedico(certificato: CertificatoMedico) {
        TODO("Not yet implemented")
    }
}