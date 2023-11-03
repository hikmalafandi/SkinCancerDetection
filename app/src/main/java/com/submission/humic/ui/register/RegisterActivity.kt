package com.submission.humic.ui.register

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.submission.humic.databinding.ActivityRegisterBinding
import com.submission.humic.model.DataUser
import com.submission.humic.model.UserPreference
import com.submission.humic.ui.ViewModelFactory

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var registerViewModel: RegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        playAnimation()
        setupViewModel()
        setupAction()
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.imgRegister, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val email = ObjectAnimator.ofFloat(binding.tvEmail, View.ALPHA, 1f).setDuration(500)
        val edEmail = ObjectAnimator.ofFloat(binding.edEmail, View.ALPHA, 1f).setDuration(500)
        val password = ObjectAnimator.ofFloat(binding.tvPassword, View.ALPHA, 1f).setDuration(500)
        val edPassword = ObjectAnimator.ofFloat(binding.edPassword, View.ALPHA, 1f).setDuration(500)
        val name = ObjectAnimator.ofFloat(binding.tvName, View.ALPHA, 1f).setDuration(500)
        val edName = ObjectAnimator.ofFloat(binding.edName, View.ALPHA, 1f).setDuration(500)
        val gender = ObjectAnimator.ofFloat(binding.tvGender, View.ALPHA, 1f).setDuration(500)
        val edGender = ObjectAnimator.ofFloat(binding.edGender, View.ALPHA, 1f).setDuration(500)
        val phone = ObjectAnimator.ofFloat(binding.tvPhone, View.ALPHA, 1f).setDuration(500)
        val edPhone = ObjectAnimator.ofFloat(binding.edPhone, View.ALPHA, 1f).setDuration(500)
        val btnRegister = ObjectAnimator.ofFloat(binding.btnRegister, View.ALPHA, 1f).setDuration(500)

        AnimatorSet().apply {
            playSequentially(email, edEmail, password, edPassword, name, edName, gender, edGender, phone, edPhone, btnRegister)
            start()
        }
    }

    private fun setupViewModel() {
        registerViewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore))
        )[RegisterViewModel::class.java]
    }

    private fun setupAction() {
        binding.btnRegister.setOnClickListener {
            val name = binding.edName.text.toString()
            val gender = binding.edGender.text.toString()
            val phone = binding.edPhone.text.toString()
            val email = binding.edEmail.text.toString()
            val password = binding.edPassword.text.toString()
            when {
                email.isEmpty() -> {
                    binding.edEmail.error = "Masukkan email"
                }
                password.isEmpty() -> {
                    binding.edPassword.error = "Masukkan password"
                }
                name.isEmpty() -> {
                    binding.edName.error = "Masukkan nama"
                }
                gender.isEmpty() -> {
                    binding.edGender.error = "Masukkan gender"
                }
                phone.isEmpty() -> {
                    binding.edPhone.error = "Masukkan nomor handphone"
                }
                else -> {
                    registerViewModel.saveUser(DataUser(name, gender, phone, email, password, false))
                    registerViewModel.register(name, gender, phone, email, password)
                    registerViewModel.isLoading.observe(this, {
                        showLoading(it)
                    })
                    registerViewModel.registerSuccess.observe(this, { success ->
                        if (success) {
                            AlertDialog.Builder(this).apply {
                                setTitle("Selamat")
                                setMessage("Berhasil register. Silahkan login")
                                setPositiveButton("Lanjut") { _, _ ->
                                    finish()
                                }
                                create()
                                show()
                            }
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