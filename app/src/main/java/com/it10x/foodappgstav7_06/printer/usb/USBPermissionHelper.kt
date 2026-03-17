package com.it10x.foodappgstav7_06.usb

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.widget.Toast

object USBPermissionHelper {

    const val ACTION_USB_PERMISSION = "com.it10x.foodappgstav7_06.USB_PERMISSION"

    private var usbReceiver: BroadcastReceiver? = null

    fun requestPermission(context: Context, device: UsbDevice, onGranted: (() -> Unit)? = null) {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager

        val permissionIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(ACTION_USB_PERMISSION),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        usbReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent?.action == ACTION_USB_PERMISSION) {
                    val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                    if (granted) {
                        Toast.makeText(context, "USB permission granted: ${device.deviceName}", Toast.LENGTH_SHORT).show()
                        onGranted?.invoke()
                    } else {
                        Toast.makeText(context, "USB permission denied: ${device.deviceName}", Toast.LENGTH_SHORT).show()
                    }
                    // Unregister after first call
                    try {
                        context.unregisterReceiver(this)
                        usbReceiver = null
                    } catch (e: Exception) { }
                }
            }
        }

        val filter = IntentFilter(ACTION_USB_PERMISSION)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            context.registerReceiver(usbReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(usbReceiver, filter)
        }

        if (!usbManager.hasPermission(device)) {
            usbManager.requestPermission(device, permissionIntent)
        } else {
            onGranted?.invoke()
        }
    }


    fun release(context: Context) {
        usbReceiver?.let {
            context.unregisterReceiver(it)
            usbReceiver = null
        }
    }
}
