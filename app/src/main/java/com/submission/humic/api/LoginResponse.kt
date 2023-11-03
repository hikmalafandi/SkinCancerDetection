package com.submission.humic.api

import com.google.gson.annotations.SerializedName

data class LoginResponse(

    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("patient")
    val patient: Patient,
    @SerializedName("token")
    val token: String,

    )