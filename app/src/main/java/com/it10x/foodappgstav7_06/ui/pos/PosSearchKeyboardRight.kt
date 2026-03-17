package com.it10x.foodappgstav7_06.ui.pos

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun PosSearchKeyboardRight(
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onClose: () -> Unit,
    onMore: () -> Unit            // 🔹 NEW
) {

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val maxButtonsInRow = 14
    val totalSpacing = 6.dp * (maxButtonsInRow - 1)
    val calculatedWidth = (screenWidth - totalSpacing - 12.dp) / maxButtonsInRow
    val buttonWidth = minOf(calculatedWidth, 48.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {

        val letterRows = listOf(
            listOf("Q","W","E","R","T","Y","U","I","O","P"),
            listOf("A","S","D","F","G","H","J","K","L"),
            listOf("Z","X","C","V","B","N","M")
        )

        val numberColumns = listOf(
            listOf("1","2","3"),
            listOf("4","5","6"),
            listOf("7","8","9","0")
        )

        for (i in 0..2) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {

                // 🔹 LEFT SIDE LETTERS
                Row(
                    modifier = Modifier.weight(4f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {

                    letterRows[i].forEach { key ->
                        KeyButtonStyled(
                            label = key,
                            width = buttonWidth,
                            height = 42.dp
                        ) { onKeyPress(key) }
                    }

                    if (i == 2) {
                        // 🔥 Bottom row extra buttons
                        KeyButtonStyled("CLEAR", buttonWidth, 42.dp) { onClear() }
                        KeyButtonStyled("More", buttonWidth, 42.dp) { onMore() }   // 🔹 NEW
                        KeyButtonStyled("OK", buttonWidth, 42.dp) { onClose() }
                        KeyButtonStyled("⌫", buttonWidth, 42.dp) { onBackspace() }
                    }
                }

                // 🔹 RIGHT SIDE NUMBERS
                Row(
                    modifier = Modifier.weight(1.4f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    numberColumns[i].forEach { key ->
                        KeyButtonStyled(
                            label = key,
                            width = buttonWidth,
                            height = 42.dp
                        ) { onKeyPress(key) }
                    }
                }
            }
        }
    }
}






@Composable
fun KeyButtonStyled(
    label: String,
    width: Dp,           // 🔥 UPDATED: width instead of weight
    height: Dp = 44.dp,
    onClick: () -> Unit
) {

    val color = when (label) {
        "OK" -> Color(0xFF81C784)
        "CLEAR" -> Color(0xFFFFCDD2)
        "⌫" -> Color(0xFFBBDEFB)
        else -> Color(0xFFE0E0E0)
    }

    val textColor = when (label) {
        "OK" -> Color(0xFF1B5E20)
        "CLEAR" -> Color(0xFFB71C1C)
        "⌫" -> Color(0xFF0D47A1)
        else -> Color.Black
    }

    Button(
        onClick = onClick,
        modifier = Modifier
            .width(width)     // 🔥 FIXED WIDTH
            .height(height),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        contentPadding = PaddingValues(vertical = 6.dp),
        shape = MaterialTheme.shapes.small,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(label, color = textColor)
    }
}

