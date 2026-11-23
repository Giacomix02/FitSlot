# GRR-95 - Sale incluse in un abbonamento

Branch indipendente GRR-95, aggiornato con pull di main al commit 92ecd2b il 20 novembre. Il nuovo commit del collega sostituisce il DAO simulato nei test utenti con SQLite in memoria. Ripristinate le modifiche locali senza conflitti. Le PR #10 (GRR-43) e #11 (GRR-19) sono ancora aperte e non sono integrate in questo branch.

## Funzionamento

Il gestore apre “Gestisci abbonamenti” dalla Home. In alto “Aggiungi abbonamento” apre il popup con nome, prezzo e durata. Anche “Modifica” apre il modulo nel popup; il salvataggio riuscito lo chiude, mentre un errore lascia i campi disponibili per correggerli.

Ogni abbonamento della lista ha “Assegna sale”: apre un popup dedicato all’abbonamento scelto, con selezione della sala e riepilogo delle sale incluse. “Associa sala” aggiorna il riepilogo e mostra la conferma. La stessa coppia non può essere inserita due volte. Le sale vengono ricaricate ad ogni apertura del popup. Non ci sono una pagina separata di associazione o un pulsante dedicato nella Home.

Si riutilizzano DropDownSelector del team, AlertDialog e il CatalogoViewModel esistente senza modificarlo. Il solo ViewModel dell’associazione carica sale e riepilogo quando viene selezionato un abbonamento. I piani a punti mantengono la loro pagina separata e il modulo attuale.

Il perimetro è associazione e riepilogo, come nel criterio della US. Non sono aggiunti rimozione delle associazioni, vendita di abbonamenti, controlli agli ingressi o modifiche ai piani a punti.

## Dati e integrazione

La nuova tabella sale_abbonamenti contiene abbonamentoId e salaId come chiave composta. Le chiavi esterne richiedono sala e abbonamento esistenti. La sala inclusa non può essere eliminata; il controllo delle sale è stato aggiornato per considerare anche gli abbonamenti oltre ai corsi. Quando si elimina un abbonamento senza iscritti, vengono rimossi i suoi collegamenti alle sale, non le sale stesse. Il blocco preesistente per piani con iscritti rimane.

AppDatabase passa dalla versione 1 alla 2. MIGRAZIONE_SALE_ABBONAMENTI crea solo la tabella e l’indice, preservando le tabelle precedenti. I vecchi helper MIGRAZIONE_1_2 e MIGRAZIONE_2_3 sono storici e non vengono registrati: partivano da una diversa struttura del progetto. La nuova migrazione parte dallo schema attuale di main. Le modifiche dello schema vanno coordinate con quelle della GRR-19 prima dell’integrazione: la PR #11 aggiunge CertificatoMedicoEntity mantenendo version = 1. Quando entrerà in main, occorrerà allineare entità, versione e migrazione alla struttura effettivamente integrata.

Prima di provare l’app su dati personali, conservare una copia del database a programma chiuso: il main attuale usa ancora lo schema 1 e non può riaprire automaticamente un file aggiornato allo schema 2. Le verifiche della migrazione sono state fatte su un file temporaneo, senza aggiornare il database personale in desktopApp.

## File

Root: `/Users/uni/IdeaProjects/FitSlot`. I file Kotlin seguenti sono sotto `shared/src/commonMain/kotlin/it/univaq/grr/fitslot/`.

Nuovi: `data/local/entity/SalaAbbonamentoEntity.kt`, `data/local/MigrazioneSaleAbbonamenti.kt`, `ui/screens/associazioneSale/AssociazioneSaleDialog.kt` e `AssociazioneSaleViewModel.kt`.

Modificati: `data/local/AppDatabase.kt`, `data/local/dao/CatalogoDao.kt`, `data/local/dao/SalaDao.kt`, `domain/repository/CatalogoRepository.kt`, `data/repository/CatalogoRepositoryImpl.kt`, `ui/screens/HomeScreen.kt`, `ui/navigator/NavHost.kt`, `ui/screens/catalogo/CatalogoScreen.kt`.

I ViewModel precedenti, il login, la registrazione e il codice dei corsi non sono stati modificati.

## Verifica

Due test essenziali in `/Users/uni/IdeaProjects/FitSlot/shared/src/jvmTest/kotlin/it/univaq/grr/fitslot/SaleAbbonamentiTest.kt`: associazione con riepilogo e duplicati, protezione della sala e cancellazione del collegamento insieme al piano.

```bash
cd /Users/uni/IdeaProjects/FitSlot
./gradlew :desktopApp:compileKotlin :shared:jvmTest
```

Verifica ripetuta dopo il pull del 20 novembre: compilazione riuscita; suite con 28 casi rilevati, 27 superati, 1 già saltato del collega. Gradle segnala inoltre un disallineamento tra versioni Compose/Skiko; compilazione e test terminano comunque correttamente. La GRR-43 non è in questo branch.

Verifica aggiuntiva su database temporaneo: apertura dello schema 1 tramite Room, migrazione alla versione 2, conservazione di sala e abbonamento preesistenti e nuova associazione. Prova desktop isolata: creazione del nuovo ViewModel dentro NavHost, caricamento, selezioni, associazione e aggiornamento del riepilogo. Queste prove temporanee non aggiungono test grafici complessi al repository.

Per provare l’interfaccia usare il gestore già disponibile nel proprio database. Se non ci sono sale o abbonamenti, crearli prima nelle rispettive gestioni.

Aggiornamento interfaccia: compilazione e suite ripetute con esito positivo (27 superati, 1 già saltato). I test verificano la logica e il database; i popup non sono coperti da test grafici automatici.
