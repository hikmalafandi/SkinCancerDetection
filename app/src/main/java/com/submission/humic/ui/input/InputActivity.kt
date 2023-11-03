package com.submission.humic.ui.input

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.submission.humic.databinding.ActivityInputBinding
import com.submission.humic.ml.Model
import com.submission.humic.model.UserPreference
import com.submission.humic.ui.ViewModelFactory
import com.submission.humic.ui.createCustomTempFile
import com.submission.humic.ui.output.OutputActivity
import com.submission.humic.ui.reduceFileImage
import com.submission.humic.ui.uriToFile
import com.submission.humic.ui.welcome.WelcomeActivity
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class InputActivity : AppCompatActivity() {

    private var getFile: File? = null
    private lateinit var binding: ActivityInputBinding
    private lateinit var inputViewModel: InputViewModel

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
        private const val INPUT_IMAGE_SIZE = 224
        private const val BYTE_PER_CHANNEL = 1
        private const val NUM_CHANNELS = 3
        private const val IMAGE_MEAN = 0
        private const val IMAGE_STD = 255.0f
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionGranted()) {
                Toast.makeText(this, "Don't have permission.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun allPermissionGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInputBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!allPermissionGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        setupViewModel()

        binding.btnCamera.setOnClickListener { startTakePhoto() }

        binding.btnGallery.setOnClickListener { startGallery() }

        binding.btnSkinDetection.setOnClickListener { detection() }

        binding.btnLogout.setOnClickListener { logout() }

    }

    private fun send() {
        if (getFile != null) {
            val imageFile = reduceFileImage(getFile as File)
            val imageRequestBody = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, imageRequestBody)

            val condition = "Normal"
            val conditionPart = condition.toRequestBody("text/plain".toMediaTypeOrNull())
            inputViewModel.sendResult(imagePart, conditionPart)
        }
    }

    private fun detection() {
        if (getFile != null) {
            val file = getFile as File

            try {
                val model = Model.newInstance(applicationContext)

                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)

                val byteBuffer = ByteBuffer.allocateDirect(4 * 31) // Ubah ukuran ByteBuffer sesuai dengan input model (1, 31)
                byteBuffer.order(ByteOrder.nativeOrder())

                val intValues = IntArray(224 * 224)
                resizedBitmap.getPixels(intValues, 0, resizedBitmap.width, 0, 0, resizedBitmap.width, resizedBitmap.height)

                for (i in 0 until 31) { // Ubah perulangan sesuai dengan jumlah elemen dalam input model (1, 31)
                    val value = intValues[i] // Sesuaikan ini dengan cara mendapatkan nilai dari intValues sesuai dengan format yang Anda perlukan
                    byteBuffer.putFloat(value * (1f / 255))
                }

                val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 31), DataType.FLOAT32) // Ubah ukuran inputFeature0 sesuai dengan input model (1, 31)
                inputFeature0.loadBuffer(byteBuffer)

                val outputs = model.process(inputFeature0)
                val outputFeature0 = outputs.outputFeature0AsTensorBuffer

                val confidences = outputFeature0.floatArray
                var maxPos = 0
                var maxConfidence = 0f
                for (i in confidences.indices) {
                    if (confidences[i] > maxConfidence) {
                        maxConfidence = confidences[i]
                        maxPos = i
                    }
                }
                val classes = arrayOf("BCC", "Melanoma", "Normal")
                //binding.outputCondition.text = classes[maxPos]
                val condition = classes[maxPos]
                val probabilitas = "Bcc: ${"%.2f".format(confidences[0] * 100)}%\nMelanoma: ${"%.2f".format(confidences[1] * 100)}%\nNormal: ${"%.2f".format(confidences[2] * 100)}%"
                //binding.hasil.text = resultText
                val intent = Intent(this@InputActivity, OutputActivity::class.java)
                intent.putExtra(OutputActivity.EXTRA_IMAGE_URI, file.toUri())
                intent.putExtra(OutputActivity.EXTRA_CONDITION, condition)
                intent.putExtra(OutputActivity.EXTRA_PROBABILITAS, probabilitas)
                startActivity(intent)

                model.close()

                inputViewModel.isLoading.observe(this, ) {
                    showLoading(it)
                }

                val imageFile = reduceFileImage(getFile as File)
                val imageRequestBody = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, imageRequestBody)

                val conditionPart = condition.toRequestBody("text/plain".toMediaTypeOrNull())

                inputViewModel.sendResult(imagePart, conditionPart)

            } catch (e: Exception) {
                // Handle exceptions, log errors, or display error messages
                val errorMessage = "An error occurred: " + e.message
                Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    private fun ByteArray.toByteBuffer(): ByteBuffer {
        return ByteBuffer.wrap(this)
    }


    private fun startTakePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.resolveActivity(packageManager)

        createCustomTempFile(application).also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this@InputActivity,
                "com.submission.humic",
                it
            )
            currentPhotoPath = it.absolutePath
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            launcherIntentCamera.launch(intent)
        }
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Select photo")
        launcherIntentGallery.launch(chooser)
    }


    private lateinit var currentPhotoPath: String
    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val myFile = File(currentPhotoPath)

            myFile.let { file ->
                getFile = file
                binding.picture.setImageBitmap(BitmapFactory.decodeFile(file.path))
            }
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val selectedimg = it.data?.data as Uri
            selectedimg.let {
                val myFile = uriToFile(it, this@InputActivity)
                getFile = myFile
                binding.picture.setImageURI(it)
            }
        }
    }

    private fun setupViewModel() {
        inputViewModel = ViewModelProvider(
            this,
            ViewModelFactory(UserPreference.getInstance(dataStore))
        ) [InputViewModel::class.java]

        inputViewModel.getUser().observe(this, {user ->
            if (user.isLogin){
                binding.nama.text = user.name
            } else {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
        })

    }

    private fun logout() {
        inputViewModel.logout()
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }



}

/*
    private fun detection() {
        if (getFile != null) {
            val file = getFile as File

            try {
                val model = Model.newInstance(applicationContext)

                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)

                val byteBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3)
                byteBuffer.order(ByteOrder.nativeOrder())

                val intValues = IntArray(224 * 224)
                resizedBitmap.getPixels(intValues, 0, resizedBitmap.width, 0, 0, resizedBitmap.width, resizedBitmap.height)
                var pixel = 0
                for (i in 0 until 224) {
                    for (j in 0 until 224) {
                        val value = intValues[pixel++]
                        byteBuffer.putFloat(((value shr 16) and 0xFF) * (1f / 255))
                        byteBuffer.putFloat(((value shr 8) and 0xFF) * (1f / 255))
                        byteBuffer.putFloat((value and 0xFF) * (1f / 255))
                    }
                }

                val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
                inputFeature0.loadBuffer(byteBuffer)

                val outputs = model.process(inputFeature0)
                val outputFeature0 = outputs.outputFeature0AsTensorBuffer

                val confidences = outputFeature0.floatArray
                var maxPos = 0
                var maxConfidence = 0f
                for (i in confidences.indices) {
                    if (confidences[i] > maxConfidence) {
                        maxConfidence = confidences[i]
                        maxPos = i
                    }
                }
                val classes = arrayOf("Sangat Siap Dikirim!", "Sebaiknya Jangan Gegabah", "Sebaiknya Kirim Sekarang")
                binding.outputCondition.text = classes[maxPos]

                model.close()
            } catch (e: Exception) {
                // Handle exceptions, log errors, or display error messages
                val errorMessage = "An error occurred: " + e.message
                Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

     */



/*
private fun detection() {

    if (getFile != null) {

        val file = getFile as File

        val model = Model.newInstance(applicationContext)
        // sebelum file diubah ke byteBuffer harus di resize dulu dengan ukuran yang sama
        // dengan Tensorbuffer
        val byteBuffer = file.readBytes().toByteBuffer()

        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(224, 224), DataType.FLOAT32)
        inputFeature0.loadBuffer(byteBuffer)

        val outputs = model.process(inputFeature0)
        val outputsFeature0 = outputs.outputFeature0AsTensorBuffer


        model.close()

    }

}

 */

//Log.d("shape", byteBuffer.toString())
//Log.d("shape", inputFeature0.buffer.toString())

/*
    private fun detection() {
        getFile?.let { imageFile ->
            // Resize the image to the desired INPUT_IMAGE_SIZE
            val bitmap = BitmapFactory.decodeFile(imageFile.path)
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_IMAGE_SIZE, INPUT_IMAGE_SIZE, false)

            // Convert the Bitmap to ByteBuffer for model input
            val bufferSize = INPUT_IMAGE_SIZE * INPUT_IMAGE_SIZE * BYTE_PER_CHANNEL * NUM_CHANNELS * 4
            val byteBuffer = ByteBuffer.allocateDirect(bufferSize)
            byteBuffer.order(ByteOrder.nativeOrder())
            val intValues = IntArray(INPUT_IMAGE_SIZE * INPUT_IMAGE_SIZE)
            scaledBitmap.getPixels(intValues, 0, scaledBitmap.width, 0, 0, scaledBitmap.width, scaledBitmap.height)

            var pixel = 0
            for (i in 0 until INPUT_IMAGE_SIZE) {
                for (j in 0 until INPUT_IMAGE_SIZE) {
                    val value = intValues[pixel++]
                    byteBuffer.putFloat(((value shr 16 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                    byteBuffer.putFloat(((value shr 8 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                    byteBuffer.putFloat(((value and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                }
            }

            // Make sure byteBuffer has the correct size
            if (byteBuffer.remaining() != 0) {
                throw IllegalArgumentException("ByteBuffer size is incorrect.")
            }


            // Run model inference
            val model = LiteModel.newInstance(this)
            val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, INPUT_IMAGE_SIZE, INPUT_IMAGE_SIZE, NUM_CHANNELS), DataType.FLOAT32)
            inputFeature0.loadBuffer(byteBuffer)
            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer
            model.close()

            // Get the prediction result
            val bcc = outputFeature0.getFloatValue(0) * 100
            val melanoma = outputFeature0.getFloatValue(1) * 100
            val normal = outputFeature0.getFloatValue(2) * 100

            // Recycle the bitmap to free up memory
            bitmap.recycle()

            // Do something with the prediction result, e.g., display it in a TextView
            val resultText = "Bcc: ${"%.2f".format(bcc)}%\nMelanoma: ${"%.2f".format(melanoma)}%\nNormal: ${"%.2f".format(normal)}%"
            binding.outputCondition.text = resultText
        } ?: run {
            Toast.makeText(this, "Please take or select a photo first.", Toast.LENGTH_SHORT).show()
        }
    }
     */

/*
    private fun detection() {
        getFile?.let { imageFile ->
            // Resize the image to the desired INPUT_IMAGE_SIZE
            val bitmap = BitmapFactory.decodeFile(imageFile.path)
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_IMAGE_SIZE, INPUT_IMAGE_SIZE, false)

            // Convert the Bitmap to ByteBuffer for model input
            val bufferSize = INPUT_IMAGE_SIZE * INPUT_IMAGE_SIZE * BYTE_PER_CHANNEL * NUM_CHANNELS * 4
            val byteBuffer = ByteBuffer.allocateDirect(bufferSize)
            byteBuffer.order(ByteOrder.nativeOrder())
            val intValues = IntArray(INPUT_IMAGE_SIZE * INPUT_IMAGE_SIZE)
            scaledBitmap.getPixels(intValues, 0, scaledBitmap.width, 0, 0, scaledBitmap.width, scaledBitmap.height)

            var pixel = 0
            for (i in 0 until INPUT_IMAGE_SIZE) {
                for (j in 0 until INPUT_IMAGE_SIZE) {
                    val value = intValues[pixel++]
                    byteBuffer.putFloat(((value shr 16 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                    byteBuffer.putFloat(((value shr 8 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                    byteBuffer.putFloat(((value and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                }
            }

            // Make sure byteBuffer has the correct size
            if (byteBuffer.remaining() != 0) {
                throw IllegalArgumentException("ByteBuffer size is incorrect.")
            }


            // Run model inference
            val model = LiteModel.newInstance(this)
            val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, INPUT_IMAGE_SIZE, INPUT_IMAGE_SIZE, NUM_CHANNELS), DataType.FLOAT32)
            inputFeature0.loadBuffer(byteBuffer)
            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer
            model.close()

            // Get the prediction result
            val bcc = outputFeature0.getFloatValue(0) * 100
            val melanoma = outputFeature0.getFloatValue(1) * 100
            val normal = outputFeature0.getFloatValue(2) * 100

            // Recycle the bitmap to free up memory
            bitmap.recycle()

            // Do something with the prediction result, e.g., display it in a TextView
            val resultText = "Bcc: ${"%.2f".format(bcc)}%\nMelanoma: ${"%.2f".format(melanoma)}%\nNormal: ${"%.2f".format(normal)}%"
            binding.outputCondition.text = resultText
        } ?: run {
            Toast.makeText(this, "Please take or select a photo first.", Toast.LENGTH_SHORT).show()
        }
    }
     */

/*
private fun detection() {
    getFile?.let { file ->

        val model = LiteModel.newInstance(this)
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 31), DataType.FLOAT32)

        val bitmap = BitmapFactory.decodeFile(file.path)
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 31, 31, true)
        val floatValues = ImageUtils.bitmapToFloatArray(resizedBitmap)
        inputFeature0.loadArray(floatValues)
    }
}

 */
