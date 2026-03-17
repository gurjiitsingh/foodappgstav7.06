package com.it10x.foodappgstav7_06.printer.discovery

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import androidx.core.content.ContextCompat

class PrinterDiscoveryRepository(
    private val context: Context
) {
    private val usbManager =
        context.getSystemService(Context.USB_SERVICE) as UsbManager

    // ---------------- USB ----------------
    fun getUsbDevices(): List<UsbDevice> =
        usbManager.deviceList.values.toList()

    // -------------- BLUETOOTH -------------
    fun getPairedBluetoothDevices(): List<BluetoothDevice> {
        val adapter = BluetoothAdapter.getDefaultAdapter() ?: return emptyList()

        // ✅ ANDROID 12+ PERMISSION CHECK (PREVENTS CRASH)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                return emptyList() // ⛔ prevent SecurityException
            }
        }

        return adapter.bondedDevices?.toList() ?: emptyList()
    }
}
