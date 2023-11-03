package com.submission.humic.ui.splashscreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.submission.humic.model.DataUser
import com.submission.humic.model.UserPreference

class SplashScreenViewModel(private val pref: UserPreference): ViewModel() {

    fun getUser():  LiveData<DataUser> {
        return pref.getUser().asLiveData()
    }


}