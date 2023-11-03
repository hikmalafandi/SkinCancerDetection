package com.submission.humic.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.submission.humic.model.UserPreference
import com.submission.humic.ui.input.InputViewModel
import com.submission.humic.ui.login.LoginViewModel
import com.submission.humic.ui.output.OutputViewModel
import com.submission.humic.ui.register.RegisterViewModel
import com.submission.humic.ui.splashscreen.SplashScreenViewModel

class ViewModelFactory(private val pref: UserPreference): ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> {
                RegisterViewModel(pref) as T
            }
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(pref) as T
            }
            modelClass.isAssignableFrom(InputViewModel::class.java) -> {
                InputViewModel(pref) as T
            }
            modelClass.isAssignableFrom(OutputViewModel::class.java) -> {
                OutputViewModel(pref) as T
            }
            modelClass.isAssignableFrom(SplashScreenViewModel::class.java) -> {
                SplashScreenViewModel(pref) as T
            }
            else -> throw java.lang.IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }
}