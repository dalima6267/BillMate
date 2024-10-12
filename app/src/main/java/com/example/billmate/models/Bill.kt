package com.example.billmate.models

import android.net.Uri

data class Bill(
    val name: String?,
    val date: String?,
    val type: String?,
    val imageUri: List<Uri> = listOf()// This could be a URL, file path, or a drawable resource
)

