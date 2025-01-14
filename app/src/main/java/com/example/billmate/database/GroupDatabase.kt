package com.example.billmate.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.billmate.models.Group
import com.example.billmate.utils.GroupTypeConverter

@Database(entities = [Group::class], version = 2, exportSchema = true) // Updated version to 2
@TypeConverters(GroupTypeConverter::class) // Assuming custom type converters for List<String> or other complex types
abstract class GroupDatabase : RoomDatabase() {

    abstract fun groupDao(): GroupDao

    companion object {
        @Volatile
        private var INSTANCE: GroupDatabase? = null

        // Singleton pattern to ensure only one instance of the database is created
        fun getDatabase(context: Context): GroupDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GroupDatabase::class.java,
                    "group_database"
                )
                    // Handle migrations (if needed)
                    .fallbackToDestructiveMigration() // Use this only if you don't need migrations
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
