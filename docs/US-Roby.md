# US di Roby: sale, abbonamenti e piani a punti

## Avvio

Dalla directory `/Users/uni/IdeaProjects/FitSlot`:

```bash
./gradlew :desktopApp:run
```

L'applicazione usa un unico database in `~/.fitslot/fitslot.db`.
Login, password, sessione e registrazione sono di competenza delle US del team.
I file di login, UserDao, UserRepository e PasswordSecurity coincidono con main.
Non viene aggiunta alcuna configurazione del primo gestore.
L'accesso come gestore è una precondizione delle US di Roby: il collegamento
dei permessi verrà concordato con chi implementa l'autenticazione. La Home attuale
del team è una schermata di prova con collegamenti alle diverse funzionalità.

`FITSLOT_DB_PATH` permette di usare un database separato per le prove.
I database sperimentali creati in passato dai colleghi come `fitslot.db`
nella directory di lavoro non vengono importati o cancellati automaticamente.

## GRR-14

Creazione della sala con nome e capienza, salvataggio e conferma.
Il collegamento al ruolo del gestore è demandato all'integrazione con il team.

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
- Il controllo del gestore non viene reimplementato nelle US dei cataloghi: va integrato con il team.

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
iscritti, chiavi esterne, validazione dei prezzi e migrazione. I test Compose verificano le azioni
dei moduli. I test di login e password sono quelli del collega, mantenuti invariati.

Il lavoro dei corsi conserva il TODO del collega in `Sala.isAlreadyOccupiedAtFasciaOraria`:
il controllo calendario è ancora provvisorio e non fa parte delle US di Roby.

## GitHub Actions

La cartella `.github` proviene da main, dove è stata integrata la GRR-12.
Il workflow esegue i test sulle PR verso main e sui push a main, conserva i report
del team ed esegue i test UI con Xvfb sul runner Linux. Viene verificata anche la
compilazione desktop. I report Pages rimangono limitati ai push su main.
