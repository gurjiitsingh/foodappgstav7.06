//package com.it10x.foodappgstav7_06.data.pos
//
//import android.content.Context
//import androidx.room.Room
//import androidx.room.migration.Migration
//import androidx.sqlite.db.SupportSQLiteDatabase
//
//object AppDatabaseProvider {
//
//    @Volatile
//    private var INSTANCE: AppDatabase? = null
//
//    fun get(context: Context): AppDatabase {
//        return INSTANCE ?: synchronized(this) {
//            Room.databaseBuilder(
//                context.applicationContext,
//                AppDatabase::class.java,
//                "pos.db"
//            )
//                .addMigrations(MIGRATION_46_47)
//                .build()
//                .also { INSTANCE = it }
//        }
//    }
//
//    // 🟢 Example: safe migration 46→47
//    private val MIGRATION_46_47 = object : Migration(46, 47) {
//        override fun migrate(database: SupportSQLiteDatabase) {
//            // No destructive change — keep existing data
//        }
//    }
//}
//
//



package com.it10x.foodappgstav7_06.data.pos

import android.content.Context
import androidx.room.Room

object AppDatabaseProvider {

    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun get(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "pos.db"     // ⭐ THIS IS THE ONLY DB FILE
            ).fallbackToDestructiveMigration().build().also {
                INSTANCE = it
            }
        }
    }
}



