package com.it10x.foodappgstav7_06.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/* =========================
   🔧 KEYBOARD SIZE PRESETS
   Comment what you don't want
   ========================= */

// --- Key height presets ---
val KEY_HEIGHT_56 = 56.dp
val KEY_HEIGHT_64 = 64.dp   // ✅ recommended default
val KEY_HEIGHT_72 = 72.dp

// --- Font size presets ---
val FONT_18 = 18.sp
val FONT_22 = 22.sp   // ✅ recommended default
val FONT_26 = 26.sp

// --- Spacing presets ---
val SPACE_6 = 6.dp
val SPACE_8 = 8.dp   // ✅ recommended default
val SPACE_10 = 10.dp

// --- Space key width presets ---
val SPACE_WEIGHT_SMALL = 1.5f
val SPACE_WEIGHT_MEDIUM = 2f   // ✅ recommended default
val SPACE_WEIGHT_BIG = 2.5f


@Composable
fun PosTouchKeyboard(
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onClose: () -> Unit,
    onMore: () -> Unit
) {

    // ✅ choose active preset here
    val keyHeight = KEY_HEIGHT_56
    val fontSize = FONT_22
    val spacing = SPACE_8
    val spaceWeight = SPACE_WEIGHT_MEDIUM

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {

        // -------- Row 1 (Numbers) --------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {

            // 🔹 LEFT MAIN KEYS



            Column(
                modifier = Modifier.weight(4f),
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {

                // Row 1 (Numbers)
                Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    listOf("1","2","3","4","5","6","7","8","9","0").forEach {
                        KeyBig(it, keyHeight, fontSize, Modifier.weight(1f)) {
                            onKeyPress(it)
                        }
                    }
                }

                // Row 2
                Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    listOf("Q","W","E","R","T","Y","U","I","O","P").forEach {
                        KeyBig(it, keyHeight, fontSize, Modifier.weight(1f)) {
                            onKeyPress(it)
                        }
                    }
                }

                // Row 3
                Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    listOf("A","S","D","F","G","H","J","K","L").forEach {
                        KeyBig(it, keyHeight, fontSize, Modifier.weight(1f)) {
                            onKeyPress(it)
                        }
                    }
                }

                // Row 4
                Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    listOf("Z","X","C","V","B","N","M",".").forEach {
                        KeyBig(it, keyHeight, fontSize, Modifier.weight(1f)) {
                            onKeyPress(it)
                        }
                    }

                    KeyBig("⌫", keyHeight, fontSize, Modifier.weight(1f)) {
                        onBackspace()
                    }
                }
            }

            // 🔹 RIGHT ACTION COLUMN
            Column(
                modifier = Modifier.weight(0.5f),
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {
                KeyBig("CLOSE", keyHeight, fontSize, Modifier.fillMaxWidth()) {
                    onClose()
                }



                KeyBig("More.", keyHeight, fontSize, Modifier.fillMaxWidth()) {
                    onMore()
                }
                KeyBig("DEL", keyHeight, fontSize, Modifier.fillMaxWidth()) {
                    onClear()
                }

                KeyBig("SPACE", keyHeight, fontSize, Modifier.fillMaxWidth()) {
                    onKeyPress(" ")
                }


            }

        }


        // -------- Bottom Row --------

    }

}



@Composable
fun KeyBig(
    label: String,
    height: Dp,
    fontSize: androidx.compose.ui.unit.TextUnit,
    modifier: Modifier,
    onClick: () -> Unit
) {

    val isNumber = label.all { it.isDigit() }

    val backgroundColor = when (label) {
        "CLOSE" -> androidx.compose.ui.graphics.Color(0xFF43A047)
        "DEL" -> androidx.compose.ui.graphics.Color(0xFFD32F2F)
        else -> androidx.compose.ui.graphics.Color.White
    }

    val textColor = when (label) {
        "CLOSE", "DEL" -> androidx.compose.ui.graphics.Color.White
        else -> androidx.compose.ui.graphics.Color.Black
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .height(if (label == "SPACE") height + 4.dp else height),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp), // 🔥 subtle professional radius
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor
        ),
        border = if (backgroundColor == androidx.compose.ui.graphics.Color.White)
            androidx.compose.foundation.BorderStroke(
                1.dp,
                androidx.compose.ui.graphics.Color(0xFFE0E0E0)
            )
        else null,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 1.dp,
            pressedElevation = 2.dp
        )
    ) {
        Text(
            text = label,
            fontSize = fontSize,
            fontWeight = if (isNumber)
                androidx.compose.ui.text.font.FontWeight.Bold
            else
                androidx.compose.ui.text.font.FontWeight.Medium
        )
    }
}





//@Composable
//fun KeyBig(
//    label: String,
//    height: Dp,
//    fontSize: androidx.compose.ui.unit.TextUnit,
//    modifier: Modifier,
//    onClick: () -> Unit
//) {
//
//    val isNumber = label.all { it.isDigit() }
//
//    val backgroundColor = when (label) {
//        "OK" -> androidx.compose.ui.graphics.Color(0xFF43A047)      // professional green
//        "CLEAR" -> androidx.compose.ui.graphics.Color(0xFFD32F2F)   // deep red
//        else -> androidx.compose.ui.graphics.Color.White
//    }
//
//    val textColor = when (label) {
//        "OK", "CLEAR" -> androidx.compose.ui.graphics.Color.White
//        else -> androidx.compose.ui.graphics.Color.Black
//    }
//
//    Button(
//        onClick = onClick,
//        modifier = modifier
//            .height(
//                if (label == "SPACE") height + 4.dp else height
//            ),
//        colors = ButtonDefaults.buttonColors(
//            containerColor = backgroundColor,
//            contentColor = textColor
//        ),
//        border = if (backgroundColor == androidx.compose.ui.graphics.Color.White)
//            androidx.compose.foundation.BorderStroke(
//                1.dp,
//                androidx.compose.ui.graphics.Color(0xFFE0E0E0)   // subtle gray border
//            )
//        else null,
//        elevation = ButtonDefaults.buttonElevation(
//            defaultElevation = 1.dp,
//            pressedElevation = 2.dp
//        )
//    ) {
//        Text(
//            text = label,
//            fontSize = fontSize,
//            fontWeight = if (isNumber)
//                androidx.compose.ui.text.font.FontWeight.Bold  // 🔥 numbers stronger
//            else
//                androidx.compose.ui.text.font.FontWeight.Medium
//        )
//    }
//}


//@Composable
//fun KeyBig(
//    label: String,
//    height: Dp,
//    fontSize: androidx.compose.ui.unit.TextUnit,
//    modifier: Modifier,
//    onClick: () -> Unit
//) {
//
//    val backgroundColor = when (label) {
//        "OK" -> androidx.compose.ui.graphics.Color(0xFF4CAF50)      // green confirm
//        "CLEAR" -> androidx.compose.ui.graphics.Color(0xFFE53935)  // red clear
//        else -> androidx.compose.ui.graphics.Color.White            // 🔥 default white
//    }
//
//    val textColor = when (label) {
//        "OK", "CLEAR" -> androidx.compose.ui.graphics.Color.White
//        else -> androidx.compose.ui.graphics.Color.Black
//    }
//
//    Button(
//        onClick = onClick,
//        modifier = modifier.height(height),
//        colors = ButtonDefaults.buttonColors(
//            containerColor = backgroundColor,
//            contentColor = textColor
//        ),
//        elevation = ButtonDefaults.buttonElevation(0.dp)
//    ) {
//        Text(label, fontSize = fontSize)
//    }
//}

