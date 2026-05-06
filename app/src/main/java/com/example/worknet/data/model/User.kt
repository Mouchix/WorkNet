package com.example.worknet.data.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val birthDate: String = "",
    val description: String = "",
    val education: String = "",
    val residence: String = "",
    val photoUrl: String? = null,
    val cvUrl: String? = null,
    val savedJobs: List<String> = emptyList(),
    val fcmToken: String? = null
)
