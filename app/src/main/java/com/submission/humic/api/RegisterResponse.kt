package com.submission.humic.api

import com.google.gson.annotations.SerializedName

data class RegisterResponse(

    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("patient")
    val patient: Patient,

)

