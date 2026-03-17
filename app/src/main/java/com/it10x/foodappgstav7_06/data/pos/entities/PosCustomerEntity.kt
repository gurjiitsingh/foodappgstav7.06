package com.it10x.foodappgstav7_06.data.pos.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pos_customers",
    indices = [
        Index(value = ["phone"], unique = true),
        Index(value = ["syncStatus"])
    ]
)
data class PosCustomerEntity(

    @PrimaryKey
    var id: String = "",                 // MUST have default

    var ownerId: String = "",
    var outletId: String = "",

    // BASIC
    var name: String? = null,
    var phone: String = "",              // MUST have default
    var countryCode: String? = null,
    var normalizedPhone: String? = null,
    var email: String? = null,

    // ADDRESS
    var addressLine1: String? = null,
    var addressLine2: String? = null,
    var city: String? = null,
    var state: String? = null,
    var zipcode: String? = null,
    var landmark: String? = null,

    // CREDIT
    var creditLimit: Double = 0.0,
    var currentDue: Double = 0.0,

    // META
    var source: String = "POS",
    var isActive: Boolean = true,
    var createdAt: Long = 0L,            // MUST have default
    var updatedAt: Long? = null,

    var syncStatus: String = "PENDING",
    var lastSyncedAt: Long? = null
)
