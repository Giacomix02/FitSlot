# US di Roby: sale, abbonamenti e piani a punti

## Avvio

Dalla directory `/Users/uni/IdeaProjects/FitSlot`:

```bash
./gradlew :desktopApp:run
```

L'applicazione usa un unico database in `~/.fitslot/fitslot.db`.
Se non esistono utenti compare la configurazione del primo gestore:
inserire nome, email, password e conferma. Non esistono credenziali predefinite.
Questa configurazione è bloccata in transazione dopo la creazione del primo utente.
Alle aperture successive si accede con le credenziali scelte. Il logout elimina
la sessione in memoria. Non vengono conservate password nella sessione.

`FITSLOT_DB_PATH` permette di usare un database separato per le prove.
I database sperimentali creati in passato dai colleghi come `fitslot.db`
nella directory di lavoro non vengono importati o cancellati automaticamente.

## GRR-14

Creazione della sala con nome e capienza, salvataggio e conferma.
La Home mostra l'operazione solo al gestore autenticato. Il ruolo viene controllato
anche dal repository prima dell'inserimento.

## GRR-15

Catalogo degli abbonamenti: nome, prezzo in euro, durata in giorni.
L'abbonamento include ingressi illimitati per il periodo previsto.
È possibile creare, modificare ed eliminare i piani. Il catalogo osserva il
database e presenta subito il piano creato come sottoscrivibile.

## GRR-44

Catalogo dei piani a punti: nome, prezzo in euro, ingressi inclusi.
Come concordato, i punti rappresentano ingressi, non una seconda valuta.
È possibile creare, modificare ed eliminare i piani.

## Regole comuni

- Nome obbligatorio e univoco all'interno di ciascun catalogo.
- Il confronto elimina gli spazi iniziali/finali e ignora maiuscole/minuscole.
- Prezzo non negativo, con massimo due decimali; virgola e punto sono accettati.
- I prezzi vengono salvati in centesimi interi, senza arrotondamenti floating point.
- Durata in giorni e ingressi inclusi devono essere interi maggiori di zero.
- Prima dell'eliminazione viene richiesta conferma.
- L'eliminazione viene impedita se ci sono iscritti; l'utente riceve un messaggio.
- Errori di salvataggio mantengono i dati del modulo per permettere la correzione.
- Le operazioni di modifica sono riservate al gestore anche nel repository.

Prezzo, durata in giorni e validazioni sono le scelte minime di implementazione:
vanno confermate in revisione dal team, perché le US non dettagliano questi campi.

## Integrazione con la gestione iscritti

I cataloghi descrivono i piani disponibili, non i singoli acquisti.
Le associazioni effettive sono nelle tabelle `iscrizioni_abbonamenti` e
`iscrizioni_piani_a_punti`, con chiavi esterne verso `user` e il rispettivo piano.
Il DAO espone `associaAbbonamento` e `associaPianoAPunti` per l'integrazione
con le US di assegnazione. Non è stata aggiunta una schermata di assegnazione,
che appartiene alle altre US del team.

Il controllo considera tutte le associazioni presenti: un piano collegato a
un iscritto non può essere eliminato. La transazione e i vincoli SQLite evitano
eliminazioni accidentali anche se il dato cambia durante l'operazione.

## Migrazioni

- Versione 1: tabella delle sale già usata nella prima prova.
- Versione 2: integra utenti, corsi e fasce orarie dei colleghi.
- Versione 3: aggiunge cataloghi e associazioni agli iscritti.

Le migrazioni sono additive, senza fallback distruttivo. Gli schemi esportati
sono conservati in `shared/schemas`. Il test parte dallo schema 1 esportato,
verifica il passaggio alla versione attuale e riapre il database controllando
che sala e piano salvati siano ancora presenti.

## Verifiche

```bash
./gradlew :desktopApp:compileKotlin :shared:jvmTest
```

I test SQLite usano database temporanei o in memoria, non il database personale.
Coprono CRUD dei due cataloghi, duplicati in creazione/modifica, piano con
iscritti, chiavi esterne, permessi, validazione dei prezzi, login,
configurazione iniziale e migrazione. I test Compose verificano le azioni
dei moduli e la disponibilità delle operazioni in base al ruolo.

Il lavoro dei corsi conserva il TODO del collega in `Sala.isAlreadyOccupiedAtFasciaOraria`:
il controllo calendario è ancora provvisorio e non fa parte delle US di Roby.
