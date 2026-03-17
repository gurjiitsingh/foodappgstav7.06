package com.it10x.foodappgstav7_06.core

import android.content.Context

object PosRoleManager {

    private const val PREF_NAME = "pos_prefs"
    private const val KEY_ROLE = "device_role"

    fun saveRole(context: Context, role: PosRole) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_ROLE, role.name)
            .apply()
    }

    fun getRole(context: Context): PosRole? {
        val role = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_ROLE, null)

        return role?.let { PosRole.valueOf(it) }
    }

    fun isRoleSelected(context: Context): Boolean {
        return getRole(context) != null
    }
}
