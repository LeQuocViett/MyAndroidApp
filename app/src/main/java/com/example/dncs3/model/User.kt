package com.example.dncs3.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: Int = 0,
    val name: String,
    val email: String,
    val phone: String,
    val role: String = "USER", // "USER" or "ADMIN"
    val status: String = "ACTIVE", // "ACTIVE", "BLOCKED", "DELETED"
    @SerializedName("created_at")
    val createdAt: String = "",
    @SerializedName("total_appointments")
    val totalAppointments: Int = 0
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val user: User?
)

data class BaseResponse(
    val success: Boolean,
    val message: String
)
