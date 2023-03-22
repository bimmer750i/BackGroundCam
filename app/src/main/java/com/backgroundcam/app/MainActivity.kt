package com.backgroundcam.app

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.os.PersistableBundle
import android.provider.MediaStore
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.backgroundcam.app.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var serviceIntent: Intent
    companion object {
        @JvmStatic lateinit var preview: Preview
        @JvmStatic lateinit var surfaceProvider: Preview.SurfaceProvider
        @JvmStatic lateinit var aContext: Context
    }
    private val TAG = "BackGroundCam"
    private val OLD_VERSIONS_REQUEST_CODE = 1
    private val MIDDLE_VERSIONS_REQUEST_CODE = 2
    private val VERSION_13_REQUEST_CODE = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        serviceIntent = Intent(applicationContext,CameraService::class.java)
        setContentView(binding.root)
        aContext = this
        surfaceProvider = binding.viewFinder.surfaceProvider
        binding.button.setOnClickListener {
            Log.d(TAG, "Start button clicked")
            if (!checkPermissions()) {
                makeToast(getString(R.string.not_all_permissions))
            }
            else {
                bindService(serviceIntent,object: ServiceConnection {
                    override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
                        Log.d(TAG, "Service connected")
                    }

                    override fun onServiceDisconnected(p0: ComponentName?) {
                        Log.d(TAG, "Service disconnected")
                    }
                },Context.BIND_ABOVE_CLIENT)
                ContextCompat.startForegroundService(applicationContext,serviceIntent)
            }
        }
        binding.button2.setOnClickListener {
           stopService(serviceIntent)
        }
        preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider) }
        requestPermissions()
    }

    override fun onDestroy() {
        super.onDestroy()
        applicationContext.stopService(serviceIntent)
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT < 29 ) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE),OLD_VERSIONS_REQUEST_CODE)
        }
        else if (Build.VERSION.SDK_INT in 29..32) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO),MIDDLE_VERSIONS_REQUEST_CODE)
        }
        else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO,Manifest.permission.POST_NOTIFICATIONS),VERSION_13_REQUEST_CODE)
        }
    }

    private fun makeToast(text: String) {
        Toast.makeText(this,text,Toast.LENGTH_LONG).show()
    }

    private fun checkPermissions(): Boolean {
        var result: Boolean
        val res1 = checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val res2 = checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (Build.VERSION.SDK_INT < 29 ) {
            val res3 = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            result = res1 && res2 && res3
        }
        else if (Build.VERSION.SDK_INT in 29..32) {
            result = res1 && res2
        }
        else {
            val res3 = checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            result = res1 && res2 && res3
        }
        return result
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (!checkPermissions()) {
            makeToast(getString(R.string.not_all_permissions))
        }
        else {
            makeToast(getString(R.string.all_permissions_granted))
        }
    }


}