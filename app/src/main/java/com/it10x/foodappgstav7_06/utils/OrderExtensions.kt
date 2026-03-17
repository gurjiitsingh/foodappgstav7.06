package com.it10x.foodappgstav7_06.utils

import com.it10x.foodappgstav7_06.data.online.models.OrderMasterData

fun OrderMasterData.createdAtMillis(): Long {
    return createdAt?.toDate()?.time ?: createdAtMillis
}