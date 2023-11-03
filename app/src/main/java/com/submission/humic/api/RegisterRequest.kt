package com.submission.humic.api

data class RegisterRequest(

    val name: String,
    val gender: String,
    val phone: String,
    val email: String,
    val password: String

)
