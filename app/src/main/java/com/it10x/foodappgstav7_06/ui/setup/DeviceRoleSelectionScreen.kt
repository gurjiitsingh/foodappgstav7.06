package com.it10x.foodappgstav7_06.ui.setting

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav7_06.core.PosRole
import com.it10x.foodappgstav7_06.core.PosRoleManager

@Composable
fun DeviceRoleSelectionScreen(
    onRoleSelected: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Select Device Type",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                PosRoleManager.saveRole(context, PosRole.MAIN)
                onRoleSelected()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("MAIN POS")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                PosRoleManager.saveRole(context, PosRole.WAITER)
                onRoleSelected()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("WAITER POS")
        }
    }
}

//THESE LINE I WILL IMPLIMENT LATTER FOR SAFTY
//val context = LocalContext.current
//val activity = context as? Activity
//PosRoleManager.saveRole(context, PosRole.MAIN)
//activity?.recreate()


//Button(onClick = {
//
//    PosRoleManager.saveRole(context, PosRole.MAIN)
//
//    DeviceRegistrar.registerDevice(
//        context,
//        restaurantId = "REST_001",
//        role = PosRole.MAIN,
//        deviceName = "Counter 1"
//    )
//
//    activity?.recreate()
//
//}) {
//    Text("MAIN POS")
//}



//REFINED CODE
//
//@Composable
//fun DeviceRoleSelectionScreen(
//    navController: NavController
//) {
//    val context = LocalContext.current
//    val activity = context as? Activity
//
//    Column(
//        modifier = Modifier.fillMaxSize(),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//
//        Button(onClick = {
//            PosRoleManager.saveRole(context, PosRole.MAIN)
//
//            navController.navigate("pos") {
//                popUpTo(0)   // clear whole backstack
//            }
//
//            activity?.recreate()  // 🔥 restart app safely
//        }) {
//            Text("MAIN POS")
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Button(onClick = {
//            PosRoleManager.saveRole(context, PosRole.WAITER)
//
//            navController.navigate("pos") {
//                popUpTo(0)
//            }
//
//            activity?.recreate()  // 🔥 restart
//        }) {
//            Text("WAITER POS")
//        }
//    }
//}
