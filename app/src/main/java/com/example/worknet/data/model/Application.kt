package com.example.worknet.data.model

data class Application(
    val id: String = "",
    val jobId: String = "",
    val placeId: String = "",
    val userId: String = "",
    val message: String = "",
    val status: String = "pending", // pending | accepted | rejected
    val createdAt: Long = System.currentTimeMillis(),
    val contactEmail: String? = null,
    val contactPhone: String? = null
)
