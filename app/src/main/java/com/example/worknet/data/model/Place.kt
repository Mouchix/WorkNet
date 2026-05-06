package com.example.worknet.data.model

data class Place(
    val id: String = "",
    val ownerId: String = "",
    val title: String = "",
    val description: String = "",
    val address: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val imageUrl: String? = null
)
