package com.it10x.foodappgstav7_06.data.pos.viewmodel

import com.it10x.foodappgstav7_06.data.pos.entities.ProductEntity

/**
 * Products to show in POS list:
 * 1. Parent products (have variants)
 * 2. Normal products (no variants)
 */
fun getParentProducts(all: List<ProductEntity>): List<ProductEntity> =
    all.filter {
        it.parentId == null
//                && ( it.type == "parent" || it.type == null
//                )
    }

/**
 * Only variant products
 */
//fun getVariants(all: List<ProductEntity>): List<ProductEntity> =
//    all.filter { it.type == "variant" }


fun getVariants(all: List<ProductEntity>): List<ProductEntity> =
    all.filter { it.parentId != null }
/**
 * Variants belonging to a parent product
 */
fun getVariantsFor(
    parentId: String,
    all: List<ProductEntity>
): List<ProductEntity> =
    all.filter {
        it.parentId == parentId && it.type == "variant"
    }
