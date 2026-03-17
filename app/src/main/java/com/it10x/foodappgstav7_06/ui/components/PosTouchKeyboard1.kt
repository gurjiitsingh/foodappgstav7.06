package com.it10x.foodappgstav7_06.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun PosTouchKeyboard1(
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onClose: () -> Unit,
    onMore: () -> Unit
) {

    val keyHeight = 64.dp
    val spacing = 8.dp

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {

        val rows = listOf(
            listOf("Q","W","E","R","T","Y","U"),
            listOf("A","S","D","F","G","H","J"),
            listOf("Z","X","C","V","B","N","⌫")
        )

        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                row.forEach { key ->
                    KeyBig(
                        label = key,
                        height = keyHeight,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (key == "⌫") onBackspace()
                        else onKeyPress(key)
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
            KeyBig("CLEAR", keyHeight, Modifier.weight(1.5f)) { onClear() }
            KeyBig("SPACE", keyHeight, Modifier.weight(2f)) { onKeyPress(" ") }
         //   KeyBig("OK", keyHeight, Modifier.weight(1.5f)) { onKeyPress() }
        }
    }
}


@Composable
fun KeyBig(
    label: String,
    height: Dp,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(height),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Text(label, fontSize = 18.sp)
    }
}


