# GRR-43 - Visualizzazione del proprio certificato medico

Base del lavoro: pull di main al commit 38d1c47, che comprende la PR #9 e la correzione del login ripetuto del team.

L’iscritto autenticato trova in Home “Il mio certificato medico”. La pagina mostra il documento di esempio e la scadenza. Se mancano al massimo 20 giorni, mostra l’avviso. Se il certificato non è presente, mostra il messaggio dedicato. Il gestore e gli altri ruoli non vedono il pulsante; la pagina controlla nuovamente il ruolo.

## Mock e perimetro

Come indicato nella descrizione della US, foto e data sono mock. Il documento è una risorsa di esempio con la scritta “MOCK - NON VALIDO”. La data predefinita è 20 giorni dopo la creazione del ViewModel, così il caso dell’avviso è visibile nella demo. Non viene caricato un documento reale, non viene interrogata una tabella certificati e non si salva alcun dato medico. Il collegamento alla GRR-19 resta da concordare con il collega.

La lettura dell’utente usa LocalStorage già esistente. Nessuna modifica al login, ai repository o al database. I soli file condivisi modificati sono HomeScreen.kt e ui/navigator/NavHost.kt, per pulsante e destinazione.

La US richiede l’avviso a 20 giorni: qui si mostra nell’intervallo da 20 giorni prima fino al giorno della scadenza incluso. Un certificato già scaduto non attiva questo avviso di prossima scadenza. Non è il controllo di ingresso della GRR-77.

## Codice e test

Tutti i percorsi partono da `/Users/uni/IdeaProjects/FitSlot`.

- Schermata e ViewModel: `shared/src/commonMain/kotlin/it/univaq/grr/fitslot/ui/screens/certificatoMedico/`.
- Immagine di esempio: `shared/src/commonMain/composeResources/drawable/certificato_mock.svg`.
- Due test semplici: `shared/src/jvmTest/kotlin/it/univaq/grr/fitslot/CertificatoMedicoTest.kt`.

I test sono in jvmTest perché usano java.time.LocalDate, come le fasce orarie del team. Usano date fisse e non dipendono dal giorno di esecuzione.

```bash
cd /Users/uni/IdeaProjects/FitSlot
./gradlew :shared:jvmTest --tests 'it.univaq.grr.fitslot.CertificatoMedicoTest'
```

Verifica completa:

```bash
cd /Users/uni/IdeaProjects/FitSlot
./gradlew :desktopApp:compileKotlin :shared:jvmTest
```

Avvio:

```bash
cd /Users/uni/IdeaProjects/FitSlot
./gradlew :desktopApp:run
```

Per provare manualmente la pagina serve un account ISCRITTO, non il gestore. Il caso predefinito mostra certificato e avviso. Per provare la mancanza del documento si può passare alla schermata un CertificatoMedicoViewModel con certificatoPresente = false. Per una scadenza lontana si può passare una data oltre 20 giorni. Sono parametri del mock per le prove, non comandi aggiunti alla schermata dell’utente.

## Esito della verifica locale

Compilazione desktop riuscita. Suite: 28 casi rilevati, 27 superati e 1 già saltato del collega. I due test della GRR-43 sono superati. Un’anteprima isolata è stata avviata con dati mock senza accesso al database personale, ma la finestra Java non è stata ispezionabile tramite lo strumento di controllo: la verifica visiva e il percorso Home-login con un account iscritto restano da confermare manualmente.

Correzione dell’apertura: il ViewModel è creato esplicitamente con `viewModel { CertificatoMedicoViewModel() }`, come nelle altre destinazioni del progetto. La factory automatica desktop non supportava la creazione usata inizialmente. L’apertura della schermata è stata poi verificata in una destinazione NavHost con dati mock, senza passare il ViewModel dall’esterno.
