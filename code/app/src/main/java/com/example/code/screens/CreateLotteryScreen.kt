package com.example.code.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.code.Campsite
import com.example.code.Lottery
import com.example.code.WaitingList
import com.example.code.components.BackButton
import com.example.code.data.SampleCampsites
import com.example.code.ui.theme.DarkBrown
import com.example.code.ui.theme.DeepSage
import com.example.code.ui.theme.PichusTheme
import com.google.firebase.Timestamp
import java.util.Date

/**
 * US 02.04.01, US 02.04.02, US 02.04.03, US 02.04.04, US 02.04.05, US 02.04.07:
 * As a campsite owner, I want to create a lottery, select multiple campsites (US 02.04.02),
 * set registration period (US 02.04.03), booking period (US 02.04.04), number of winners (US 02.04.05),
 * and optional capacity (US 02.04.07).
 */
@Composable
fun CreateLotteryScreen(
    ownerCampsites: List<Campsite>,
    ownerID: Int,
    existingLottery: Lottery?,
    onSaveLottery: (Lottery) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedCampsiteIDs by rememberSaveable {
        mutableStateOf(existingLottery?.campsiteIDs?.toSet() ?: emptySet())
    }

    var regDaysText by rememberSaveable { mutableStateOf("7") }
    var capacityText by rememberSaveable {
        mutableStateOf(existingLottery?.waitingList?.capacity?.toString() ?: "")
    }
    var winnersText by rememberSaveable {
        mutableStateOf(existingLottery?.numWinners?.toString() ?: "1")
    }
    var bookDaysText by rememberSaveable { mutableStateOf("7") }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        BackButton(
            onBack = onBack,
            modifier = Modifier.align(Alignment.Start)
        )

        Text(
            text = "CREATE LOTTERY",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Multiple Campsites Selection (US 02.04.02)
        Text(
            text = "Select Campsites for Lottery:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Start)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (ownerCampsites.isEmpty()) {
                    Text("No campsites registered yet.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    ownerCampsites.forEach { campsite ->
                        val isChecked = campsite.id in selectedCampsiteIDs
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedCampsiteIDs = if (isChecked) {
                                        selectedCampsiteIDs - campsite.id
                                    } else {
                                        selectedCampsiteIDs + campsite.id
                                    }
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    selectedCampsiteIDs = if (checked) {
                                        selectedCampsiteIDs + campsite.id
                                    } else {
                                        selectedCampsiteIDs - campsite.id
                                    }
                                }
                            )
                            Text(
                                text = campsite.name,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Registration Period (US 02.04.03)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Registration Period (Days from now):",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline
            )
            LotteryPillTextField(
                value = regDaysText,
                onValueChange = { regDaysText = it.filter { char -> char.isDigit() } },
                placeholderText = "#",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        // Optional Capacity (US 02.04.07)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Optional Capacity (Leave blank for unlimited):",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline
            )
            LotteryPillTextField(
                value = capacityText,
                onValueChange = { capacityText = it.filter { char -> char.isDigit() } },
                placeholderText = "#",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        // Number of Winners (US 02.04.05)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Number of Winners:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline
            )
            LotteryPillTextField(
                value = winnersText,
                onValueChange = { winnersText = it.filter { char -> char.isDigit() } },
                placeholderText = "#",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        // Booking Period (US 02.04.04)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Booking Period (Days from now):",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline
            )
            LotteryPillTextField(
                value = bookDaysText,
                onValueChange = { bookDaysText = it.filter { char -> char.isDigit() } },
                placeholderText = "#",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Button(
            onClick = {
                if (selectedCampsiteIDs.isEmpty()) {
                    errorMessage = "Please select at least one campsite for the lottery."
                    return@Button
                }
                val regDays = regDaysText.toIntOrNull()
                val bookDays = bookDaysText.toIntOrNull()
                val numWinners = winnersText.toIntOrNull()

                if ((regDays == null) || (regDays <= 0) || (bookDays == null) || (bookDays <= 0) || (numWinners == null) || (numWinners <= 0)) {
                    errorMessage = "Please enter valid positive numbers for periods and winners."
                    return@Button
                }

                val now = System.currentTimeMillis()
                val regEndMillis = now + (regDays.toLong() * 24 * 60 * 60 * 1000)
                val bookEndMillis = regEndMillis + (bookDays.toLong() * 24 * 60 * 60 * 1000)

                val lotteryID = existingLottery?.lotteryID ?: "lottery_${ownerID}_$now"
                val capacity = capacityText.toIntOrNull()

                val lottery = Lottery(
                    lotteryID = lotteryID,
                    ownerID = ownerID,
                    campsiteIDs = selectedCampsiteIDs.toList(),
                    registrationStart = Timestamp(Date(now)),
                    registrationEnd = Timestamp(Date(regEndMillis)),
                    bookingStart = Timestamp(Date(regEndMillis)),
                    bookingEnd = Timestamp(Date(bookEndMillis)),
                    numWinners = numWinners,
                    waitingList = WaitingList(
                        campsiteID = selectedCampsiteIDs.first(),
                        capacity = capacity,
                        registrationDeadline = Timestamp(Date(regEndMillis))
                    )
                )

                onSaveLottery(lottery)
            },
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = DeepSage,
                contentColor = MaterialTheme.colorScheme.onTertiary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .height(48.dp)
        ) {
            Text(
                text = "SAVE LOTTERY",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun LotteryPillTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholderText: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholderText,
                color = Color(0xFF8A7F6E)
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(50),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        ),
        keyboardOptions = keyboardOptions,
        modifier = modifier.fillMaxWidth()
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF4EFE3)
@Composable
private fun CreateLotteryScreenPreview() {
    PichusTheme {
        CreateLotteryScreen(
            ownerCampsites = SampleCampsites.all(),
            ownerID = 1,
            existingLottery = null,
            onSaveLottery = {},
            onBack = {}
        )
    }
}
