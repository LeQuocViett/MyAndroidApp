package com.example.dncs3.model

import com.google.gson.annotations.SerializedName

data class Appointment(
    val id: Int = 0,
    @SerializedName("user_id") val userId: Int = 0,
    @SerializedName("service_id") val serviceId: Int = 0,
    @SerializedName("appointment_date") val appointmentDate: String = "",
    @SerializedName("appointment_time") val appointmentTime: String = "",
    val status: String = "PENDING",
    @SerializedName("userName") val userName: String = "",
    @SerializedName("serviceName") val serviceName: String = "",
    @SerializedName("user_phone") val userPhone: String = "",
    val note: String = "",
    val price: Double = 0.0,
    @SerializedName("cancel_reason") val cancelReason: String = ""
)
