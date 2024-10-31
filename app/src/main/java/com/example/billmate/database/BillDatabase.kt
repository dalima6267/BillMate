package com.example.billmate.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.billmate.utils.Converters

@Database(entities = [Bill::class], version = 2, exportSchema = false) // Incremented version for migration example
@TypeConverters(Converters::class)
abstract class BillDatabase : RoomDatabase() {
    abstract fun billDao(): BillDao

    companion object {
        @Volatile
        private var INSTANCE: BillDatabase? = null

        fun getDatabase(context: Context): BillDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BillDatabase::class.java,
                    "bill_database"
                )
                    .addMigrations(MIGRATION_1_2) // Add any future migrations here
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Migration from version 1 to 2 (example). Adjust as needed.
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Example: Modify table or add new columns.
                // database.execSQL("ALTER TABLE bills ADD COLUMN new_column_name INTEGER DEFAULT 0")
            }
        }

        // Optional: Callback for pre-populating data
        private val PREPOPULATE_CALLBACK = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Populate database here if needed
            }
        }
    }
}
