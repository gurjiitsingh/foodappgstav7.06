package com.it10x.foodappgstav7_06.data.pos.viewmodel

import com.it10x.foodappgstav7_06.data.pos.entities.PosCartEntity

object OrderCalculator {

    fun subtotal(items: List<PosCartEntity>): Double {
        return items.sumOf { it.basePrice * it.quantity }
    }

    fun tax(items: List<PosCartEntity>): Double {
        return items.sumOf {
            val rate = it.taxRate ?: 0.0
            (it.basePrice * it.quantity) * rate / 100
        }
    }

    fun grandTotal(items: List<PosCartEntity>): Double {
        return subtotal(items) + tax(items)
    }
}
