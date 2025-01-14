package com.example.billmate.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.billmate.utils.Converters

@Entity(tableName = "group_table")
@TypeConverters(Converters::class) // Use converters for complex types like List
data class Group(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,                          // Unique ID for the group
    val name: String,                         // Group name
    val description: String = "",             // Description of the group
    val members: List<String> = listOf(),     // List of member names (String)
    val totalExpense: Double = 0.0,           // Total expenses for the group
    val createdAt: Long = System.currentTimeMillis(), // Timestamp of group creation
    val splitType: String = "Equally",        // Split type (e.g., equally, percentage, etc.)
    val notes: String = ""                    // Additional notes or comments
)
