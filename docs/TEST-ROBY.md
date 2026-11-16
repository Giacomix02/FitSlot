# Studiare ed eseguire i test delle US di Roby

Progetto: `/Users/uni/IdeaProjects/FitSlot`.

## Dove si trovano

- Logica comune: `/Users/uni/IdeaProjects/FitSlot/shared/src/commonTest/kotlin/it/univaq/grr/fitslot/PrezzoTest.kt`.
- Database: `/Users/uni/IdeaProjects/FitSlot/shared/src/jvmTest/kotlin/it/univaq/grr/fitslot/CatalogoDatabaseTest.kt` e `GestioneSaleTest.kt`.
- Le schermate si provano manualmente: i test grafici di Roby sono stati rimossi per ridurre il codice da studiare.

`commonTest` contiene test che non richiedono API specifiche Java/JVM. `jvmTest` contiene quelli che richiedono l'ambiente JVM, anche se sono scritti in Kotlin. In questo progetto il task `jvmTest` esegue anche i test di `commonTest`: non serve un comando `commonTest` separato.

## Da IntelliJ

1. Apri uno dei file indicati sopra.
2. Premi il triangolo verde accanto al nome di un test per eseguire solo quel caso.
3. Premi quello accanto alla classe per eseguire tutti i casi di quella classe.
4. Nel pannello Run controlla il risultato. Verde indica riuscito, rosso fallito. Un test saltato non è un test superato.

## Dal terminale

Ogni sequenza parte dalla directory del progetto.

Solo i test semplici del prezzo:

```bash
cd /Users/uni/IdeaProjects/FitSlot
./gradlew :shared:jvmTest --tests 'it.univaq.grr.fitslot.PrezzoTest'
```

Solo il caso della conversione:

```bash
cd /Users/uni/IdeaProjects/FitSlot
./gradlew :shared:jvmTest --tests 'it.univaq.grr.fitslot.PrezzoTest.conversioneDelPrezzo'
```

Solo i test del database delle sale:

```bash
cd /Users/uni/IdeaProjects/FitSlot
./gradlew :shared:jvmTest --tests 'it.univaq.grr.fitslot.GestioneSaleTest'
```

Solo i test dei cataloghi:

```bash
cd /Users/uni/IdeaProjects/FitSlot
./gradlew :shared:jvmTest --tests 'it.univaq.grr.fitslot.CatalogoDatabaseTest'
```

Compilazione desktop e tutti i test:

```bash
cd /Users/uni/IdeaProjects/FitSlot
./gradlew :desktopApp:compileKotlin :shared:jvmTest
```

Su GitHub Actions il team usa Xvfb per fornire un display virtuale ai test grafici Linux. Sul Mac usa la normale sessione desktop.

## Come leggere il risultato

`BUILD SUCCESSFUL` indica che i task richiesti sono riusciti. `UP-TO-DATE` indica che Gradle ha riutilizzato risultati precedenti perché gli input non sono cambiati. Per chiedere una nuova esecuzione dei test:

```bash
cd /Users/uni/IdeaProjects/FitSlot
./gradlew :shared:jvmTest --rerun-tasks
```

Il report completo è `/Users/uni/IdeaProjects/FitSlot/shared/build/reports/tests/jvmTest/index.html`. Mostra conteggio, test saltati ed eventuali errori. Un'esecuzione con `--tests` produce un report dei soli test selezionati.

## I concetti minimi

- `@Test`: identifica una prova.
- `assertEquals(atteso, ottenuto)`: confronta due valori.
- `assertTrue`: controlla che una condizione sia vera.
- `assertNull`: controlla che non ci sia un valore.
- `assertFailsWith<TipoErrore> { operazione }`: controlla che l’operazione venga rifiutata con l’errore previsto, come nei test dei colleghi.
- `@BeforeTest`: prepara i dati prima di ogni test.
- `@AfterTest`: chiude il database e pulisce l'utente dopo ogni test.
- `runBlocking<Unit>`: permette al test JVM di chiamare le funzioni `suspend`; `Unit` specifica che il test non restituisce un risultato.
- `first()`: legge il primo valore emesso dal Flow; nei test serve a controllare l'elenco attuale.

Un test si legge in tre passi: preparo i dati, eseguo l'azione, controllo il risultato. Per esempio, inserisco una sala e un corso collegato, provo a eliminare la sala e controllo che sia ancora presente.

## Scelte della semplificazione

I ViewModel usano controlli `if` espliciti e non forzano i numeri nullable con `!!`. Ogni salvataggio/eliminazione contiene la propria coroutine, con errore e fine dell'attesa visibili nello stesso metodo. La cancellazione della coroutine va rilanciata: non è un errore dell'utente da mostrare nella schermata.

Le due schermate dei cataloghi restano condivise perché cambiano solo tipo e campi del piano. Room, Flow e Compose restano necessari per il progetto: non vengono sostituiti da un secondo modo di salvare dati o aggiornare l'interfaccia.

Sono rimasti sei test delle US di Roby: uno sul prezzo, due sulle sale e tre sui cataloghi. I test database provano creazione, modifica, eliminazione, nomi duplicati e blocchi dell’eliminazione. Sono esempi essenziali: non verificano ogni valore, ruolo o percorso delle schermate.

Non ci sono più i test UI di Roby o il suo test sui permessi. I controlli dell’app non sono stati rimossi. Le schermate si provano creando, modificando ed eliminando una sala e un piano per ciascun catalogo. I test dei colleghi restano invariati.

I test non definiscono funzioni dentro altre funzioni e non usano helper generici personalizzati. Usano `assertFailsWith` come quelli dei colleghi: il blocco contiene solo l’operazione che deve fallire. `runBlocking` serve a chiamare le operazioni sospendibili del database. La factory di Room segue la stessa forma dei test dei corsi.

Lo stile comune usa funzioni con nomi espliciti, controlli `if`, `viewModelScope.launch` per le operazioni sospendibili, `StateFlow` e `copy` per lo stato, repository e DAO per i dati. Login e corsi hanno alcune scelte diverse tra loro: non si riscrivono i file dei colleghi per uniformarli. Non vengono introdotti wrapper o framework nuovi.
