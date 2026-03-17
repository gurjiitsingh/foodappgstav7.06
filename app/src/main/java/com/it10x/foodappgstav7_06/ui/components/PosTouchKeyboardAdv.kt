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
val _KEY_HEIGHT_56 = 56.dp
val _KEY_HEIGHT_64 = 64.dp   // ✅ recommended default
val _KEY_HEIGHT_72 = 72.dp

// --- Font size presets ---
val _FONT_18 = 18.sp
val _FONT_22 = 22.sp   // ✅ recommended default
val _FONT_26 = 26.sp

// --- Spacing presets ---
val _SPACE_6 = 6.dp
val _SPACE_8 = 8.dp   // ✅ recommended default
val _SPACE_10 = 10.dp

// --- Space key width presets ---
val _SPACE_WEIGHT_SMALL = 1.5f
val _SPACE_WEIGHT_MEDIUM = 2f   // ✅ recommended default
val _SPACE_WEIGHT_BIG = 2.5f


@Composable
fun PosTouchKeyboardAdv(
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onClose: () -> Unit,
    onMore: () -> Unit
) {

    // ✅ choose active preset here
    val keyHeight = _KEY_HEIGHT_56
    val fontSize = _FONT_22
    val spacing = _SPACE_8
    val spaceWeight = _SPACE_WEIGHT_MEDIUM

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
                        KeyBigAdv(it, keyHeight, fontSize, Modifier.weight(1f)) {
                            onKeyPress(it)
                        }
                    }
                }

                // Row 2
                Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    listOf("Q","W","E","R","T","Y","U","I").forEach {
                        KeyBigAdv(it, keyHeight, fontSize, Modifier.weight(1f)) {
                            onKeyPress(it)
                        }
                    }
                }

                // Row 3
                Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    listOf("A","S","D","F","G","H","J","K").forEach {
                        KeyBigAdv(it, keyHeight, fontSize, Modifier.weight(1f)) {
                            onKeyPress(it)
                        }
                    }
                }

                // Row 4
                Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    listOf("Z","X","C","V","B","N",".").forEach {
                        KeyBigAdv(it, keyHeight, fontSize, Modifier.weight(1f)) {
                            onKeyPress(it)
                        }
                    }

                    KeyBigAdv("⌫", keyHeight, fontSize, Modifier.weight(1f)) {
                        onBackspace()
                    }
                }
            }

            // 🔹 RIGHT ACTION COLUMN
            Column(
                modifier = Modifier.weight(0.6f),
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {
                KeyBigAdv("OK", keyHeight, fontSize, Modifier.fillMaxWidth()) {
                    onClose()
                }



                KeyBigAdv("More", keyHeight, fontSize, Modifier.fillMaxWidth()) {
                    onMore()
                }
                KeyBigAdv("CLEAR", keyHeight, fontSize, Modifier.fillMaxWidth()) {
                    onClear()
                }

                KeyBigAdv("SPACE", keyHeight, fontSize, Modifier.fillMaxWidth()) {
                    onKeyPress(" ")
                }


            }

        }


        // -------- Bottom Row --------

    }

}



@Composable
fun KeyBigAdv(
    label: String,
    height: Dp,
    fontSize: androidx.compose.ui.unit.TextUnit,
    modifier: Modifier,
    onClick: () -> Unit
) {

    val isNumber = label.all { it.isDigit() }

    val backgroundColor = when (label) {
        "OK" -> androidx.compose.ui.graphics.Color(0xFF43A047)
        "CLEAR" -> androidx.compose.ui.graphics.Color(0xFFD32F2F)
        else -> androidx.compose.ui.graphics.Color.White
    }

    val textColor = when (label) {
        "OK", "CLEAR" -> androidx.compose.ui.graphics.Color.White
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
//fun KeyBigAdv(
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
//fun KeyBigAdv(
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

