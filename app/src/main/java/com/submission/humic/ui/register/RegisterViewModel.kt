package com.submission.humic.ui.register

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.submission.humic.api.ApiConfig
import com.submission.humic.api.RegisterRequest
import com.submission.humic.api.RegisterResponse
import com.submission.humic.model.DataUser
import com.submission.humic.model.UserPreference
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterViewModel(private val pref: UserPreference): ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _registerSuccess = MutableLiveData(false)
    val registerSuccess: LiveData<Boolean> = _registerSuccess

    fun register(name: String, gender: String, phone: String, email: String, password: String) {
        _isLoading.value = (true)
        val apiService = ApiConfig().getApiService()
        val request = RegisterRequest(
            name = name,
            gender = gender,
            phone = phone,
            email = email,
            password = password
        )
        val registerUser = apiService.register(request)
        registerUser.enqueue(object : Callback<RegisterResponse>{
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                _isLoading.value = (false)
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    saveUser(DataUser(request.name, request.gender, request.phone, request.email, request.password, false))
                    if (responseBody != null) {
                        //saveUser(DataUser(responseBody.patient.name, responseBody.patient.gender, responseBody.patient.phone, responseBody.patient.email, responseBody.patient.password, false))
                        //saveUser(DataUser(responseBody.patient.id, responseBody.patient.name, responseBody.patient.gender, responseBody.patient.phone, responseBody.patient.email, false ))
                        _registerSuccess.postValue(true)
                        Log.e(TAG, "Register Berhasil ${response.message()}")
                    }
                } else {
                    Log.e(TAG, "Register error ${response.message()}")
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                Log.e(TAG, "Register Gagal ${t.message.toString()}")
            }
        })
    }

    /*
    fun saveUser(dataUser: DataUser) {
        viewModelScope.launch {
            pref.savePatient(dataUser)
        }
    }
     */


    fun saveUser(dataUser: DataUser) {
        viewModelScope.launch {
            pref.saveUser(dataUser)
        }
    }


    companion object {
        private const val TAG = "RegisterViewModel"
    }


}