package com.it10x.foodappgstav7_06.core

import android.content.Context

object FirstSyncManager {

    private const val PREF = "pos_first_sync"
    private const val KEY = "first_sync_done"

    fun isFirstSyncDone(context: Context): Boolean {
        val pref = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        return pref.getBoolean(KEY, false)
    }

    fun setFirstSyncDone(context: Context) {
        val pref = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        pref.edit().putBoolean(KEY, true).apply()
    }
}