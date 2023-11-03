package com.submission.humic.ui.login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ReportFragment.Companion.reportFragment
import androidx.lifecycle.ViewModelProvider
import com.submission.humic.R
import com.submission.humic.databinding.ActivityLoginBinding
import com.submission.humic.model.DataUser
import com.submission.humic.model.UserPreference
import com.submission.humic.ui.ViewModelFactory
import com.submission.humic.ui.input.InputActivity

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var user: DataUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this, InputActivity::class.java))
        }

        playAnimation()
        setupViewModel()
        setupAction()
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.imgLogin, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val email = ObjectAnimator.ofFloat(binding.tvEmail, View.ALPHA, 1f).setDuration(500)
        val edEmail = ObjectAnimator.ofFloat(binding.edEmail, View.ALPHA, 1f).setDuration(500)
        val password = ObjectAnimator.ofFloat(binding.tvPassword, View.ALPHA, 1f).setDuration(500)
        val edPassword = ObjectAnimator.ofFloat(binding.edPassword, View.ALPHA, 1f).setDuration(500)
        val btnLogin = ObjectAnimator.ofFloat(binding.btnLogin, View.ALPHA, 1f).setDuration(500)

        AnimatorSet().apply {
            playSequentially(email, edEmail, password, edPassword, btnLogin)
            start()
        }
    }

    private fun setupViewModel() {
        loginViewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore))
        )[LoginViewModel::class.java]

        loginViewModel.getUser().observe(this, {user ->
            this.user = user
        })
    }

    private fun setupAction() {
        binding.btnLogin.setOnClickListener {
            val email = binding.edEmail.text.toString()
            val password = binding.edPassword.text.toString()
            when {
                email.isEmpty() -> {
                    binding.edEmail.error = "Masukkan email"
                }
                password.isEmpty() -> {
                    binding.edPassword.error = "Masukkan password"
                }
                else -> {
                    loginViewModel.login(email, password)
                    loginViewModel.isLoading.observe(this, {
                        showLoading(it)
                    })
                    loginViewModel.loginSuccess.observe(this, { success ->
                        if (success) {
                            AlertDialog.Builder(this, R.style.AlertDialogTheme).apply {
                                setTitle("Selamat")
                                setMessage("Anda berhasil login")
                                setPositiveButton("Lanjut") { _, _ ->
                                    val intent = Intent(context, InputActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                    startActivity(intent)
                                    finish()
                                }
                                create()
                                show()
                            }
                            loginViewModel.loginUser()
                        } else {
                            loginViewModel.loginFailed.observe(this, {failed ->
                                if (failed) {
                                    Toast.makeText(this@LoginActivity, "Email atau Password salah", Toast.LENGTH_SHORT).show()
                                }
                            })
                        }
                    })
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }
}