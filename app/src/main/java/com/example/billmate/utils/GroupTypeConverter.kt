package com.example.billmate.utils

import androidx.room.TypeConverter

class GroupTypeConverter {

    // Converts a List<String> to a String (JSON-like format)
    @TypeConverter
    fun fromMembersList(members: List<String>): String {
        return members.joinToString(",")
    }

    // Converts a String back to a List<String>
    @TypeConverter
    fun toMembersList(membersString: String): List<String> {
        return membersString.split(",").map { it.trim() }
    }
}
