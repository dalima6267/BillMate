package com.example.billmate.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.billmate.models.Group
import com.example.billmate.utils.GroupTypeConverter

@Database(entities = [Group::class], version = 1, exportSchema = false)
@TypeConverters(GroupTypeConverter::class) // If you have custom type converters for complex types
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
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
