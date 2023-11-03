package com.submission.humic.ui.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.submission.humic.api.ApiConfig
import com.submission.humic.api.LoginRequest
import com.submission.humic.api.LoginResponse
import com.submission.humic.model.DataPatient
import com.submission.humic.model.DataUser
import com.submission.humic.model.UserPreference
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel(private val pref: UserPreference): ViewModel() {

    fun getUser(): LiveData<DataUser> {
        return pref.getUser().asLiveData()
    }

    fun saveToken(token: String) {
        viewModelScope.launch{
            pref.saveToken(token)
        }
    }

    fun loginUser() {
        viewModelScope.launch {
            pref.login()
        }
    }

    fun savePatient(dataPatient: DataPatient) {
        viewModelScope.launch {
            pref.savePatient(dataPatient)
        }
    }

    fun saveUserId(id: Int) {
        viewModelScope.launch {
            pref.saveUserId(id)
        }
    }

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _loginSuccess = MutableLiveData(false)
    val loginSuccess: LiveData<Boolean> = _loginSuccess

    private val _loginFailed = MutableLiveData(false)
    val loginFailed: LiveData<Boolean> = _loginFailed

    fun login(email: String, password: String) {
        _isLoading.value = (true)
        val apiService = ApiConfig().getApiService()
        val request = LoginRequest(
            email = email,
            password = password
        )
        val loginUser = apiService.login(request)
        loginUser.enqueue(object: Callback<LoginResponse>{
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                _isLoading.value = (false)
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        if (responseBody.success) {
                            savePatient(DataPatient(responseBody.patient.id, responseBody.patient.name, responseBody.patient.gender, responseBody.patient.phone, responseBody.patient.email))
                            saveToken(responseBody.token)
                            saveUserId(responseBody.patient.id)
                            Log.e(TAG, "User ID: ${responseBody.patient.id}") // Cetak ID user ke logcat
                            _loginSuccess.postValue(true)
                            _loginFailed.postValue(false)
                            Log.e(TAG, "Login Berhasil ${response.message()}")
                        } else {
                            _loginSuccess.postValue(false)
                            _loginFailed.postValue(true)
                            Log.e(TAG, "Login Gagal ${response.message()}")
                        }
                    }
                } else {
                    _loginSuccess.postValue(false)
                    _loginFailed.postValue(true)
                    Log.e(TAG, "Login Error ${response.message()}")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                _isLoading.value = (false)
                _loginSuccess.postValue(false)
                _loginFailed.postValue(true)
                Log.e(TAG, "Login Gagal: ${t.message.toString()}")
            }

        })
    }

    companion object {
        private const val TAG = "LoginViewModel"
    }

}