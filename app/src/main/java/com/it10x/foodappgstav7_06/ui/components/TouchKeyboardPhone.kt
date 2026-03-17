package com.it10x.foodappgstav7_06.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardHide
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

/* =========================
   🔧 KEYBOARD SIZE PRESETS
   Comment what you don't want
   ========================= */

// --- Key height presets ---
val Phone_KEY_HEIGHT_56 = 56.dp
val Phone_KEY_HEIGHT_64 = 64.dp   // ✅ recommended default
val Phone_KEY_HEIGHT_72 = 72.dp

// --- Font size presets ---
val Phone_FONT_18 = 18.sp
val Phone_FONT_22 = 22.sp   // ✅ recommended default
val Phone_FONT_26 = 26.sp

// --- Spacing presets ---
val Phone_SPACE_6 = 6.dp
val Phone_SPACE_8 = 8.dp   // ✅ recommended default
val Phone_SPACE_10 = 10.dp

// --- Space key width presets ---
val Phone_SPACE_WEIGHT_SMALL = 1.5f
val Phone_SPACE_WEIGHT_MEDIUM = 2f   // ✅ recommended default
val Phone_SPACE_WEIGHT_BIG = 2.5f


@Composable
fun TouchKeyboardPhone(
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onClose: () -> Unit,
    onMore: () -> Unit
) {
    val isPhone = LocalConfiguration.current.screenWidthDp < 600
    // ✅ choose active preset here
    val keyHeight = if (isPhone) 44.dp else Phone_KEY_HEIGHT_56
    val fontSize = if (isPhone) 16.sp else Phone_FONT_22
    val spacing = if (isPhone) 4.dp else Phone_SPACE_8


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {

        // -------- Row 1 (Numbers) --------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            // 🔹 LEFT MAIN KEYS



            Column(
                modifier = Modifier.weight(4f),
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {

                // Row 1 (Numbers)
                Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    listOf("1","2","3","4","5","6","7","8","9").forEach {
                        keyBigPhoneAdv(it, keyHeight, fontSize, Modifier.weight(1f)) {
                            onKeyPress(it)
                        }
                    }
                }

                // Row 2
                Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    listOf("Q","W","E","R","T","Y","U","I").forEach { key ->
                        keyBigPhoneAdv(key, keyHeight, fontSize, Modifier.weight(1f)) {

                            when (key) {
                                "CLOSE" -> onClose()
                                else -> onKeyPress(key)
                            }

                        }
                    }
                }

                // Row 3
                Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    listOf("A","S","D","F","G","H","J","K","L").forEach { key ->
                        keyBigPhoneAdv(key, keyHeight, fontSize, Modifier.weight(1f)) {

                            when (key) {
                                "SPACE" -> onKeyPress(" ")
                                else -> onKeyPress(key)
                            }

                        }
                    }
                }


                // Row 4
                Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    listOf("Z","X","C","V","B","N","M").forEach {
                        keyBigPhoneAdv(it, keyHeight, fontSize, Modifier.weight(1f)) {
                            onKeyPress(it)
                        }
                    }


                }
                Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    listOf("O","P","0","SPACE","CLOSE").forEach { key ->
                        keyBigPhoneAdv(key, keyHeight, fontSize, Modifier.weight(1f)) {

                            when (key) {
                                "CLOSE" -> onClose()
                                "SPACE" -> onKeyPress(" ")
                                else -> onKeyPress(key)
                            }

                        }
                    }
                    keyBigPhoneAdv("⌫", keyHeight, fontSize, Modifier.weight(1f)) {
                        onBackspace()
                    }
                }
            }



        }


        // -------- Bottom Row --------

    }

}



@Composable
fun keyBigPhoneAdv(
    label: String,
    height: Dp,
    fontSize: androidx.compose.ui.unit.TextUnit,
    modifier: Modifier,
    onClick: () -> Unit
) {

    val isNumber = label.all { it.isDigit() }

    // 🎨 COLORS (Same Logic As Compact)
    val backgroundColor = when {
        label == "CLOSE" -> Color(0xFFD32F2F)   // 🔴 Red
        label == "DEL" -> Color(0xFFFFC107)     // 🟡 Yellow
        label == "⌫" -> Color(0xFF455A64)       // Dark gray
        label == "SPACE" -> Color(0xFF4CAF50)   // 🟢 Green
        isNumber -> Color(0xFFE8F5E9)            // Light green numbers
        else -> Color.White
    }

    val textColor = when {
        label == "CLOSE" -> Color.White
        label == "DEL" -> Color.Black
        label == "⌫" -> Color.White
        label == "SPACE" -> Color.White
        else -> Color.Black
    }

    Button(
        onClick = onClick,
        modifier = modifier.height(height),
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor
        ),
        border = if (backgroundColor == Color.White)
            BorderStroke(1.dp, Color(0xFFE0E0E0))
        else null,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 1.dp
        )
    ) {

        when (label) {

            "⌫" -> Icon(
                imageVector = Icons.Default.Backspace,
                contentDescription = "Backspace"
            )

            "DEL" -> Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Clear"
            )

            "CLOSE" -> Icon(
                imageVector = Icons.Default.KeyboardHide,
                contentDescription = "Close"
            )

            "SPACE" -> Text(
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







