package com.it10x.foodappgstav7_06.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun HomeScreen(
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {



        Text(
            text = "FOOD POS",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Welcome",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ✅ MAIN POS ACTION

        Button(onClick = { navController.navigate("pos") }) {
            Text("POS")
        }

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            onClick = { navController.navigate("orders") }
        ) {
            Text("Open Orders / POS")
        }

        // ✅ SECONDARY ACTIONS
//        OutlinedButton(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(52.dp),
//            onClick = { navController.navigate("products") }
//        ) {
//            Text("Products")
//        }

//        OutlinedButton(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(52.dp),
//            onClick = { navController.navigate("categories") }
//        ) {
//            Text("Categories")
//        }

        Spacer(modifier = Modifier.height(24.dp))

        // ✅ SETTINGS
        OutlinedButton(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            onClick = { navController.navigate("printer_role_selection") }
        ) {
            Text("Printer Settings")
        }


        Button(
            onClick = { navController.navigate("sync_data") }
        ) {
            Text("Sync Products & Categories")
        }

        Button(onClick = { navController.navigate("local_products") }) {
            Text("View Local Products")
        }
        Button(onClick = { navController.navigate("local_categories") }) {
            Text("View Local Categories")
        }



    }
}
