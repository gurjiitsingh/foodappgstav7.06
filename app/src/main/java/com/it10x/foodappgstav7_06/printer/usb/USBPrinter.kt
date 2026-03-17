package com.it10x.foodappgstav7_06.printer.usb

import android.content.Context
import android.hardware.usb.*
import android.util.Log
import com.it10x.foodappgstav7_06.usb.USBPermissionHelper
import kotlinx.coroutines.*

object USBPrinter {

    private const val TAG = "USBPrinter"
    const val ACTION_USB_PERMISSION = "com.it10x.foodappgstav7_06.USB_PERMISSION"

    private var usbManager: UsbManager? = null
    private var usbDevice: UsbDevice? = null
    private var connection: UsbDeviceConnection? = null
    private var outEndpoint: UsbEndpoint? = null

    // =================================================
    // INIT + PERMISSION
    // =================================================
    fun init(
        context: Context,
        device: UsbDevice,
        onReady: (Boolean) -> Unit
    ) {
        usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        usbDevice = device

        USBPermissionHelper.requestPermission(context, device) {
            try {
                setupConnection(device)
                onReady(true)
            } catch (e: Exception) {
                Log.e(TAG, "USB setup failed", e)
                onReady(false)
            }
        }
    }

    // =================================================
    // USB CONNECTION SETUP
    // =================================================
    private fun setupConnection(device: UsbDevice) {
        val iface = device.getInterface(0)
        outEndpoint = null

        for (i in 0 until iface.endpointCount) {
            val ep = iface.getEndpoint(i)
            if (
                ep.type == UsbConstants.USB_ENDPOINT_XFER_BULK &&
                ep.direction == UsbConstants.USB_DIR_OUT
            ) {
                outEndpoint = ep
                break
            }
        }

        if (outEndpoint == null) {
            throw IllegalStateException("No OUT endpoint found")
        }

        connection = usbManager?.openDevice(device)
            ?: throw IllegalStateException("Unable to open USB device")

        connection?.claimInterface(iface, true)
        Log.d(TAG, "USB printer connected: ${device.deviceName}")
    }

    // =================================================
    // TEST PRINT
    // =================================================
    fun printTest(
        context: Context,
        device: UsbDevice,
        roleLabel: String,
        onResult: (Boolean) -> Unit
    ) {
        init(context, device) { ready ->
            if (!ready) {
                onResult(false)
                return@init
            }

            val testText = """
            ****************************
                 TEST PRINT
            ****************************
            Printer Role : $roleLabel
            Connection   : USB
            Device Name  : ${device.deviceName}
            Status       : OK
            ----------------------------


        """.trimIndent()

            printText(testText) { success ->
                onResult(success)
            }
        }
    }

    // =================================================
    // CORE PRINT (ORDER / AUTO)
    // =================================================
    fun printText(
        text: String,
        onResult: (Boolean) -> Unit
    ) {
        val ep = outEndpoint
        val conn = connection

        if (ep == null || conn == null) {
            Log.e(TAG, "USB printer not ready")
            onResult(false)
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // ✅ ESC/POS INIT
                val init = byteArrayOf(0x1B, 0x40)

                // ✅ FEED 3 LINES + FULL CUT (Safe for all printers)
                val feedAndCut = byteArrayOf(
                    0x1B, 0x64, 0x03, // Feed 3 lines
                    0x1D, 0x56, 0x01  // Full cut
                )

                // ✅ Convert LF → CRLF
                val safeText = text.replace("\n", "\r\n").toByteArray(Charsets.US_ASCII)

                val data = init + safeText + feedAndCut

                val sent = conn.bulkTransfer(ep, data, data.size, 5000)

                withContext(Dispatchers.Main) {
                    onResult(sent > 0)
                }

            } catch (e: Exception) {
                Log.e(TAG, "USB print error", e)
                withContext(Dispatchers.Main) {
                    onResult(false)
                }
            }
        }
    }

    // =================================================
    // DEVICE LIST
    // =================================================
    fun getConnectedUSBDevices(context: Context): List<UsbDevice> {
        val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        return manager.deviceList.values.toList()
    }

    // =================================================
    // RELEASE
    // =================================================
    fun release() {
        try {
            connection?.close()
        } catch (_: Exception) {}
        connection = null
        outEndpoint = null
        usbDevice = null
    }
}
