package com.abrah.nightmare.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp

import com.abrah.nightmare.canvas.CanvasColors
import com.abrah.nightmare.canvas.DarkCanvasPalette
import com.abrah.nightmare.canvas.LightCanvasPalette

/**
 * ⚠ Dynamic color is deliberately absent, not forgotten (docs/UI.md section 1).
 * A wallpaper-derived palette makes every install look like a different app, and
 * node-category colors must be stable across devices or a shared screenshot
 * stops meaning anything.
 *
 * EVERY slot is set explicitly -- the Material baseline is lavender, and any
 * slot left to its default (secondary, tertiary, inverse*, surfaceDim, ...)
 * leaks purple into the UI.
 *
 * ⚠ The PRIMARY is the TEXT colour, not a hue (Material You mono style):
 * nav selection, sliders, switches, radios and buttons all read from
 * primary, so a purple there would repaint every control. The canvas keeps
 * its own selection purple (CanvasColors) -- that one is semantic.
 */
// ── Dark: bg #121212, card #1E1E1E, text #E4E4E7 ─────────────────────────
private val DarkBg = Color(0xFF121212)
private val DarkCard = Color(0xFF1E1E1E)
private val DarkOn = Color(0xFFE4E4E7)
private val DarkOnMuted = Color(0xFFA1A1AA)   // secondary text, same grey family
private val DarkOutline = Color(0xFF2E2E32)
private val DarkRaised = Color(0xFF2A2A2E)    // selected/pressed containers

private val NightmareDark = darkColorScheme(
    primary = DarkOn,
    onPrimary = DarkBg,
    primaryContainer = Color(0xFF2A2A2E),
    onPrimaryContainer = DarkOn,
    secondary = DarkOnMuted,
    onSecondary = DarkBg,
    secondaryContainer = DarkRaised,
    onSecondaryContainer = DarkOn,
    tertiary = DarkOnMuted,
    onTertiary = DarkBg,
    tertiaryContainer = DarkRaised,
    onTertiaryContainer = DarkOn,
    background = DarkBg,
    onBackground = DarkOn,
    surface = DarkBg,
    onSurface = DarkOn,
    // ⚠ Must NOT equal the card colour (#1E1E1E). CardDefaults uses
    // contentColorFor(container), which maps a colour equal to
    // surfaceVariant onto onSurfaceVariant -- titles inside every Card
    // would then render in the muted grey instead of onSurface.
    surfaceVariant = DarkRaised,
    onSurfaceVariant = DarkOnMuted,
    // Cards, sheets and elevated controls all sit on the card level; the
    // two-level spec has no per-elevation ramp, so every container is the
    // same #1E1E1E and `outline` does the separating.
    surfaceContainer = DarkCard,
    surfaceContainerLow = DarkCard,
    surfaceContainerHigh = DarkCard,
    surfaceContainerHighest = DarkCard,
    surfaceContainerLowest = DarkBg,
    surfaceDim = DarkBg,
    surfaceBright = DarkCard,
    surfaceTint = Color.Transparent,
    inverseSurface = DarkOn,
    inverseOnSurface = DarkBg,
    inversePrimary = Color(0xFF1D1D1D),
    outline = DarkOutline,
    outlineVariant = DarkOutline,
    error = Color(0xFFFF6B6B),
    onError = DarkBg,
    errorContainer = Color(0xFF3A1A1A),
    onErrorContainer = Color(0xFFFFB4AB),
    scrim = Color(0xFF000000),
)

// ── Light: bg #F7F8FA, card #FFFFFF, text #1D1D1D ────────────────────────
private val LightBg = Color(0xFFF7F8FA)
private val LightCard = Color(0xFFFFFFFF)
private val LightOn = Color(0xFF1D1D1D)
private val LightOnMuted = Color(0xFF6B6B70)
private val LightOutline = Color(0xFFE4E4E8)
private val LightRaised = Color(0xFFE9EAEE)

private val NightmareLight = lightColorScheme(
    primary = LightOn,
    onPrimary = LightCard,
    primaryContainer = LightRaised,
    onPrimaryContainer = LightOn,
    secondary = LightOnMuted,
    onSecondary = LightCard,
    secondaryContainer = LightRaised,
    onSecondaryContainer = LightOn,
    tertiary = LightOnMuted,
    onTertiary = LightCard,
    tertiaryContainer = LightRaised,
    onTertiaryContainer = LightOn,
    background = LightBg,
    onBackground = LightOn,
    surface = LightBg,
    onSurface = LightOn,
    // Same trap as dark: Card contentColorFor(surfaceVariant) is
    // onSurfaceVariant. Keep surfaceVariant off the card colour so titles
    // inside a Card stay onSurface (#1D1D1D) and only captions stay muted.
    surfaceVariant = LightRaised,
    onSurfaceVariant = LightOnMuted,
    surfaceContainer = LightCard,
    surfaceContainerLow = LightCard,
    surfaceContainerHigh = LightCard,
    surfaceContainerHighest = LightCard,
    surfaceContainerLowest = LightCard,
    surfaceDim = LightBg,
    surfaceBright = LightCard,
    surfaceTint = Color.Transparent,
    inverseSurface = LightOn,
    inverseOnSurface = LightBg,
    inversePrimary = Color(0xFFE4E4E7),
    outline = LightOutline,
    outlineVariant = LightOutline,
    error = Color(0xFFB3261E),
    onError = LightCard,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    scrim = Color(0xFF000000),
)

/** The system font for anything an agent or a human reads as a measurement. */
val LogTextStyle = TextStyle(fontFamily = FontFamily.Default, fontSize = 12.sp)

/**
 * Slider colours shared by every knob in the app (inspector panels only, as
 * of the 2026-09 pass). All knobs draw at 69% opacity — thumb and active
 * track in the text colour, the inactive track in the card colour — so a
 * slider states itself without shouting over the node it tweaks.
 */
@Composable
fun nightmareSliderColors(): SliderColors = SliderDefaults.colors(
    thumbColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.69f),
    activeTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.69f),
    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.69f),
)

/**
 * Filled Button colours matching the home-tab "模型" chip: secondaryContainer
 * fill (#E9EAEE light / #2A2A2E dark) and onSecondaryContainer text. Replaces
 * the default primary fill (#1D1D1D in light, #E4E4E7 in dark) so every
 * filled action reads the same as the nav selection.
 */
@Composable
fun nightmareButtonColors(): ButtonColors = ButtonDefaults.buttonColors(
    containerColor = MaterialTheme.colorScheme.secondaryContainer,
    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NightmareTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // The canvas palette follows the theme too: the palette swap happens here,
    // once per theme change, and every DrawScope reader sees the new values.
    CanvasColors.apply(if (darkTheme) DarkCanvasPalette else LightCanvasPalette)
    MaterialTheme(
        colorScheme = if (darkTheme) NightmareDark else NightmareLight,
        typography = Typography(),
    ) {
        // ⚠⚠ Android 12+ stretch overscroll is the Compose default. Pulling
        // past the end of a list rubber-bands the whole page and then sticks
        // at both edges -- reported from the phone. Null here drops the
        // effect app-wide; glow is gone too, which is the lesser cost.
        CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
            content()
        }
    }
}

/**
 * ⭐ The star that keeps a picture in Results.
 *
 * ⚠⚠ **A colour, not a second glyph.** `material-icons-core` has no outlined
 * star and the extended set costs ~55 MB of dex for one, so the KEPT state is
 * carried by tint instead — amber when kept, grey when not. Asked for from the
 * phone, 2026-09-11.
 *
 * ⚠ Fixed values rather than theme roles: this pair must read the same on the
 * light canvas, the dark canvas and the black fullscreen viewer, and a scheme
 * colour would drift between them.
 */
val StarKept = androidx.compose.ui.graphics.Color(0xFFFFC107)
val StarIdle = androidx.compose.ui.graphics.Color(0xFF9E9E9E)
