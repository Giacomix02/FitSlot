package it.univaq.grr.fitslot.ui.screens.catalogo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import it.univaq.grr.fitslot.domain.models.Abbonamento
import it.univaq.grr.fitslot.ui.screens.associazioneSale.AssociazioneSaleDialog
import it.univaq.grr.fitslot.ui.screens.associazioneSale.AssociazioneSaleViewModel
import it.univaq.grr.fitslot.ui.screens.associazioneCorsi.AssociazioneCorsiDialog
import it.univaq.grr.fitslot.ui.screens.associazioneCorsi.AssociazioneCorsiViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import it.univaq.grr.fitslot.utils.formattaPrezzo

/** Mostra il catalogo e il modulo di creazione o modifica del piano. */
@Composable
fun CatalogoScreen(
    tipo: TipoCatalogo,
    stato: CatalogoUiState,
    onNome: (String) -> Unit,
    onPrezzo: (String) -> Unit,
    onQuantita: (String) -> Unit,
    onSalva: () -> Unit,
    onNuovo: () -> Unit,
    onModifica: (VoceCatalogo) -> Unit,
    onElimina: (VoceCatalogo) -> Unit,
    onConfermaEliminazione: () -> Unit,
    onAnnullaEliminazione: () -> Unit,
    onIndietro: () -> Unit,
    saleViewModel: AssociazioneSaleViewModel? = null,
    corsiViewModel: AssociazioneCorsiViewModel? = null
) {
    val abbonamenti = tipo == TipoCatalogo.ABBONAMENTI
    val abilitato = !stato.operazioneInCorso
    var mostraModulo by remember { mutableStateOf(false) }
    var mostraCorsi by remember { mutableStateOf(false) }
    var mostraSale by remember { mutableStateOf(false) }
    // Il popup si chiude solo quando il salvataggio è riuscito.
    LaunchedEffect(stato.conferma) {
        if (stato.conferma != null) mostraModulo = false
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            TextButton(onClick = onIndietro, enabled = abilitato) { Text("Torna alla Home") }
            Text(if (abbonamenti) "Abbonamenti" else "Piani a punti", style = MaterialTheme.typography.headlineMedium)
            Text(if (abbonamenti) "Ingressi illimitati per la durata dell'abbonamento."
                else "Ogni punto corrisponde a un ingresso incluso nel piano.")
        }
        if (abbonamenti) {
            item {
                Button(onClick = { onNuovo(); mostraModulo = true }, enabled = abilitato) {
                    Text("Aggiungi abbonamento")
                }
            }
        } else {
        item { Text(if (stato.idModifica == null) "Nuovo piano" else "Modifica piano", style = MaterialTheme.typography.titleLarge) }
        item { OutlinedTextField(stato.nome, onNome, label = { Text("Nome") }, singleLine = true, enabled = abilitato, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(stato.prezzo, onPrezzo, label = { Text("Prezzo (€)") }, singleLine = true, enabled = abilitato, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(stato.quantita, onQuantita, label = { Text(if (abbonamenti) "Durata in giorni" else "Ingressi inclusi") }, singleLine = true, enabled = abilitato, modifier = Modifier.fillMaxWidth()) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onSalva, enabled = abilitato) {
                    Text(if (!abilitato) "Operazione in corso..." else if (stato.idModifica == null) "Crea piano" else "Salva modifiche")
                }
                if (stato.idModifica != null) TextButton(onClick = onNuovo, enabled = abilitato) { Text("Annulla modifica") }
            }
        }
        }
        item {
            val errore = stato.errore
            if (errore != null) {
                Text(errore, color = MaterialTheme.colorScheme.error)
            }
            val conferma = stato.conferma
            if (conferma != null) {
                Text(conferma)
            }
        }
        item {
            Text("Piani sottoscrivibili", style = MaterialTheme.typography.titleLarge)
            if (stato.caricamento) Text("Caricamento...")
            else if (stato.elenco.isEmpty()) Text("Non ci sono piani. Aggiungi il primo.")
        }
        items(stato.elenco, key = { it.id }) { voce ->
            OutlinedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(voce.nome, style = MaterialTheme.typography.titleMedium)
                    Text("${formattaPrezzo(voce.prezzoCentesimi)} €")
                    Text(if (abbonamenti) "${voce.quantita} giorni · ingressi illimitati" else "${voce.quantita} ingressi")
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextButton(onClick = { onModifica(voce); if (abbonamenti) mostraModulo = true }, enabled = abilitato) { Text("Modifica") }
                        TextButton(onClick = { onElimina(voce) }, enabled = abilitato) { Text("Elimina") }
                        if (abbonamenti && saleViewModel != null) {
                            TextButton(onClick = {
                                saleViewModel.scegliAbbonamento(Abbonamento(voce.id, voce.nome, voce.prezzoCentesimi, voce.quantita))
                                mostraSale = true
                            }) { Text("Assegna sale") }
                        }
                        if (abbonamenti && corsiViewModel != null) {
                            TextButton(onClick = {
                                corsiViewModel.scegliAbbonamento(Abbonamento(voce.id, voce.nome, voce.prezzoCentesimi, voce.quantita))
                                mostraCorsi = true
                            }) { Text("Assegna corsi") }
                        }
                    }
                }
            }
        }
    }
    if (mostraModulo && abbonamenti) {
        AlertDialog(
            onDismissRequest = { if (abilitato) { mostraModulo = false; onNuovo() } },
            title = { Text(if (stato.idModifica == null) "Aggiungi abbonamento" else "Modifica abbonamento") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(stato.nome, onNome, label = { Text("Nome") }, singleLine = true, enabled = abilitato)
                    OutlinedTextField(stato.prezzo, onPrezzo, label = { Text("Prezzo (€)") }, singleLine = true, enabled = abilitato)
                    OutlinedTextField(stato.quantita, onQuantita, label = { Text("Durata in giorni") }, singleLine = true, enabled = abilitato)
                    stato.errore?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = { TextButton(onClick = onSalva, enabled = abilitato) { Text("Salva") } },
            dismissButton = { TextButton(onClick = { mostraModulo = false; onNuovo() }, enabled = abilitato) { Text("Annulla") } }
        )
    }
    if (mostraSale && saleViewModel != null) {
        AssociazioneSaleDialog(saleViewModel) { mostraSale = false }
    }
    if (mostraCorsi && corsiViewModel != null) {
        AssociazioneCorsiDialog(corsiViewModel) { mostraCorsi = false }
    }
    val voce = stato.daEliminare
    if (voce != null) {
        AlertDialog(
            onDismissRequest = { if (abilitato) onAnnullaEliminazione() },
            title = { Text("Elimina piano") },
            text = { Text("Vuoi eliminare “${voce.nome}”? L'operazione sarà bloccata se sono presenti iscritti.") },
            confirmButton = { TextButton(onClick = onConfermaEliminazione, enabled = abilitato) { Text("Elimina") } },
            dismissButton = { TextButton(onClick = onAnnullaEliminazione, enabled = abilitato) { Text("Annulla") } }
        )
    }
}
