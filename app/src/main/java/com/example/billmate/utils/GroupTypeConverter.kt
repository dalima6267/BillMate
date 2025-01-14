package com.example.billmate.utils

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class GroupTypeConverter {

    private val gson = Gson()

    // Converts a List<String> to a JSON string
    @TypeConverter
    fun fromMembersList(members: List<String>): String {
        return gson.toJson(members)
    }

    // Converts a JSON string back to a List<String>
    @TypeConverter
    fun toMembersList(membersJson: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(membersJson, type) ?: emptyList()
    }

    // Converts a Map<String, Double> (or similar) to a JSON string
    @TypeConverter
    fun fromExpenseDetailsMap(expenseDetails: Map<String, Double>): String {
        return gson.toJson(expenseDetails)
    }

    // Converts a JSON string back to a Map<String, Double>
    @TypeConverter
    fun toExpenseDetailsMap(expenseDetailsJson: String): Map<String, Double> {
        val type = object : TypeToken<Map<String, Double>>() {}.type
        return gson.fromJson(expenseDetailsJson, type) ?: emptyMap()
    }

    // Add more converters as needed for new complex fields in the Group class
}
