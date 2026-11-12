package it.univaq.grr.fitslot.data.repository

import it.univaq.grr.fitslot.data.local.dao.CorsoDao
import it.univaq.grr.fitslot.data.mapper.toDomain
import it.univaq.grr.fitslot.data.mapper.toEntity
import it.univaq.grr.fitslot.domain.models.Corso
import it.univaq.grr.fitslot.domain.models.FasciaOraria
import it.univaq.grr.fitslot.domain.repository.CorsoRepository


class CorsoRepositoryImpl (private val corsoDao: CorsoDao): CorsoRepository {
    override suspend fun creaCorso(corso: Corso) {
        // Il controllo definitivo avviene nel database: evita doppie prenotazioni
        // anche se due schermate provano a creare lo stesso corso in contemporanea.
        check(!salaOccupata(corso.salaAssegnata.id, corso.fasciaOraria)) {
            "La sala ${corso.salaAssegnata.nome} è già occupata in questa fascia oraria."
        }
        // La fascia deve esistere nel database per poter essere confrontata
        // con le altre tramite la query di sovrapposizione. Se la fascia non
        // ha ancora un id (id == 0) usiamo quello generato dal database.
        val fasciaId = salvaFasciaOraria(corso.fasciaOraria)
        corsoDao.insertAll(corso.copy(fasciaOraria = corso.fasciaOraria.copy(id = fasciaId)).toEntity())
    }

    override suspend fun salvaFasceOrarie(fasceOrarie: List<FasciaOraria>) {
        fasceOrarie.forEach { salvaFasciaOraria(it) }
    }

    private suspend fun salvaFasciaOraria(fasciaOraria: FasciaOraria): Long {
        val orarioInizio = fasciaOraria.orarioInizio.toString()
        val orarioFine = fasciaOraria.orarioFine.toString()
        if (fasciaOraria.id != 0L) {
            corsoDao.insertFasciaOraria(fasciaOraria.toEntity())
            return fasciaOraria.id
        }
        corsoDao.fasciaOrariaId(orarioInizio, orarioFine)?.let { return it }
        return corsoDao.insertFasciaOraria(fasciaOraria.toEntity())
    }

    override suspend fun salaOccupata(salaId: Long, fasciaOraria: FasciaOraria): Boolean =
        corsoDao.contaCorsiSovrapposti(
            salaId = salaId,
            orarioInizio = fasciaOraria.orarioInizio.toString(),
            orarioFine = fasciaOraria.orarioFine.toString()
        ) > 0

    override suspend fun saleOccupate(fasciaOraria: FasciaOraria): Set<Long> =
        corsoDao.saleOccupateInFascia(
            orarioInizio = fasciaOraria.orarioInizio.toString(),
            orarioFine = fasciaOraria.orarioFine.toString()
        ).toSet()

    override suspend fun fasceOccupate(salaId: Long): List<FasciaOraria> =
        corsoDao.fasceOrarieOccupatePerSala(salaId).map { it.toDomain() }
}
