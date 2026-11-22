# GRR-43 - Il mio certificato medico

Aggiornamento del 22 novembre: sostituiti foto e data mock con l'ultimo certificato dell'iscritto autenticato, letto dalla tabella e da MediaStorage della GRR-19.

La schermata mostra immagine e scadenza, avviso entro 20 giorni, avviso di certificato scaduto o messaggio se manca. Sono disponibili Ricarica e Apri documento. I PDF si aprono con l'app del sistema; non viene introdotto un lettore PDF interno.

La data salvata dal DatePicker in millisecondi UTC viene convertita da dataScadenza; si accettano anche date ISO. Le letture considerano solo l'utente autenticato, usando LocalStorage esistente. Nessuna modifica al login.

Due test brevi sulla finestra dei 20 giorni e sul certificato assente; la lettura del certificato reale e del rinnovo più recente è verificata in CertificatiReceptionTest.kt. Dettagli di integrazione, ricreazione del database e risultati: Completamento-US-Roby.md.

```bash
cd /Users/uni/IdeaProjects/FitSlot
./gradlew :desktopApp:compileKotlin :shared:jvmTest
```
