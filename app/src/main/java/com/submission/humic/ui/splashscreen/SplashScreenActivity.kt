package com.submission.humic.ui.splashscreen

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.submission.humic.R
import com.submission.humic.databinding.ActivitySplashScreen2Binding
import com.submission.humic.model.UserPreference
import com.submission.humic.ui.ViewModelFactory
import com.submission.humic.ui.input.InputActivity
import com.submission.humic.ui.welcome.WelcomeActivity

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SplashScreenActivity : AppCompatActivity() {

    private val splash_time: Long = 750
    private lateinit var binding: ActivitySplashScreen2Binding
    private lateinit var splashScreenViewModel: SplashScreenViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreen2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        splashScreenViewModel = ViewModelProvider(
            this, ViewModelFactory(UserPreference.getInstance(dataStore))
        ) [SplashScreenViewModel::class.java]

        Handler().postDelayed({

            val i = Intent(this@SplashScreenActivity, InputActivity::class.java)
            val j = Intent(this@SplashScreenActivity, WelcomeActivity::class.java)

            splashScreenViewModel.getUser().observe(this, {user ->
                if (user.isLogin) {
                    startActivity(i)
                } else {
                    startActivity(j)
                }
            })

            finish()
        }, splash_time)

    }
}