package com.example.billmate.database

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.billmate.utils.Converters


@Entity(tableName = "bills")
@TypeConverters(Converters::class)
data class Bill(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String?,
    val date: String?,
    val type: String?,
    val imageUri: List<Uri> = emptyList()
)

