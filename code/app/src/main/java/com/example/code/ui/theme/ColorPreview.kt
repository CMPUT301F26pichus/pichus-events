package com.example.code.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Preview of the pichus palette, fonts, buttons and colors. Just for reference.
 */
@Preview(showBackground = true, backgroundColor = 0xFFF4EFE3)
@Composable
fun PichusPalettePreview() {
    PichusTheme {
        Column(Modifier.padding(16.dp)) {
            Text("PICHUS PALETTE", style = Typography.titleLarge)
            Swatch("TitleBrown", TitleBrown)
            Swatch("CreamBg", CreamBg)
            Swatch("SageCard", SageCard)
            Swatch("PichusGreen", PichusGreen)
            Swatch("GreenDark", GreenDark)
            Swatch("DarkBrown", DarkBrown)
            Swatch("MutedBrown", MutedBrown)
            Swatch("ButtonCream", ButtonCream)
            Swatch("PichusRed", PichusRed)
            Text("Title style", style = Typography.titleLarge)
            Text("Card heading style", style = Typography.titleMedium)
            Text("Body text style", style = Typography.bodyLarge)
            Text("Secondary text style", style = Typography.bodyMedium)
            Text("Caption style", style = Typography.bodySmall)

            // The four button kinds (see the rules comment in Theme.kt)
            Button(onClick = {}) { Text("CONFIRM") }
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                )
            ) { Text("CANCEL RESERVATION") }
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) { Text("POLICY AND GUIDELINES") }
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) { Text("DELETE ACCOUNT") }
        }
    }
}

@Composable
private fun Swatch(name: String, color: Color) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(40.dp)
                .background(color, RoundedCornerShape(8.dp))
        )
        Text("  $name", style = Typography.bodyLarge)
    }
}