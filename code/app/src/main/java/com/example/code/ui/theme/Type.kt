package com.example.code.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * App typography. Matches project part 2's design system:
 * Screen titles = heavy serif
 * Everything else = clean sans serif
 *
 * Text style rules:
 * - titleLarge = screen titles (in all caps)
 * - titleMedium = card headings
 * - bodyLarge = default body text
 * - bodyMedium = secondary text
 * - bodySmall = captions or fine print
 * - labelLarge = button text
 */

val TitleFamily = FontFamily.Serif
val BodyFamily = FontFamily.SansSerif
val Typography = Typography(
    // Screen titles in all caps
    titleLarge = TextStyle(
        fontFamily = TitleFamily,
        fontWeight = FontWeight.Black,
        fontSize = 24.sp,
        letterSpacing = 0.sp,
        color = TitleBrown
    ),
    // Card headings
    titleMedium = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        letterSpacing = 0.5.sp,
        color = DarkBrown
    ),
    // Default body text
    bodyLarge = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    // Secondary text
    bodyMedium = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = MutedBrown
    ),
    // captions and fine print
    bodySmall = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = MutedBrown
    ),
    // Button text
    labelLarge = TextStyle(
        fontFamily = BodyFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        letterSpacing = 1.sp
    )
)