package com.example.worknet.data.model

data class Job(
    val id: String = "",
    val placeId: String = "",
    val title: String = "",
    val description: String = "",
    val salary: String? = null,
    val contractType: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
