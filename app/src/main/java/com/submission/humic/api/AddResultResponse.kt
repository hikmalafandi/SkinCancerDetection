package com.submission.humic.api

import com.google.gson.annotations.SerializedName

data class AddResultResponse(

    @SerializedName("error")
    val error: Boolean,
    @SerializedName("addResult")
    val addResultRequest: AddResultRequest,
    @SerializedName("message")
    val message: String,

)
