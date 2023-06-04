package com.example.mlkitcrown.ml_kit

import android.Manifest
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context.AUDIO_SERVICE
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.mlkitcrown.R
import com.example.mlkitcrown.databinding.FragmentDetectObjectBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


//https://developers.google.com/ml-kit/vision/object-detection/android#try-it-out
class DetectObjectFragment : Fragment() {

//    private var recording: Recording? = null
//    private lateinit var videoCapture: VideoCapture<Recorder>
//    private var recorder: Recorder? = null

    private val FILENAME_FORMAT = "HH:mm:ss:SSSS"

    private val contentResolver: ContentResolver by lazy { requireActivity().applicationContext.contentResolver }

    private var _binding: FragmentDetectObjectBinding? = null
    private val binding: FragmentDetectObjectBinding get() = _binding ?: throw Throwable()

    private var objectDetector: ObjectDetector? = null
    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null
    private var adapter: DetectorObjectAdapter? = null
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDetectObjectBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
        initAdapter()
        // Live detection and tracking
        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableClassification()  // Optional
            .enableMultipleObjects()
            .build()

        objectDetector = ObjectDetection.getClient(options)

        requestPermissions()
        binding.takePhotoButton.setOnClickListener {
            takePhoto()
        }

        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.camera_shutter_sound)
        mediaPlayer?.setVolume(1f, 1f)

        val audioManager = requireActivity().getSystemService(AUDIO_SERVICE) as AudioManager
        audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
    }

    private fun initAdapter() {
        adapter = DetectorObjectAdapter()
        binding.detectorRecyclerView.adapter = adapter
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

//            recorder = Recorder.Builder()
//                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
//                .build()
//            videoCapture = VideoCapture.withOutput(recorder!!)

            imageCapture = ImageCapture.Builder()
                .build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, /*videoCapture*/
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        mediaPlayer?.start()
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time stamped name and MediaStore entry.
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    Log.d(TAG, msg)

                    try {
                        val image: InputImage = InputImage.fromFilePath(
                            requireContext(),
                            output.savedUri ?: Uri.parse("")
                        )

                        setImage(image)

                    } catch (e: IOException) {
                        e.printStackTrace()

                    }

                }
            }
        )
    }

    private fun setImage(image: InputImage) {
        objectDetector?.process(image)
            ?.addOnSuccessListener { detectedObjects ->
                // Task completed successfully
                var newObject = detectedObjects.map {
                    it.labels.map { label ->
                        label.text
                    }
                }.joinToString(separator = ", ")
                if (newObject.isEmpty()) {
                    newObject = "empty"
                }
                adapter?.updateData(newObject) ?: Toast.makeText(
                    requireContext(), newObject,
                    Toast.LENGTH_SHORT
                ).show()

                Log.d(TAG, "Average luminosity: $newObject")
            }
            ?.addOnFailureListener { e ->
                // Task failed with an exception
                Log.d(TAG, "Average luminosity: ${e.message}")
            }
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        )
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(
                    requireContext(),
                    "Permission request denied",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                startCamera()
            }
        }

    override fun onDestroyView() {
        _binding = null
        cameraExecutor.shutdown()
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroyView()
    }


    companion object {
        private const val TAG = "CameraX-MLKit"
        private val REQUIRED_PERMISSIONS =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                mutableListOf(
                    Manifest.permission.CAMERA
                )
            } else {
                mutableListOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }.toTypedArray()
    }
}

