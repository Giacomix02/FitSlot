# GRR-95 - Sale incluse in un abbonamento

Il gestore apre “Gestisci abbonamenti” dalla Home. “Aggiungi abbonamento” apre il modulo nel popup; anche la modifica usa un popup. Ogni abbonamento ha “Assegna sale”: un dropdown con checkbox permette di selezionare più sale e “Associa sale” le salva insieme. Compare il riepilogo aggiornato. Le sale già incluse sono disabilitate; il salvataggio aggiunge quelle nuove senza rimuovere le precedenti.

La tabella sale_abbonamenti usa la coppia abbonamentoId/salaId come chiave, senza duplicati. La sala inclusa non può essere eliminata. Eliminare un abbonamento libero rimuove solo i collegamenti alle sale, non le sale stesse. La GRR-94 usa un popup analogo per i corsi, nella stessa gestione abbonamenti. I piani a punti restano separati.

La versione del database è 3. Per scelta del team non sono registrate migrazioni né cancellazioni automatiche: dopo un cambiamento dello schema si ricrea manualmente il database di prova a programma chiuso. Percorso rilevato e istruzioni in Completamento-US-Roby.md. Questa operazione perde i dati locali, da reinserire; il gestore predefinito viene inizializzato dal codice esistente.

Test brevi in /Users/uni/IdeaProjects/FitSlot/shared/src/jvmTest/kotlin/it/univaq/grr/fitslot/SaleAbbonamentiTest.kt: associazione e riepilogo, duplicati, protezione della sala, associazioni multiple e corsi. Suite complessiva: 37 superati, 1 già disattivato.

```bash
cd /Users/uni/IdeaProjects/FitSlot
./gradlew :desktopApp:compileKotlin :shared:jvmTest
```
