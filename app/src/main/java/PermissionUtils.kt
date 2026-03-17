package com.it10x.foodappgstav7_06

import android.Manifest
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat

object PermissionUtils {

    fun requestBluetoothPermissions(activity: ComponentActivity) {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        ActivityCompat.requestPermissions(
            activity,
            permissions.toTypedArray(),
            1
        )
    }
}
