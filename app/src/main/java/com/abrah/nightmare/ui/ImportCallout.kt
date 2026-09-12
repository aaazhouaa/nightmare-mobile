package com.abrah.nightmare.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.abrah.nightmare.R

/**
 * ⭐⭐ "Bring something of your own in" — the one card shape every importer uses.
 *
 * ⚠⚠ **One definition, three callers.** Models, Flows and Settings each offer an
 * import, and they had drifted into three different things: a tinted callout
 * with a sentence and an Import button on Models, and on Flows a plain card with
 * two bare buttons labelled "Flow" and "Nodes" — labels that only parsed while
 * they sat side by side, and stopped meaning anything once each was alone.
 * Reported from the phone, 2026-09-11.
 *
 * ⚠ On Models and Flows the card is deliberately NOT the same surface as the
 * rows around it. An import is an ACTION sitting in a list of things; drawn
 * identically it reads as one more item — which is exactly how the Models one
 * went unnoticed at the bottom of its list. The outline and the tinted ground
 * say "this one is different" without shouting.
 *
 * ⚠ Settings → Community is the other case: the import sits in a stack of
 * the same informational cards, and a tinted outline there reads as a
 * different *kind* of card rather than an action. Pass [highlighted] false
 * so it shares `surfaceVariant` with the sections under it.
 *
 * @param warning shown in the error colour under [body], for an import whose
 *   consequence the user has to accept BEFORE tapping. Null for the ordinary
 *   case. ⚠ It lives here rather than in a doc because the only moment it is
 *   useful is the moment of the tap.
 */
@Composable
fun ImportCallout(
    title: String,
    body: String,
    onImport: () -> Unit,
    modifier: Modifier = Modifier,
    buttonLabel: String = stringResource(R.string.flows_import),
    enabled: Boolean = true,
    warning: String? = null,
    /**
     * ⚠ True on Models / Flows (tinted, outlined). False on Settings →
     * Community, where the card has to match the `surfaceVariant` sections
     * it sits with.
     */
    highlighted: Boolean = true,
) {
    Card(
        modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (highlighted) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        ),
        border = if (highlighted) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        } else {
            null
        },
    ) {
        Row(
            Modifier.fillMaxWidth().padding(if (highlighted) 12.dp else 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(if (highlighted) 2.dp else 4.dp),
            ) {
                Text(
                    title,
                    style = if (highlighted) {
                        MaterialTheme.typography.titleMedium
                    } else {
                        MaterialTheme.typography.labelLarge
                    },
                    fontWeight = FontWeight.Normal,
                )
                Text(
                    body,
                    style = LogTextStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                warning?.let {
                    Text(it, style = LogTextStyle, color = MaterialTheme.colorScheme.error)
                }
            }
            // ⚠ Default filled Button uses primary as the FILL — and in this
            // theme primary is the text colour, so the chip reads as a black
            // or white slab on Community while every other action uses
            // [nightmareButtonColors] (the same raised grey as the cards).
            Button(
                onClick = onImport,
                enabled = enabled,
                colors = nightmareButtonColors(),
            ) { Text(buttonLabel) }
        }
    }
}
