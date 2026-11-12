# US di Roby: sale, abbonamenti e piani a punti

## Avvio

Dalla directory `/Users/uni/IdeaProjects/FitSlot`:

```bash
./gradlew :desktopApp:run
```

L'applicazione usa un unico database in `~/.fitslot/fitslot.db`.
Login, password, sessione e registrazione rimangono di competenza del team e i relativi
file non sono modificati da questo completamento. Il login esistente salva un oggetto
User alla chiave "user" di LocalStorage. PermessiGestioneDaLocalStorage legge quel ruolo
senza creare, modificare o cancellare sessioni.

Il gestore vede sulla Home Gestisci sale, Gestisci abbonamenti e Gestisci piani a punti.
Le tre destinazioni rifiutano l'accesso agli altri utenti e i repository ricontrollano
il ruolo prima di ogni inserimento, modifica o eliminazione. Mancanza di sessione o
ruolo diverso da GESTORE produce un rifiuto. Non esiste una nuova schermata di login
o di creazione del gestore.

## GRR-14

Elenco delle sale salvate, creazione, modifica di nome e capienza e cancellazione con
conferma. La modifica mantiene l'ID della sala, quindi non cambia i riferimenti dei corsi.
La capienza deve essere intera e maggiore di zero, il nome obbligatorio.

L'eliminazione viene bloccata se esiste un corso con salaAssegnataId corrispondente.
Il controllo è parte dell'istruzione DELETE ed è eseguito in transazione con la gestione
dell'esito: non dipende solo dal pulsante. Il messaggio mostrato è:
"Impossibile eliminare la sala: è assegnata a uno o più corsi."

Annullare modifica o eliminazione non modifica il database. Gli errori mantengono i dati
del modulo. Una sala assegnata a un corso può essere rinominata o cambiare capienza:
non vengono inventate regole sulle prenotazioni, assenti nel modello attuale.

Il controllo copre i collegamenti realmente presenti oggi (tabella corso). Eventuali
future tabelle di prenotazioni o associazioni sala-abbonamento andranno aggiunte ai
controlli nel momento in cui saranno introdotte. Il codice dei corsi del collega
non viene modificato.

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
- Il ruolo è letto dalla sessione esistente del team, senza reimplementare il login.
- Anche i repository delle tre US verificano GESTORE prima di ogni scrittura.

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

## Database

Questo completamento non cambia tabelle o schema: aggiunge query e operazioni sulle
tabelle già presenti in main. Non introduce nuove migrazioni e non modifica il builder
del database condiviso. I file e i test storici delle migrazioni restano come in main;
il test storico saltato non rappresenta una verifica eseguita da questa modifica.

## Verifiche

```bash
./gradlew :desktopApp:compileKotlin :shared:jvmTest
```

I test SQLite usano database temporanei o in memoria, non il database personale.
Coprono CRUD dei due cataloghi, duplicati in creazione/modifica, piano con
iscritti, chiavi esterne e validazione dei prezzi. I nuovi test coprono anche CRUD sale, collegamenti ai corsi e permessi. I test Compose verificano le azioni
dei moduli, la conferma e l'annullamento dell'eliminazione, il blocco della sala usata,
l'assenza delle funzioni per gli iscritti e il rifiuto di una route se il ruolo è cambiato.
I test di login e password sono quelli del collega, mantenuti invariati.

I dati utente impostati nei nostri test esistono solo nel processo di test e vengono
rimossi dopo ciascuna prova. I test non accedono al database personale.

Il lavoro dei corsi conserva il TODO del collega in `Sala.isAlreadyOccupiedAtFasciaOraria`:
il controllo calendario è ancora provvisorio e non fa parte delle US di Roby.

## GitHub Actions

La cartella `.github` proviene da main, dove è stata integrata la GRR-12.
Il workflow esegue i test sulle PR verso main e sui push a main, conserva i report
del team ed esegue i test UI con Xvfb sul runner Linux. Viene verificata anche la
compilazione desktop. I report Pages rimangono limitati ai push su main.
