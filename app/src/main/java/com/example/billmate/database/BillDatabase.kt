package com.example.billmate.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.billmate.utils.Converters

@Database(entities = [Bill::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class BillDatabase : RoomDatabase() {
    abstract fun billDao(): BillDao

    companion object {
        @Volatile
        private var INSTANCE: BillDatabase? = null

        // Singleton pattern to ensure only one instance of the database is used
        fun getDatabase(context: Context): BillDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BillDatabase::class.java,
                    "bills"
                )
                    .addMigrations(MIGRATION_1_2) // Add migration from version 1 to version 2
                    .fallbackToDestructiveMigration() // Optional: Useful for testing, clears the database when migration fails
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Migration from version 1 to version 2 (adding the 'amount' column)
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Adding a new column 'amount' to the bills table
                database.execSQL("ALTER TABLE bills ADD COLUMN amount REAL NOT NULL DEFAULT 0.0")
            }
        }
    }
}
