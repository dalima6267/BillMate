package com.example.billmate.models


data class Group(
    val id: Int? = null,
    val name: String,
    val description: String = "",
    val members: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
) {
    fun membersAsString(): String = members.joinToString(", ")
}


