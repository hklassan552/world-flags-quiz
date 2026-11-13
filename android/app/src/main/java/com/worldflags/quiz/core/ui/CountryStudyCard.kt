package com.worldflags.quiz.core.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.worldflags.quiz.R
import com.worldflags.quiz.core.theme.AtlasPurple
import com.worldflags.quiz.domain.model.Country

@Composable
fun CountryStudyCard(country: Country, bits: Int, expanded: Boolean, onToggle: () -> Unit) {
    val expansion = stringResource(if (expanded) R.string.expanded else R.string.collapsed)
    AtlasCard(Modifier.semantics { stateDescription = expansion }) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FlagImage(country.id, stringResource(R.string.flag_of, countryName(country.id)), 62.dp, 42.dp)
            Column(Modifier.weight(1f)) {
                Text(countryName(country.id), style = MaterialTheme.typography.titleMedium, color = AtlasPurple)
                Text(capitalName(country.id), style = MaterialTheme.typography.bodyMedium)
            }
        }
        Text(knowledgeLabel(bits), style = MaterialTheme.typography.labelSmall)
        if (expanded) {
            FlagImage(country.id, stringResource(R.string.flag_of, countryName(country.id)), 192.dp, 112.dp)
            Text(capitalFact(country), style = MaterialTheme.typography.bodyLarge)
            Text(continentName(country.continent), style = MaterialTheme.typography.bodyMedium)
            Text(flagDescription(country.id), style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.fact_status, stringResource(R.string.flag_label), stringResource(if (bits and 1 != 0) R.string.learned else R.string.not_started)), style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.fact_status, stringResource(R.string.capital_label), stringResource(if (bits and 2 != 0) R.string.learned else R.string.not_started)), style = MaterialTheme.typography.bodyMedium)
        }
        SecondaryButton(stringResource(if (expanded) R.string.close else R.string.details), onToggle)
    }
}
