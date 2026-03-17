package com.it10x.foodappgstav7_06.com.it10x.foodappgstav7_06.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardHide
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- Key height presets ---
val KEY_HEIGHT_C_56 = 56.dp
val KEY_HEIGHT_C_64 = 64.dp
val KEY_HEIGHT_C_72 = 72.dp

// --- Font size presets ---
val FONT_C_18 = 18.sp
val FONT_C_22 = 22.sp
val FONT_C_26 = 26.sp

// --- Spacing presets ---
val SPACE_C_6 = 6.dp
val SPACE_C_8 = 8.dp
val SPACE_C_10 = 10.dp

@Composable
fun KeyboardCompactExt(
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onClose: () -> Unit,
    onMore: () -> Unit,
) {

    val keyHeight = KEY_HEIGHT_C_56
    val fontSize = FONT_C_22
    val spacing = SPACE_C_8

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {

            // LEFT SIDE
            Column(
                modifier = Modifier.weight(4f),
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {

                Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    listOf("Q","W","E","R","T","Y","U","I","O","P","-").forEach { key ->
                        KeyBigCompact(key, keyHeight, fontSize, Modifier.weight(1f)) {
                            onKeyPress(key)
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    listOf("A","S","D","F","G","H","J","K","L","@").forEach { key ->
                        KeyBigCompact(key, keyHeight, fontSize, Modifier.weight(1f)) {
                            onKeyPress(key)
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    listOf("Z","X","C","V","B","N","M","Spa",".","⌫").forEach { key ->
                        KeyBigCompact(key, keyHeight, fontSize, Modifier.weight(1f)) {
                            when (key) {
                                "⌫" -> onBackspace()
                                "Spa" -> onKeyPress(" ")
                                else -> onKeyPress(key)
                            }
                        }
                    }
                }
            }

            // RIGHT SIDE
            Column(
                modifier = Modifier.weight(1.3f),
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {

                Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    listOf("1","2","3","CLOSE").forEach { key ->
                        KeyBigCompact(key, keyHeight, fontSize, Modifier.weight(1f)) {
                            when (key) {
                                "CLOSE" -> onClose()
                                else -> onKeyPress(key)
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    listOf("4","5","6","DEL").forEach { key ->
                        KeyBigCompact(key, keyHeight, fontSize, Modifier.weight(1f)) {
                            when (key) {
                                "DEL" -> onClear()
                                else -> onKeyPress(key)
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    listOf("7","8","9","0").forEach { key ->
                        KeyBigCompact(key, keyHeight, fontSize, Modifier.weight(1f)) {
                            onKeyPress(key)
                        }

                    }
                }
            }
        }
    }
}

@Composable
fun KeyBigCompact(
    label: String,
    height: Dp,
    fontSize: TextUnit,
    modifier: Modifier,
    onClick: () -> Unit
) {

    val isNumber = label.all { it.isDigit() }

// 🎨 Colors
    val backgroundColor = when {
        label == "CLOSE" -> Color(0xFFD32F2F)   // 🔴 Red
        label == "DEL" -> Color(0xFFFFC107)     // 🟡 Yellow
        label == "⌫" -> Color(0xFF455A64)       // Dark gray
        label == "Spa" -> Color(0xFF4CAF50)     // 🟢 Green
        isNumber -> Color(0xFFE8F5E9)            // 💚 Very light green numbers
        else -> Color.White
    }

    val textColor = when {
        label == "CLOSE" -> Color.White
        label == "DEL" -> Color.Black
        label == "⌫" -> Color.White
        label == "Spa" -> Color.White
        else -> Color.Black
    }


    Button(
        onClick = onClick,
        modifier = modifier.height(height),
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor
        ),
        border = if (backgroundColor == Color.White)
            BorderStroke(1.dp, Color(0xFFE0E0E0))
        else null,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 1.dp,
            pressedElevation = 2.dp
        )
    ) {

        when (label) {

            "⌫" -> Icon(Icons.Default.Backspace, contentDescription = "Backspace")

            "DEL" -> Icon(Icons.Default.Clear, contentDescription = "Clear")

            "CLOSE" -> Icon(Icons.Default.KeyboardHide, contentDescription = "Close")

            "Spa" -> Text(
                text = "SP",
                fontSize = fontSize,
                fontWeight = FontWeight.Medium
            )

            else -> Text(
                text = label,
                fontSize = fontSize,
                fontWeight = if (isNumber) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
