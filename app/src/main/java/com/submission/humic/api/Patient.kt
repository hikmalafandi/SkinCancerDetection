package com.submission.humic.api

import com.google.gson.annotations.SerializedName

data class Patient(

    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("gender")
    val gender: String,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("email")
    val email: String,

)
