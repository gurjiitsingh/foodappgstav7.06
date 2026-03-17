package com.it10x.foodappgstav7_06.ui.settings

import com.it10x.foodappgstav7_06.data.PrinterConfig
import com.it10x.foodappgstav7_06.data.PrinterRole

data class PrinterSettingsState(
    val printers: Map<PrinterRole, PrinterConfig> = emptyMap()
)
