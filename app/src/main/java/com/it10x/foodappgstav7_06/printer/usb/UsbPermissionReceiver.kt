package com.it10x.foodappgstav7_06.usb

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log
import android.widget.Toast

class UsbPermissionReceiver(private val onGranted: ((UsbDevice) -> Unit)? = null) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        if (ACTION_USB_PERMISSION == action) {
            val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
            val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)

            if (granted && device != null) {
                Log.d("USB", "Permission granted for device: ${device.deviceName}")
                Toast.makeText(context, "USB permission granted: ${device.deviceName}", Toast.LENGTH_SHORT).show()
                onGranted?.invoke(device)
            } else {
                Log.e("USB", "Permission denied for device")
                Toast.makeText(context, "USB permission denied", Toast.LENGTH_SHORT).show()
            }

            // Auto-unregister after first response (safety)
            try {
                context.unregisterReceiver(this)
            } catch (e: Exception) {
                Log.e("USB", "Receiver already unregistered")
            }
        }
    }

    companion object {
        const val ACTION_USB_PERMISSION = "com.it10x.foodappgstav7_06.USB_PERMISSION"
    }
}
