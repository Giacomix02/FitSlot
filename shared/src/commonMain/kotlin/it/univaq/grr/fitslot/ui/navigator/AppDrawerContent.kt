package it.univaq.grr.fitslot.ui.navigator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Loyalty
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Room
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import it.univaq.grr.fitslot.Screens
import it.univaq.grr.fitslot.domain.models.Ruolo
import it.univaq.grr.fitslot.domain.models.User
import it.univaq.grr.fitslot.ui.components.NavigationDrawerItem
import it.univaq.grr.fitslot.utils.EnvRetriever

@Composable
fun AppDrawerContent(
    user: User?,
    navController: NavController,
    onClose: () -> Unit,
    onLogout: () -> Unit
) {
    ModalDrawerSheet {
        fun navigate(screen: Screens) {
            navController.navigate(screen) { launchSingleTop = true }
            onClose()
        }

        fun testing(): Boolean {
            return EnvRetriever.get("TESTING") == "true"
        }

        // Header
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = MaterialTheme.shapes.large,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Utente",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .padding(10.dp)
                            .size(28.dp),
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = user?.let { "${it.nome} ${it.cognome}" } ?: "Ospite",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = user?.ruolo?.value ?: "Non autenticato",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    )
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            //******************** ISCRITTO ********************//
            if (user?.ruolo == Ruolo.ISCRITTO || testing()) {
                NavigationDrawerItem(
                    label = "Il mio certificato medico",
                    icon = Icons.Filled.MedicalServices,
                    onClick = { navigate(Screens.CertificatoMedico) },
                )
            }

            //******************** GESTORE ********************//
            if (user?.ruolo == Ruolo.GESTORE || testing()) {
                NavigationDrawerItem(
                    label = "Gestisci sale",
                    icon = Icons.Filled.Domain,
                    onClick = { navigate(Screens.CreazioneSala) },
                )
                NavigationDrawerItem(
                    label = "Gestisci abbonamenti",
                    icon = Icons.Filled.CardMembership,
                    onClick = { navigate(Screens.Abbonamenti) },
                )
                NavigationDrawerItem(
                    label = "Gestisci piani a punti",
                    icon = Icons.Filled.Loyalty,
                    onClick = { navigate(Screens.PianiAPunti) },
                )
                NavigationDrawerItem(
                    label = "Registra un nuovo utente",
                    icon = Icons.Filled.PersonAdd,
                    onClick = { navigate(Screens.Registration) },
                )
                NavigationDrawerItem(
                    label = "Gestisci Fascie orarie",
                    icon = Icons.Filled.Alarm,
                    onClick = { navigate(Screens.FasceOrarieScreen) },
                )
                NavigationDrawerItem(
                    label = "Crea Corsi",
                    icon = Icons.Filled.Book,
                    onClick = { navigate(Screens.CorsiScreen) },
                )
            }

            //******************** RECEPTIONIST ********************//
            if (user?.ruolo == Ruolo.RECEPIONIST || testing()) {
                NavigationDrawerItem(
                    label = "Iscritti e certificati",
                    icon = Icons.Filled.People,
                    onClick = { navigate(Screens.CertificatiReception) },
                )
                NavigationDrawerItem(
                    label = "Registrazione accesso",
                    icon = Icons.Filled.Badge,
                    onClick = { navigate(Screens.LogAccesso) },
                )
            }

            //******************** NESSUN UTENTE ********************//
            if (user == null) {
                NavigationDrawerItem(
                    label = "Login",
                    icon = Icons.AutoMirrored.Filled.Login,
                    onClick = { navigate(Screens.Login) },
                )
                NavigationDrawerItem(
                    label = "Registrati",
                    icon = Icons.Filled.PersonAdd,
                    onClick = { navigate(Screens.Registration) },
                )
            } else {
                //******************** TUTTI GLI UTENTI DOPO IL LOGIN********************//
                NavigationDrawerItem(
                    label = "Logout",
                    icon = Icons.AutoMirrored.Filled.Logout,
                    onClick = onLogout,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}