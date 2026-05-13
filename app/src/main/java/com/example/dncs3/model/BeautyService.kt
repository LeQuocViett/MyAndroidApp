package com.example.dncs3.model

import com.google.gson.annotations.SerializedName

data class BeautyService(
    val id: Int = 0,
    val name: String,
    val price: Double,
    val description: String,
    @SerializedName("image_url")
    val imageUrl: String = "",
    @SerializedName("category_id")
    val categoryId: Int = 0,
    val duration: Int = 30,
    val status: String = "Hoạt động"
)
