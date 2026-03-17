package com.it10x.foodappgstav7_06.data.print


import com.it10x.foodappgstav7_06.data.pos.entities.config.OutletEntity

/**
 * Utility mapper to safely build OutletInfo from database entity.
 */
object OutletMapper {

    fun fromEntity(outlet: OutletEntity?): OutletInfo {
        return outlet?.let {
            OutletInfo(
                name = it.outletName.takeIf { it.isNotBlank() } ?: "FOOD APP",
                addressLine1 = it.addressLine1.takeIf { it.isNotBlank() } ?: "",
                addressLine2 = it.addressLine2?.takeIf { it.isNotBlank() },
                addressLine3 = it.addressLine3?.takeIf { it.isNotBlank() },
                city = it.city.takeIf { it.isNotBlank() },
                phone = it.phone.takeIf { it.isNotBlank() },
                phone2 = it.phone2?.takeIf { it.isNotBlank() },
                email = it.email?.takeIf { it.isNotBlank() },
                web = it.web?.takeIf { it.isNotBlank() },
                gst = it.gstVatNumber?.takeIf { it.isNotBlank() },
                defaultCurrency = it.defaultCurrency?.takeIf { it.isNotBlank() } ?: "₹",
                footerNote = it.footerNote?.takeIf { it.isNotBlank() }
            )
        } ?: OutletInfo(name = "FOOD APP")
    }
}