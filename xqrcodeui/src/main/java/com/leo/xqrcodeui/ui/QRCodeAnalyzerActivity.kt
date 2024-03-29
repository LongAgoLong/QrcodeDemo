package com.leo.xqrcodeui.ui

import android.Manifest
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.TranslateAnimation
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.leo.xqrcodeui.R
import com.leo.xqrcodeui.analyzer.QRCodeAnalyzer
import com.leo.xqrcodeui.analyzer.QRCodeDecodeListener
import com.leo.xqrcodeui.databinding.ActivityQrcodeAnalyzerBinding
import com.leo.xqrcodeui.ext.FLAGS_FULLSCREEN
import com.leo.xqrcodeui.ext.allPermissionsGranted
import com.leo.xqrcodeui.ext.aspectRatio
import java.lang.ref.WeakReference
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class QRCodeAnalyzerActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "QRCodeAnalyzerActivity"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    private val cameraExecutor: ExecutorService by lazy {
        Executors.newSingleThreadExecutor()
    }

    private val qrCodeAnalyzer: QRCodeAnalyzer by lazy {
        QRCodeAnalyzer(object : QRCodeDecodeListener {
            override fun onDecode(result: String?) {
                Toast.makeText(
                        this@QRCodeAnalyzerActivity,
                        (if (result.isNullOrEmpty()) "result:isNullOrEmpty" else "result:$result"),
                        Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private lateinit var mBinding: ActivityQrcodeAnalyzerBinding

    private var mCameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var mAnimation: TranslateAnimation? = null
    private var mCamera: Camera? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_qrcode_analyzer)
        mBinding.onEventListener = OnEventListener(this)

        // Request camera permissions
        if (!allPermissionsGranted(REQUIRED_PERMISSIONS)) {
            val permissionRequest =
                    registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                        if (it.values.all { value -> value }) {
                            startCamera(CameraSelector.DEFAULT_BACK_CAMERA)
                            startScanAnimation()
                        } else {
                            Toast.makeText(
                                    this,
                                    "Permissions not granted by the user.",
                                    Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                    }
            permissionRequest.launch(REQUIRED_PERMISSIONS)
        }
    }

    override fun onResume() {
        super.onResume()
        mBinding.viewFinder.post {
            mBinding.viewFinder.systemUiVisibility = FLAGS_FULLSCREEN
            mBinding.maskerView.setOverView(mBinding.captureCropLayout)
            if (allPermissionsGranted(REQUIRED_PERMISSIONS)) {
                if (null == mCamera) {
                    startCamera(CameraSelector.DEFAULT_BACK_CAMERA)
                }
                startScanAnimation()
            }
        }

    }

    override fun onPause() {
        super.onPause()
        stopScanAnimation()
    }

    private fun startScanAnimation() {
        mAnimation = TranslateAnimation(
                TranslateAnimation.ABSOLUTE,
                0f, TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0.9f
        ).also {
            it.duration = 1500
            it.repeatCount = -1
            it.repeatMode = Animation.REVERSE
            it.interpolator = LinearInterpolator()
        }
        mBinding.captureScanLine.startAnimation(mAnimation)
    }

    private fun stopScanAnimation() {
        if (null != mAnimation) {
            mAnimation!!.cancel()
            mAnimation = null
        }
    }

    private fun startCamera(cameraSelector: CameraSelector) {
        val metrics = DisplayMetrics().also { mBinding.viewFinder.display.getRealMetrics(it) }
        val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
        val rotation = mBinding.viewFinder.display.rotation
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            // Preview
            val preview = Preview.Builder()
//                .setTargetResolution(size)
                    // We request aspect ratio but no resolution
                    .setTargetAspectRatio(screenAspectRatio)
                    // Set initial target rotation
                    .setTargetRotation(rotation)
                    .build()
                    .also {
                        it.setSurfaceProvider(mBinding.viewFinder.surfaceProvider)
                    }

            val imageAnalyzer = ImageAnalysis.Builder()
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, qrCodeAnalyzer)
                    }
            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()
                // Bind use cases to camera
                mCamera = cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageAnalyzer
                )
                mBinding.lightImg.visibility = if (mCamera?.cameraInfo!!.hasFlashUnit()) {
                    View.VISIBLE
                } else {
                    View.INVISIBLE
                }
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
        mCameraSelector = cameraSelector
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    /**
     * 继续扫描
     */
    private fun continueScan() {
        qrCodeAnalyzer.isAbortDecode = false
    }

    /**
     * 切换摄像头
     */
    private fun switchCamera() {
        val selector = if (mCameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
            CameraSelector.DEFAULT_BACK_CAMERA
        } else {
            CameraSelector.DEFAULT_FRONT_CAMERA
        }
        startCamera(selector)
    }

    inner class OnEventListener(activity: QRCodeAnalyzerActivity) {
        private var reference: WeakReference<QRCodeAnalyzerActivity> = WeakReference(activity)

        fun onClick(view: View) {
            val activity = reference.get() ?: return
            when (view.id) {
                R.id.continueScanBtn -> {
                    activity.continueScan()
                }
                R.id.switchCameraBtn -> {
                    activity.switchCamera()
                }
                R.id.light_img -> {
                    val open = !view.isSelected
                    mCamera?.cameraControl!!.enableTorch(open)
                    view.isSelected = open
                }
            }
        }
    }
}