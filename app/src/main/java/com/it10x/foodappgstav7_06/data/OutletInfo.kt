package com.it10x.foodappgstav7_06.data.print

data class OutletInfo(
    val name: String,
    val addressLine1: String? = null,
    val addressLine2: String? = null,
    val addressLine3: String? = null,
    val city: String? = null,
    val phone: String? = null,
    val phone2: String? = null,
    val email: String? = null,
    val web: String? = null,
    val gst: String? = null,
    val defaultCurrency: String = "₹", // <-- default
    val footerNote: String? = null
)