package it.univaq.grr.fitslot.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import it.univaq.grr.fitslot.domain.models.FasciaOraria
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class PreviewProvider(override val values: Sequence<FasciaOraria> = sequenceOf(
    FasciaOraria(1, LocalDateTime.parse("2027-06-01T15:30"), LocalDateTime.parse("2027-06-01T17:30")),
    FasciaOraria(2, LocalDateTime.parse("2027-06-01T18:30"), LocalDateTime.parse("2027-06-01T19:30")),
    FasciaOraria(3, LocalDateTime.parse("2027-06-02T12:30"), LocalDateTime.parse("2027-06-02T15:00"))
)
) : PreviewParameterProvider<FasciaOraria>{

}

@Preview
@Composable
fun FasciaOrariaCard(@PreviewParameter(PreviewProvider::class) fasciaOraria: FasciaOraria) {
    Card(
        Modifier.wrapContentHeight().width(226.dp).padding(5.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Row(
            Modifier.align(Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            orarioDiFasciaOrariaCard(fasciaOraria.orarioInizio)
            Icon(
                Icons.Default.KeyboardDoubleArrowRight,
                contentDescription = "dalle ore ${fasciaOraria.orarioInizio.hour} e ${fasciaOraria.orarioInizio.minute} alle ore ${fasciaOraria.orarioFine.hour} e ${fasciaOraria.orarioFine.minute}"
            )
            orarioDiFasciaOrariaCard(fasciaOraria.orarioFine)
        }
    }
}

@Composable
private fun orarioDiFasciaOrariaCard(orario: LocalDateTime) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH : mm")

    Card(
        Modifier.width(100.dp).padding(horizontal = 12.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
//        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    ) {
        Text(
            text = orario.format(timeFormatter),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 3.dp),
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}