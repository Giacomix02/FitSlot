# Capitolo 2: Struttura del Progetto FitSlot e Guida "Dove Metto Questo Codice?"

Uno dei problemi più grandi per chi lavora in team o per chi è alle prime armi non è la sintassi di Kotlin, ma l'**organizzazione del codice**:
- *"In quale cartella creo questo file?"*
- *"Posso importare questa classe da un'altra parte?"*
- *"Dove scrivo la logica di controllo di un orario o di un campo di testo?"*

In questo capitolo esaminiamo l'esatta struttura ad albero del repository **FitSlot** e forniamo un albero decisionale per rispondere a ogni dubbio.

---

## 1. La Mappa Completa delle Cartelle di FitSlot

Ecco l'albero effettivo dei file del nostro progetto:

```
FitSlot/
├── desktopApp/                                  ◄── Applicazione eseguibile Desktop
│   └── src/main/kotlin/it/univaq/grr/fitslot/
│       └── main.kt                              ◄── Punto di ingresso main() e finestra Window
│
├── shared/                                      ◄── Il codice condiviso dell'applicazione
│   └── src/
│       ├── jvmMain/kotlin/.../fitslot/          ◄── Codice SPECIFICO Desktop (JVM)
│       │   └── data/local/DatabaseBuilder.kt    ◄── Costruzione del percorso file .db sul PC
│       │
│       └── commonMain/kotlin/.../fitslot/       ◄── Codice COMUNE (95% del progetto)
│           ├── App.kt                           ◄── Composable radice dell'applicazione
│           │
│           ├── domain/                          ◄── IL CUORE DELL'APP (Zero librerie esterne)
│           │   ├── models/                      ◄── Data class pure (Workout, Slot, User)
│           │   └── repository/                  ◄── Interfacce astratte (WorkoutRepository)
│           │
│           ├── data/                            ◄── LA GESTIONE DEI DATI (Room 3)
│           │   ├── local/
│           │   │   ├── AppDatabase.kt           ◄── Classe astratta del database Room
│           │   │   ├── entity/                  ◄── Tabelle SQLite (@Entity)
│           │   │   ├── dao/                     ◄── Query SQL e firme (@Dao)
│           │   │   └── converter/               ◄── Convertitori di tipo (@ColumnTypeConverter)
│           │   ├── mapper/                      ◄── Funzioni di traduzione (Entity <-> Domain)
│           │   └── repository/                  ◄── Implementazioni concrete (WorkoutRepositoryImpl)
│           │
│           ├── ui/                              ◄── L'INTERFACCIA GRAFICA (Compose Multiplatform)
│           │   ├── theme/                       ◄── Colori, font, FitSlotTheme
│           │   ├── components/                  ◄── Elementi riutilizzabili (Bottoni, Card, Dialog)
│           │   ├── navigator/                   ◄── Routing e navigazione tra schermate
│           │   └── screens/                     ◄── Schermate divise per funzionalità
│           │       ├── workout/                 ◄── WorkoutListScreen.kt, WorkoutViewModel.kt
│           │       └── slot/                    ◄── SlotScreen.kt, SlotViewModel.kt
│           │
│           └── utils/                           ◄── FUNZIONI DI SUPPORTO E UTILITY
│               ├── TimeUtils.kt                 ◄── Formattatori minuti -> "10:30", calcolo durate
│               └── ValidationUtils.kt           ◄── Controllo validità campi e input
```

---

## 2. Guida Decisionale: "Dove Devo Mettere Questo?"

Quando devi scrivere una nuova classe o funzione, consulta questa tabella:

| Cosa devi creare? | In quale cartella va? | Esempio concreto |
|---|---|---|
| Una nuova tabella da salvare nel database SQLite | `data/local/entity/` | `SlotEntity.kt`, `UserEntity.kt` |
| Le query SQL per leggere e scrivere su una tabella | `data/local/dao/` | `SlotDao.kt`, `UserDao.kt` |
| Una data class pura che rappresenta un concetto di business | `domain/models/` | `Workout.kt`, `Slot.kt` |
| Una funzione che trasforma un'Entity in un modello di Domain | `data/mapper/` | `WorkoutMapper.kt` (`fun WorkoutEntity.toDomain()`) |
| Un'interfaccia che dichiara cosa si può fare con i dati | `domain/repository/` | `WorkoutRepository.kt` |
| La classe che implementa il Repository usando il DAO | `data/repository/` | `WorkoutRepositoryImpl.kt` |
| Una classe che tiene lo stato di una schermata e risponde ai click | `ui/screens/<feature>/` | `WorkoutViewModel.kt` |
| Il codice grafico `@Composable` di una schermata intera | `ui/screens/<feature>/` | `WorkoutListScreen.kt` |
| Un componente grafico usato in più schermate (es. un bottone personalizzato o una dialog di conferma) | `ui/components/` | `FitSlotButton.kt`, `ConfirmDeleteDialog.kt` |
| La gestione del cambio pagina (avanti, indietro, schermata corrente) | `ui/navigator/` | `Navigator.kt`, `Screen.kt` |
| La tavolozza dei colori dell'applicazione o lo stile del testo | `ui/theme/` | `Color.kt`, `Type.kt`, `Theme.kt` |
| Funzioni generiche di formattazione (es. trasformare minuti in "10:30") o calcolo date | `utils/` | `TimeUtils.kt` |
| Codice che tocca direttamente le API di Windows/Mac/Linux (es. trovare la cartella `user.home`) | `shared/src/jvmMain/...` | `DatabaseBuilder.jvm.kt` |

---

## 3. Le Regole di Dipendenza: Chi Può Importare Chi?

L'errore più comune nei progetti accademici e nei team agili è l'**intreccio disordinato degli import** (il cosiddetto "codice spaghetti").

Per mantenere l'app pulita, manutenibile e pronta per il futuro, seguiamo la **Matrice delle Dipendenze**:

```
           ┌──────────┐
           │    UI    │ (Conosce Domain, MA NON Data)
           └────┬─────┘
                │
                ▼
           ┌──────────┐
           │  DOMAIN  │ (NON CONOSCE NESSUNO: Zero dipendenze esterne!)
           └────▲─────┘
                │
                │
           ┌────┴─────┐
           │   DATA   │ (Conosce Domain per implementarne le interfacce)
           └──────────┘
```

### Le 4 Regole Fondamentali:

#### ❌ Regola 1: La cartella `domain` non conosce NESSUNO
Nella cartella `domain/` è severamente **vietato** importare:
- Classi da `data/` o annotazioni di Room (`@Entity`, `@Dao`, `@Database`).
- Classi da `ui/` o annotazioni di Compose (`@Composable`, `Modifier`, `Color`).
- *Perché?* Il dominio rappresenta la logica di business pura della palestra: deve poter funzionare sia con SQLite, sia con un'API online, sia senza alcuna interfaccia grafica!

#### ❌ Regola 2: La cartella `ui` non importa MAI da `data/local`
Nelle tue schermate Compose o nei tuoi ViewModel:
- **NON** importare mai `WorkoutEntity` o `WorkoutDao`.
- Importa sempre i modelli del dominio: `Workout` e `WorkoutRepository`.
- *Perché?* Se modifichi una tabella del database o una colonna SQL, l'interfaccia grafica non deve rompersi né deve essere ricompilata.

#### ❌ Regola 3: La cartella `data` non tocca MAI la `ui`
Nelle classi di database e repository non deve esserci alcuna traccia di Compose o ViewModel.

#### ✅ Regola 4: `utils/` contiene solo codice puro di supporto
Le funzioni di utility devono essere indipendenti e accettare solo tipi base (Int, String, Long) o al massimo modelli del `domain`.

---

## 4. Domande Frequenti dei Principianti

### D1: *"Dove metto la logica di validazione di un input?"*
> **Risposta:**
> Dipende dal tipo di validazione:
> 1. **Validazione formale/sintattica** (es: *"L'utente ha inserito lettere in un campo che richiede solo numeri?"* oppure *"Il campo è vuoto?"*): gestiscila nel **ViewModel** o in `utils/ValidationUtils.kt` prima di chiamare il repository.
> 2. **Regola di business complessa** (es: *"Uno slot non può sovrapporsi a un altro slot nello stesso orario per la stessa sala"*): gestiscila nel **Repository** o in un apposito **UseCase** nel layer `domain`.

### D2: *"Posso mettere il ViewModel nella cartella `domain`?"*
> **Risposta:**
> **No.** Il `ViewModel` appartiene al layer `ui/screens/<feature>/`. Il suo scopo è preparare i dati per lo schermo Compose e gestire lo stato grafico (`StateFlow<UiState>`).

### D3: *"Se ho un enum (es. `DayOfWeek` o `DifficultyLevel`), dove lo metto?"*
> **Risposta:**
> Se è un concetto che fa parte delle regole di FitSlot, mettilo in `domain/models/`. Entrambi gli strati (`data` per salvarlo come numero/stringa e `ui` per visualizzarlo) potranno usarlo.

### D4: *"Perché abbiamo `commonMain` e `jvmMain`?"*
> **Risposta:**
> - `commonMain`: contiene codice che non usa librerie esclusive di Java o Windows. Questo garantisce che lo stesso codice possa girare in futuro su Android o iOS senza cambiare una virgola.
> - `jvmMain`: contiene l'1% di codice che ha bisogno di accedere al file system del PC desktop tramite le classi `java.io.File`.

---

## 5. Riepilogo del Capitolo

- Il progetto è suddiviso nei tre layer puliti: **`domain`** (regole e modelli puri), **`data`** (Room 3, mapper e persistenza) e **`ui`** (schermate Compose, ViewModel e temi).
- La cartella `utils/` ospita funzioni pure di estensione e calcolo.
- Il rispetto della matrice delle dipendenze garantisce che modifiche al database non rompano la grafica e viceversa.

Nel prossimo capitolo uniremo tutti i fili, tracciando passo dopo passo il **Flusso Completo del Dato: da SQLite alla Schermata Compose**.
