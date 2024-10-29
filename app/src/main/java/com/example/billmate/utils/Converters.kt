package com.example.billmate.utils

import android.net.Uri
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken



class Converters {
    @TypeConverter
    fun fromUriList(uris: List<Uri>): String {
        return Gson().toJson(uris.map { it.toString() })
    }

    @TypeConverter
    fun toUriList(data: String): List<Uri> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson<List<String>>(data, listType).map { Uri.parse(it) }
    }
}


