package com.submission.humic.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {

    @POST("/api/patient/register")
    fun register(
        @Body request: RegisterRequest
    ): Call<RegisterResponse>

    @POST("/api/patient/login")
    fun login(
        @Body loginRequest: LoginRequest
    ): Call<LoginResponse>

    @Multipart
    @POST("/api/patient/{patient_id}/add_detection")
    fun send(
        @Path("patient_id") patient_id: Int,
        @Part file: MultipartBody.Part,
        @Part("condition") condition: RequestBody
    ): Call<AddResultResponse>

}

/*
@Multipart
@POST("/api/patient/{patient_id}/add_detection")
fun send(
    @Path("patient_id") patient_id: Int,
    @Part image: MultipartBody.Part,
    @Part("condition") condition: String
    //@Body addResultRequest: AddResultRequest
): Call<AddResultResponse>
 */

/*
@POST("/api/patient/{patient_id}/add_detection")
fun send(
  @Path("patient_id") patient_id: Int,
  @Body addResultRequest: AddResultRequest
): Call<AddResultResponse>
 */

