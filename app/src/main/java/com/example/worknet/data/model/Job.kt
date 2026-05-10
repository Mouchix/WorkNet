package com.example.worknet.data.model

data class Job(
    val id: String = "",
    val placeId: String = "",
    val title: String = "",
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
