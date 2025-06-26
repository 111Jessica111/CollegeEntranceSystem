package com.example.collegeentrancesystem.bean

data class College(
    val photo: Int = 0,
    val name: String = "",
    val location: String = "",
    val type: String = "",
    val level: String = "",
    val nature: String = "",
    val tags: List<String> = emptyList()
)