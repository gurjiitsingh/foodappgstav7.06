package com.it10x.foodappgstav7_06.usb

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.*
import android.widget.Toast
import android.util.Log

class USBPrinterHelper(private val context: Context) {

    private val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    private var usbConnection: UsbDeviceConnection? = null
    private var endpoint: UsbEndpoint? = null

    companion object {
        const val ACTION_USB_PERMISSION = "com.it10x.foodappgstav7_06.USB_PERMISSION"
    }

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            if (intent?.action == ACTION_USB_PERMISSION) {
                val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                if (granted && device != null) {
                    connectPrinter(device)
                    Toast.makeText(context, "USB permission granted: ${device.deviceName}", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "USB permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

  init {
    context.registerReceiver(
        usbReceiver,
        IntentFilter(ACTION_USB_PERMISSION),
        Context.RECEIVER_NOT_EXPORTED
    )
}


    fun release() {
        context.unregisterReceiver(usbReceiver)
    }

    fun findAndPrint(device: UsbDevice? = null, text: String = "USB Hello Printer\n") {
        val deviceToUse = device ?: usbManager.deviceList.values.firstOrNull()
        if (deviceToUse == null) {
            Toast.makeText(context, "No USB printer found", Toast.LENGTH_SHORT).show()
            return
        }

        if (!usbManager.hasPermission(deviceToUse)) {
            val permissionIntent = PendingIntent.getBroadcast(
                context,
                0,
                Intent(ACTION_USB_PERMISSION),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            usbManager.requestPermission(deviceToUse, permissionIntent)
            Toast.makeText(context, "Requesting USB permission", Toast.LENGTH_SHORT).show()
        } else {
            connectPrinter(deviceToUse)
            printText(text)
        }
    }

    private fun connectPrinter(device: UsbDevice) {
        val usbInterface = device.getInterface(0)
        for (i in 0 until usbInterface.endpointCount) {
            val ep = usbInterface.getEndpoint(i)
            if (ep.type == UsbConstants.USB_ENDPOINT_XFER_BULK && ep.direction == UsbConstants.USB_DIR_OUT) {
                endpoint = ep
                break
            }
        }
        usbConnection = usbManager.openDevice(device)
        usbConnection?.claimInterface(usbInterface, true)
        Toast.makeText(context, "Printer connected: ${device.deviceName}", Toast.LENGTH_SHORT).show()
    }

    fun printText(text: String) {
        val init = byteArrayOf(0x1B, 0x40) // ESC @
        val feedCut = byteArrayOf(0x0A, 0x0A, 0x1D, 0x56, 0x41, 0x10) // LF LF GS V A n
        val bytes = init + text.toByteArray(Charsets.US_ASCII) + feedCut
        endpoint?.let { ep ->
            usbConnection?.bulkTransfer(ep, bytes, bytes.size, 2000)
            Toast.makeText(context, "USB Printed", Toast.LENGTH_SHORT).show()
            Log.d("USBPrinterHelper", "Printed: $text")
        }
    }
}
