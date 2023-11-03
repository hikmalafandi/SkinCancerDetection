package com.submission.humic.ui.output

import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.submission.humic.databinding.ActivityOutputBinding
import com.submission.humic.model.UserPreference
import com.submission.humic.ui.ViewModelFactory

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class OutputActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOutputBinding
    private var image: Uri? = null
    private lateinit var outputViewModel: OutputViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOutputBinding.inflate(layoutInflater)
        setContentView(binding.root)

        image = intent.getParcelableExtra(EXTRA_IMAGE_URI)
        val condition = intent.getStringExtra(EXTRA_CONDITION)
        val probabilitas = intent.getStringExtra(EXTRA_PROBABILITAS)


        binding.outputCondition.text = condition
        binding.outputProbabilitas.text = probabilitas
        loadImageFromUri()

        binding.back.setOnClickListener {
            onBackPressed()
        }

        setupViewModel()



        /*
        binding.kirim.setOnClickListener {
            if (condition != null && image != null) {
                outputViewModel.sendResult(condition, image.toString())
            }
        }

         */



    }

    private fun setupViewModel() {
        outputViewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore))
        )[OutputViewModel::class.java]
    }

    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
        const val EXTRA_CONDITION = "extra_condition"
        const val EXTRA_PROBABILITAS = "extra_probabilitas"
    }

    private fun loadImageFromUri() {
        image?.let { image ->
            Glide.with(this)
                .load(image)
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                .into(binding.picture)
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