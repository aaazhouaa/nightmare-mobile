package com.abrah.nightmare.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.abrah.nightmare.R

/**
 * ⭐⭐ The top of every screen that sits OVER the canvas — Models, Flows and the
 * op harness.
 *
 * ⚠⚠ One composable rather than three headers, because three had already
 * drifted into three different answers to the same question: Models said
 * "Back", Flows said "Back to canvas" beside a second button, and the harness
 * drew an arrow icon. Same gesture, same destination, three shapes and two
 * type sizes — a user learns the corner, not the wording, and the corner was
 * the only thing they agreed on.
 *
 * ⭐ **A close icon, not a back arrow.** None of these is a step in a journey;
 * each is a panel over the canvas, and the canvas is always what is behind it.
 * An arrow promises history that does not exist — there is nowhere else to go
 * back to — where an ✕ says "put this away", which is what the tap does.
 *
 * ⚠ The icon stays in the SAME CORNER the canvas puts the control that opened
 * the screen, so the trip out and the trip back are one thumb position.
 */
@Composable
fun ScreenHeader(
    title: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    /** Shown just after the title — the harness puts its version here. */
    afterTitle: @Composable () -> Unit = {},
    /** Shown just before the ✕ — the harness puts its version here. */
    trailing: @Composable () -> Unit = {},
) {
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // ⚠⚠ The colour is EXPLICIT, and that is a bug fix. Models and Flows
        // set none, so their titles took `LocalContentColor`, which outside a
        // `Surface` is BLACK -- drawn on a 0xFF0B0B10 canvas. Both headlines
        // were very nearly invisible, on every build, and eleven goldens had
        // pinned them that way: a golden proves the pixels have not CHANGED,
        // never that they were right (docs/UI.md §5). Only the harness escaped,
        // because its header alone named a colour.
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            afterTitle()
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            trailing()
            IconButton(onClick = onClose) {
                Icon(
                    Icons.Filled.Close,
                    // ⚠ Names the DESTINATION, not the glyph. "close" tells a
                    // screen-reader user nothing they cannot already tell.
                    contentDescription = stringResource(R.string.cd_close_back, title),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * The ⓘ that opens the device sheet (HTP arch / VTCM). Sits with the "模型" tab
 * label (LibraryScreen) — the page that asks "will this checkpoint load on MY
 * phone".
 *
 * ⚠⚠ SIZED, and the ceiling is measured rather than guessed. Swept inside a
 * real `TabRow` (tabTextBox height, glyph box):
 *
 *      none  16dp  18dp  20dp  24dp  28dp |  32dp  36dp  40dp
 *       48    48    48    48    48    48  |   52    56    60   dp
 *
 * So anything up to 28dp leaves the row at its natural 48dp — and a plain
 * `IconButton` (40dp) grew it to 60dp, which is what pushed the label row down
 * and unbalanced the three tabs. `Icon` + `clickable` is what allows the size to
 * be chosen at all: material3 1.3.1's `IconButton` applies its own `size(40dp)`
 * and `minimumInteractiveComponentSize`, so a `size()` handed to it is ignored.
 *
 * ⚠ 18dp is the user's call (2026-09-17): the glyph reads as part of the LABEL,
 * not as a control parked beside it. It is well under the 48dp accessibility
 * minimum on purpose — the TAB itself is the 48dp target for switching tabs,
 * and this is a secondary action.
 */
@Composable
fun DeviceInfoButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Icon(
        Icons.Filled.Info,
        contentDescription = stringResource(R.string.cd_device_info),
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .size(18.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
    )
}
