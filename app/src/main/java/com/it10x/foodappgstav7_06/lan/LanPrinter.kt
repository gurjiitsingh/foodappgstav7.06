package com.it10x.foodappgstav7_06.printer.lan

import android.os.Handler
import android.os.Looper
import android.util.Log
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket

object LanPrinter {

    private const val TAG = "LanPrinter"
    private const val TIMEOUT = 3000

    private val mainHandler = Handler(Looper.getMainLooper())

    // -----------------------------
    // TEST PRINT
    // -----------------------------
    fun printTest(
        ip: String,
        port: Int,
        roleLabel: String,
        onResult: (Boolean) -> Unit
    ) {
        val testText = """
        ****************************
             TEST PRINT
        ****************************
        Printer Role : $roleLabel
        Connection   : LAN
        IP Address   : $ip
        Port         : $port
        Status       : OK
        ----------------------------


    """.trimIndent()

        printText(
            ip = ip,
            port = port,
            text = testText,
            onResult = onResult
        )
    }

    // -----------------------------
    // CORE PRINT
    // -----------------------------
    fun printText(
        ip: String,
        port: Int,
        text: String,
        onResult: (Boolean) -> Unit
    ) {
        Thread {
            var socket: Socket? = null
            var output: OutputStream? = null

            try {
                socket = Socket()
                socket.connect(InetSocketAddress(ip, port), TIMEOUT)

                output = socket.getOutputStream()

                // ✅ ESC/POS INIT
                output.write(byteArrayOf(0x1B, 0x40))


// ✅ BEEP (Kitchen Alert)
                output.write(byteArrayOf(0x1B, 0x42, 0x03, 0x02))

                // ✅ Convert LF → CRLF for consistent printing
                val safeText = text
                    .replace("\n", "\r\n")
                    .toByteArray(Charsets.UTF_8)

                output.write(safeText)

                // ✅ FEED 3 LINES + FULL CUT
                val feedAndCut = byteArrayOf(
                    0x1B, 0x64, 0x03, // feed 3 lines
                    0x1D, 0x56, 0x01  // full cut
                )
                output.write(feedAndCut)

                output.flush()

                mainHandler.post { onResult(true) }

            } catch (e: Exception) {
                Log.e(TAG, "LAN print failed", e)
                mainHandler.post { onResult(false) }
            } finally {
                try {
                    output?.close()
                    socket?.close()
                } catch (_: Exception) {}
            }
        }.start()
    }
}
