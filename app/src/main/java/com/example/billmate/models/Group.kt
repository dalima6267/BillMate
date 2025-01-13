package com.example.billmate.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "group_table")
data class Group(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String = "",
    val members: List<String> = mutableListOf(), // Changed to MutableList to support dynamic updates
    val timestamp: Long = System.currentTimeMillis()
) {
    fun membersAsString(): String = members.joinToString(", ")
}
