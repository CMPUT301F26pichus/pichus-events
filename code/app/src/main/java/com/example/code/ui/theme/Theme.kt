package com.example.code.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Pichus app theme. Maps Project Part 2's design system onto material 3 color roles.
 * Dynamic color intentionally disabled so app always uses pichus color palette instead of device default/dark
 *
 * Button rules (to keep every screen consistent):
 * - primary (green) = positive/forward actions: JOIN LOTTERY, CONTINUE, CONFIRM, REGISTER, UPDATE, etc.
 * - tertiary (dark brown) = back out/negative actions: DECLINE, CANCEL RESERVATION, LEAVE WAITLIST, REMOVE
 * - error (red) = irreversible actions only: DELETE ACCOUNT
 * - secondaryContainer (beige) = neutral pills, tabs, input fields
 *
 */
private val PichusColorScheme = lightColorScheme(
    primary = PichusGreen, // main positive buttons & actions
    onPrimary = ButtonCream, // text on green buttons
    secondary = GreenDark, // deeper green accents
    onSecondary = ButtonCream,
    tertiary = DarkBrown, // back-out/negative action buttons
    onTertiary = ButtonCream, // text on dark brown buttons
    background = CreamBg, // cream app background
    onBackground = DarkBrown, // default text
    surface = CreamBg, // screens
    onSurface = DarkBrown,
    surfaceVariant = SageCard, // cards and list items
    onSurfaceVariant = DarkBrown,
    secondaryContainer = BeigePill, // pills, tabs, input fields
    onSecondaryContainer = PillText,
    outline = MutedBrown, // borders or secondary text
    error = PichusRed, // irreversible destructive actions
    onError = ButtonCream // text on red buttons

)

/**
 * Wraps app's UI in pichus material 3 theme (colors and typography)
 * Use this at the root of every screen
 */
@Composable
fun PichusTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PichusColorScheme,
        typography = Typography,
        content = content
    )
}