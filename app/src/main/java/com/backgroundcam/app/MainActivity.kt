package com.backgroundcam.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.backgroundcam.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    companion object {
        @JvmStatic lateinit var serviceIntent: Intent
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
        binding.button.setOnClickListener {
            Log.d(TAG, "Start button clicked")
            if (!checkPermissions()) {
                makeToast(getString(R.string.not_all_permissions))
            }
            else {
                ContextCompat.startForegroundService(applicationContext,serviceIntent)
            }
        }
        binding.button2.setOnClickListener {
           stopService(serviceIntent)
        }
        binding.button3.setOnClickListener {
            val intent = Intent(this,AnotherActivity::class.java)
            startActivity(intent)
        }
        requestPermissions()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(serviceIntent)
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