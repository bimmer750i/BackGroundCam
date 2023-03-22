package com.backgroundcam.app

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.*
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import java.text.SimpleDateFormat
import java.util.*

class CameraActivity : AppCompatActivity() {

    val CHANNEL_ID = "1"
    private val TAG = "CamService"
    private lateinit var receiver: ActionReceiver
    private lateinit var notification: Notification
    private lateinit var notificationManager: NotificationManager
    companion object {
        const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }
    private var preview: Preview? = null
    private lateinit var timer: Timer
    private var cameraProvider: ProcessCameraProvider? = null

    private lateinit var activeRecording: Recording
    private lateinit var videoCapture: VideoCapture<Recorder>

    private lateinit var contentValues1: ContentValues

    private lateinit var mediaStoreOutputOptions: MediaStoreOutputOptions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        receiver = ActionReceiver()
        val filter = IntentFilter("action")
        registerReceiver(receiver,filter)
        val intentAction = Intent()
        intentAction.putExtra("action", "action1").action = "action"
        val pendingIntent = PendingIntent.getBroadcast(this.applicationContext, 1, intentAction, PendingIntent.FLAG_MUTABLE)
        createNotificationChannel()
        startCamera()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Recording in progress")
            .setOngoing(true)
            .addAction(R.drawable.ic_launcher_foreground, getString(R.string.stop_recording_action),pendingIntent)
            .build()
        notificationManager.notify(1,notification)
        startRecording()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
            .build()
        videoCapture = VideoCapture.withOutput(recorder)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider { Log.d(TAG, "Surface requested") }
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider
                    .bindToLifecycle(this, cameraSelector, preview, videoCapture)
            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun startRecording() {
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        contentValues1 = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video")
            }
        }
        mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues1)
            .build()
        activeRecording = videoCapture.output
            .prepareRecording(this, mediaStoreOutputOptions)
            .apply {
                if (PermissionChecker.checkSelfPermission(
                        this@CameraActivity,
                        Manifest.permission.RECORD_AUDIO
                    ) ==
                    PermissionChecker.PERMISSION_GRANTED
                ) {
                    withAudioEnabled()
                }
            }
            .start(ContextCompat.getMainExecutor(this)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        Log.d(TAG, "Recording started")
                    }
                    is VideoRecordEvent.Finalize -> {
                        Log.d(TAG, "Recording finalized")
                    }
                }
            }
    }


    fun stopRecording() {
        activeRecording.stop()
        try {

        }
        catch (e: java.lang.Exception) {
            Log.d(TAG, "${e.message}")
        }
    }

    private fun createNotificationChannel() {
        val name = getString(R.string.channel_name)
        val descriptionText = getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT

        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    inner class ActionReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            val action = intent.getStringExtra("action")
            if (action == "action1") {
                performAction1()
            }
        }

        fun performAction1() {
            notificationManager.cancelAll()
            stopRecording()
            finish()
        }
    }
}