package com.submission.humic.ui.output

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.submission.humic.api.AddResultRequest
import com.submission.humic.api.AddResultResponse
import com.submission.humic.api.ApiConfig
import com.submission.humic.model.UserPreference
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class OutputViewModel(private val pref: UserPreference): ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _sendSuccess = MutableLiveData(false)
    val sendSuccess: LiveData<Boolean> = _sendSuccess


    fun sendResult(image: MultipartBody.Part, condition: RequestBody) {
        viewModelScope.launch {
            _isLoading.value = (true)
            val token = pref.getToken().first() ?: ""
            val apiService = ApiConfig().getApiService(token)
            //val userId = pref.getUserId().first() ?: ""

            /*
            val request = AddResultRequest(
                condition = condition,
                image = image
            )
             */

            val addResult = apiService.send(24, image, condition)
            addResult.enqueue(object : Callback<AddResultResponse>{
                override fun onResponse(call: Call<AddResultResponse>, response: Response<AddResultResponse>) {
                    _isLoading.value = (false)
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        if (responseBody != null && !responseBody.error) {
                            _sendSuccess.postValue(true)
                            Log.e(TAG, "Hasil berhasil dikirim! ${response.message()} ")
                        } else {
                            Log.e(TAG, "Terjadi error ${response.message()}")
                        }
                    }
                }

                override fun onFailure(call: Call<AddResultResponse>, t: Throwable) {
                    Log.e(TAG, "Hasil gagal dikirim!: ${t.message.toString()}")
                }

            })
        }
    }

    companion object {
        private const val TAG = "OutputViewModel"
    }

    /*
    fun sendResult(condition: String, image: File) {
        viewModelScope.launch {
            _isLoading.value = (true)
            val token = pref.getToken().first() ?: ""
            val apiService = ApiConfig().getApiService(token)
            //val userId = pref.getUserId().first() ?: ""

            val request = AddResultRequest(
                condition = condition,
                image = image
            )

            val addResult = apiService.send(24, request)
            addResult.enqueue(object : Callback<AddResultResponse>{
                override fun onResponse(call: Call<AddResultResponse>, response: Response<AddResultResponse>) {
                    _isLoading.value = (false)
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        if (responseBody != null && !responseBody.error) {
                            _sendSuccess.postValue(true)
                            Log.e(TAG, "Hasil berhasil dikirim!: ${response.message()} ")
                        } else {
                            Log.e(TAG, "Terjadi error: ${response.message()}")
                        }
                    }
                }

                override fun onFailure(call: Call<AddResultResponse>, t: Throwable) {
                    Log.e(TAG, "Hasil gagal dikirim!: ${t.message.toString()}")
                }

            })
        }
    }

     */

}

