package com.it10x.foodappgstav7_06.printer.bluetooth

import android.bluetooth.BluetoothAdapter
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.io.OutputStream
import java.util.UUID

object BluetoothPrinter {

    private const val TAG = "PRINT_BT"

    private val SPP_UUID: UUID =
        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private val mainHandler = Handler(Looper.getMainLooper())

    // =============================
    // TEST PRINT
    // =============================
    fun printTest(
        mac: String,
        roleLabel: String,
        onResult: (Boolean) -> Unit
    ) {
        printText(
            mac,
            """
            ****************************
                 TEST PRINT
            ****************************
            Printer Role : $roleLabel
            Connection   : BLUETOOTH
            Status       : OK
            ----------------------------
            
            
            """.trimIndent(),
            onResult
        )
    }

    // =============================
    // CORE PRINT (ORDER / AUTO)
    // =============================
    fun printText(
        mac: String,
        text: String,
        onResult: (Boolean) -> Unit
    ) {
        Thread {
            var output: OutputStream? = null
            try {
                val adapter = BluetoothAdapter.getDefaultAdapter()
                    ?: throw IllegalStateException("Bluetooth not supported")

                if (!adapter.isEnabled) {
                    throw IllegalStateException("Bluetooth is OFF")
                }

                val device = adapter.getRemoteDevice(mac)
                val socket = device.createRfcommSocketToServiceRecord(SPP_UUID)

                adapter.cancelDiscovery()
                socket.connect()

                output = socket.outputStream

                // ✅ ESC/POS INIT (ONCE)
                output.write(byteArrayOf(0x1B, 0x40))

                val beep = byteArrayOf(
                    0x1B, 0x42, 0x03, 0x02
                )
                output.write(beep)

                // ✅ IMPORTANT: convert LF → CRLF
                val safeText = text
                    .replace("\n", "\r\n")
                    .toByteArray(Charsets.US_ASCII)

                output.write(safeText)

                // ✅ FEED PAPER
                // ✅ FEED 3 LINES + CUT PAPER
                val feedAndCut = byteArrayOf(
                    0x1B, 0x64, 0x03, // Feed 3 lines
                    0x1D, 0x56, 0x01  // Full cut
                )


//                if (prefs.isAutoCutterEnabled()) {
//                    output.write(feedAndCut)
//                } else {
//                    output.write(byteArrayOf(0x0A, 0x0A, 0x0A)) // just feed
//                }

                output.write(feedAndCut)

                output.flush()

                Thread.sleep(300)
                socket.close()

                mainHandler.post { onResult(true) }

            } catch (e: Exception) {
                Log.e(TAG, "Bluetooth print failed", e)
                mainHandler.post { onResult(false) }
            } finally {
                try { output?.close() } catch (_: Exception) {}
            }
        }.start()
    }
}
