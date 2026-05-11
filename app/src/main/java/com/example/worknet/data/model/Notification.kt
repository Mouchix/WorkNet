package com.example.worknet.data.model

data class Notification(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val read: Boolean = false,
    val placeId: String = "",
    val contactEmail: String? = null
)
