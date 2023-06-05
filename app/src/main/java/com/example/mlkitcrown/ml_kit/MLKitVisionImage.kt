package com.example.mlkitcrown.ml_kit


import android.app.Activity
import android.content.Context
import android.content.Context.CAMERA_SERVICE
import android.graphics.Bitmap
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.Image
import android.net.Uri
import android.util.SparseIntArray
import android.view.Surface
import com.google.mlkit.vision.common.InputImage
import java.io.IOException
import java.nio.ByteBuffer


class MLKitVisionImage {

     fun imageFromBitmap(bitmap: Bitmap): InputImage {
        val rotationDegrees = 0
       return InputImage.fromBitmap(bitmap, 0)
    }

    private fun imageFromMediaImage(mediaImage: Image, rotation: Int): InputImage {
       return InputImage.fromMediaImage(mediaImage, rotation)
    }

    private fun imageFromBuffer(byteBuffer: ByteBuffer, rotationDegrees: Int): InputImage {

       return InputImage.fromByteBuffer(
            byteBuffer,
            /* image width */ 480,
            /* image height */ 360,
            rotationDegrees,
            InputImage.IMAGE_FORMAT_NV21 // or IMAGE_FORMAT_YV12
        )
        // [END image_from_buffer]
    }

    private fun imageFromArray(byteArray: ByteArray, rotationDegrees: Int): InputImage {
        // [START image_from_array]
        return InputImage.fromByteArray(
            byteArray,
            /* image width */ 480,
            /* image height */ 360,
            rotationDegrees,
            InputImage.IMAGE_FORMAT_NV21 // or IMAGE_FORMAT_YV12
        )

        // [END image_from_array]
    }

    fun imageFromPath(context: Context, uri: Uri): InputImage? {
       return try {
           InputImage.fromFilePath(context, uri)
        } catch (e: IOException) {
            e.printStackTrace()
           null
        }
    }

    // [START get_rotation]
    /**
     * Get the angle by which an image must be rotated given the device's current
     * orientation.
     */
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Throws(CameraAccessException::class)
    private fun getRotationCompensation(
        activity: Activity,
        cameraId: String = MY_CAMERA_ID,
        isFrontFacing: Boolean = false
    ): Int {
        // Get the device's current rotation relative to its "native" orientation.
        // Then, from the ORIENTATIONS table, look up the angle the image must be
        // rotated to compensate for the device's rotation.
        val deviceRotation = activity.windowManager.defaultDisplay.rotation
        var rotationCompensation = ORIENTATIONS.get(deviceRotation)

        // Get the device's sensor orientation.
        val cameraManager = activity.getSystemService(CAMERA_SERVICE) as CameraManager
        val sensorOrientation = cameraManager
            .getCameraCharacteristics(cameraId)
            .get(CameraCharacteristics.SENSOR_ORIENTATION)!!

        if (isFrontFacing) {
            rotationCompensation = (sensorOrientation + rotationCompensation) % 360
        } else { // back-facing
            rotationCompensation = (sensorOrientation - rotationCompensation + 360) % 360
        }
        return rotationCompensation
    }

    @Throws(CameraAccessException::class)
    fun getCompensation(activity: Activity, isFrontFacing: Boolean): Int {
       return getRotationCompensation(activity, MY_CAMERA_ID, isFrontFacing)
    }

    companion object {

        private val TAG = "MLKIT"
        private val MY_CAMERA_ID = "my_camera_id"

        private val ORIENTATIONS = SparseIntArray()

        init {
            ORIENTATIONS.append(Surface.ROTATION_0, 0)
            ORIENTATIONS.append(Surface.ROTATION_90, 90)
            ORIENTATIONS.append(Surface.ROTATION_180, 180)
            ORIENTATIONS.append(Surface.ROTATION_270, 270)
        }
    }
}