package com.it10x.foodappgstav7_06.ui.components



import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun NumPad(
    onInput: (String) -> Unit
) {

    val buttons = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", "←"),
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),   // 🔥 more row spacing
        modifier = Modifier.padding(horizontal = 4.dp)      // 🔥 outer padding
    ) {

        buttons.forEach { row ->

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp) // 🔥 more gap between buttons
            ) {

                row.forEach { label ->

                    Button(
                        onClick = { onInput(label) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),   // 🔥 increased height
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE0E0E0),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp),  // 🔥 slightly more rounded
                        contentPadding = PaddingValues(6.dp) // 🔥 inner padding
                    ) {
                        Text(
                            text = label,
                            fontSize = 18.sp,               // 🔥 bigger text
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}



//@Composable
//fun NumPad(
//    onInput: (String) -> Unit
//) {
//
//    val buttons = listOf(
//        listOf("1", "2", "3",),
//        listOf("4", "5", "6", ),
//        listOf("7", "8", "9"),
//        listOf( "." , "0", "←"),
//    )
//
//    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
//
//        buttons.forEach { row ->
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.spacedBy(4.dp)
//            ) {
//
//                row.forEach { label ->
//
//                    Button(
//                        onClick = { onInput(label) },
//                        modifier = Modifier
//                            .weight(1f)
//                            .height(40.dp),
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = Color(0xFFE0E0E0),
//                            contentColor = Color.Black
//                        ),
//                        shape = RoundedCornerShape(6.dp)
//                    ) {
//                        Text(label, fontSize = 14.sp)
//                    }
//                }
//            }
//        }
//    }
//}
