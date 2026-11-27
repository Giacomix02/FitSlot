package it.univaq.grr.fitslot.data.repository

import it.univaq.grr.fitslot.data.exceptions.CertificatoMedicoNonTrovatoException
import it.univaq.grr.fitslot.data.local.dao.CertificatoMedicoDAO
import it.univaq.grr.fitslot.data.mapper.toDomain
import it.univaq.grr.fitslot.data.mapper.toEntity
import it.univaq.grr.fitslot.domain.models.CertificatoMedico
import it.univaq.grr.fitslot.domain.models.User
import it.univaq.grr.fitslot.domain.repository.CertificatoMedicoRepository
import it.univaq.grr.fitslot.utils.dataScadenza

class CertificatoMedicoRepositoryImpl(
    private val certificatoMedicoDAO: CertificatoMedicoDAO
) : CertificatoMedicoRepository {

    override suspend fun getCertificatoMedicoByUser(user: User): CertificatoMedico {
        val certificato =
            certificatoMedicoDAO.getByUserId(user.id)?.toDomain(user) ?: throw CertificatoMedicoNonTrovatoException()
        return certificato
    }

    override suspend fun addCertificatoMedico(certificato: CertificatoMedico): Long =
        certificatoMedicoDAO.insert(certificato.toEntity())

    override suspend fun modifyCertificatoMedico(certificato: CertificatoMedico) {
        TODO("Not yet implemented")
    }
    override suspend fun iscritti(): List<User> {
        return certificatoMedicoDAO.iscritti().map { it.toDomain() }
    }

    override suspend fun certificati(scaduti: Boolean?, dal: String, al: String, oggi: String): List<CertificatoMedico> {
        val risultato = mutableListOf<CertificatoMedico>()
        for (utente in iscritti()) {
            var certificato: CertificatoMedico?
            try {
                certificato = getCertificatoMedicoByUser(utente)
            } catch (_: CertificatoMedicoNonTrovatoException) {
                continue
            }

            if (scaduti == null) {
                risultato.add(certificato)
                continue
            }
            val data = dataScadenza(certificato.scadenza.toString()) ?: continue //TODO: Spaghetti, refactor
            if (scaduti && data < oggi) risultato.add(certificato)
            else if (!scaduti && data >= oggi && data >= dal && data <= al) risultato.add(certificato)
        }
        return risultato
    }
}