package com.it10x.foodappgstav7_06.data.pos.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_06.data.pos.dao.ProductDao
import com.it10x.foodappgstav7_06.data.pos.entities.ProductEntity
import kotlinx.coroutines.flow.*

class ProductsLocalViewModel(
    private val dao: ProductDao
) : ViewModel() {



    // ---------------- SEARCH STATE ----------------
    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<String?>(null)

    // ---------------- PUBLIC PRODUCTS FLOW ----------------

    // NEW: trigger "More" key
    private val _showMoreMatches = MutableStateFlow(false)



    val products: StateFlow<List<ProductEntity>> =
        combine(_searchQuery, _selectedCategory, _showMoreMatches) { query, category, more ->
            Triple(query.trim(), category, more)
        }.flatMapLatest { (query, category, showMore) ->

            when {
                query.isNotEmpty() -> {
                    val isNumeric = query.all { it.isDigit() }

                    if (isNumeric) {
                        dao.searchExactCodeWithFoodType(query, null)
                    } else {
                        dao.getAll().map { allProducts ->
                            val lowerQuery = query.lowercase()

                            // 🔹 First-word progressive match
                            val firstWordMatches = allProducts.filter { product ->
                                val firstWord = product.name.split(" ").firstOrNull()?.lowercase() ?: ""
                                firstWord.startsWith(lowerQuery)
                            }

                            if (!showMore) {
                                firstWordMatches
                            } else {
                                // 🔹 "More" key pressed → search in remaining words
                                val otherWordMatches = allProducts.filter { product ->
                                    val words = product.name.split(" ").drop(1).map { it.lowercase() }
                                    words.any { it.startsWith(lowerQuery) }
                                }
                                    .filter { it !in firstWordMatches } // avoid duplicates

                                // 🔹 Combine first-word matches + other-word matches
                                firstWordMatches + otherWordMatches
                            }
                        }
                    }
                }

                category != null -> dao.getByCategory(category)
                else -> dao.getAll()
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

//    val products: StateFlow<List<ProductEntity>> =
//        combine(_searchQuery, _selectedCategory) { query, category ->
//            query.trim() to category
//        }.flatMapLatest { (query, category) ->
//
//            when {
//                query.isNotEmpty() -> {
//                    val isNumeric = query.all { it.isDigit() }
//
//                    if (isNumeric) {
//                        dao.searchExactCodeWithFoodType(query, null)
//                    } else {
//                        dao.getAll().map { allProducts ->
//                            val lowerQuery = query.lowercase()
//
//                            allProducts.filter { product ->
//                                val firstWord = product.name.split(" ").firstOrNull()?.lowercase() ?: ""
//                                firstWord.startsWith(lowerQuery)
//                            }
//                        }
//                    }
//                }
//
//                category != null -> dao.getByCategory(category)
//                else -> dao.getAll()
//            }
//        }.stateIn(
//            viewModelScope,
//            SharingStarted.WhileSubscribed(5000),
//            emptyList()
//        )



//    val products: StateFlow<List<ProductEntity>> =
//        combine(_searchQuery, _selectedCategory) { query, category ->
//            query.trim() to category
//        }.flatMapLatest { (query, category) ->
//
//            when {
//                query.isNotEmpty() -> {
//                    val isNumeric = query.all { it.isDigit() }
//
//                    if (isNumeric) {
//                        // Numeric search: use DAO code search
//                        dao.searchExactCodeWithFoodType(query, null)
//                    } else {
//                        // Text search: get all products and filter manually
//                        dao.getAll().map { allProducts ->
//                            val lowerQuery = query.lowercase()
//
//                            // Step 1: iterate each product and its words in order
//                            allProducts.filter { product ->
//                                val words = product.name.split(" ")
//                                words.any { word ->
//                                    word.lowercase().startsWith(lowerQuery)
//                                }
//                            }
//                        }
//                    }
//                }
//
//                category != null -> dao.getByCategory(category)
//                else -> dao.getAll()
//            }
//        }.stateIn(
//            viewModelScope,
//            SharingStarted.WhileSubscribed(5000),
//            emptyList()
//        )





//    val products: StateFlow<List<ProductEntity>> =
//        combine(_searchQuery, _selectedCategory) { query, category ->
//            query.trim() to category
//        }.flatMapLatest { (query, category) ->
//
//            when {
//                query.isNotEmpty() -> {
//                    val isNumeric = query.all { it.isDigit() }
//
//                    if (isNumeric) {
//                        // Numeric query: search by code
//                        dao.searchExactCodeWithFoodType(query, null)
//                    } else {
//                        // Text query: search in first word first
//                        dao.getAll().map { allProducts ->
//                            val lowerQuery = query.lowercase()
//
//                            // 🔹 Step 1: match first word
//                            val firstWordMatches = allProducts.filter { product ->
//                                product.name.split(" ").firstOrNull()
//                                    ?.lowercase()
//                                    ?.contains(lowerQuery) == true
//                            }
//
//                            if (firstWordMatches.isNotEmpty()) {
//                                firstWordMatches
//                            } else {
//                                // 🔹 Step 2: match remaining words
//                                allProducts.filter { product ->
//                                    product.name.split(" ").drop(1)
//                                        .any { it.lowercase().contains(lowerQuery) }
//                                }
//                            }
//                        }
//                    }
//                }
//
//                category != null -> dao.getByCategory(category)
//
//                else -> dao.getAll()
//            }
//        }
//            .stateIn(
//                viewModelScope,
//                SharingStarted.WhileSubscribed(5000),
//                emptyList()
//            )



    // ---------------- FUNCTIONS ----------------
//    fun setSearchQuery(query: String) {
//        _searchQuery.value = query
//
//        // 🔥 Reset "More" when query changes
//        _showMoreMatches.value = false
//    }

    fun setSearchQuery(query: String) {
        if (_searchQuery.value != query) {
            _searchQuery.value = query
            _showMoreMatches.value = false
        }
    }

    fun setCategory(categoryId: String?) {
        _selectedCategory.value = categoryId
    }

    fun showMoreMatches(enable: Boolean) {
        _showMoreMatches.value = enable
    }
}
