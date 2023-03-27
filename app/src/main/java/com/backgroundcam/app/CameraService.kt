package com.backgroundcam.app

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.os.*
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.lifecycle.LifecycleService
import java.text.SimpleDateFormat
import java.util.*


class CameraService(): LifecycleService() {

    val CHANNEL_ID = "1"
    private val TAG = "CamService"
    private lateinit var receiver: ActionReceiver
    private lateinit var notification: Notification
    private lateinit var notificationManager: NotificationManager
    val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

    private lateinit var activeRecording: Recording
    private lateinit var videoCapture: VideoCapture<Recorder>

    private lateinit var contentValues1: ContentValues

    private lateinit var mediaStoreOutputOptions: MediaStoreOutputOptions

    companion object {
        @JvmStatic var running = false
    }




    override fun onCreate() {
        Log.d(TAG, "Service created")
        receiver = ActionReceiver()
        val filter = IntentFilter("action")
        registerReceiver(receiver,filter)
        startCamera()
        super.onCreate()
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (running) {
            stopSelf()
            running = false
        }
        val intentAction = Intent()
        intentAction.putExtra("action", "action1").action = "action"
        // TODO PendingIntent version
        val pendingIntent = PendingIntent.getBroadcast(this.applicationContext, 1, intentAction, PendingIntent.FLAG_MUTABLE)
        createNotificationChannel()
        notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Recording in progress")
            .setOngoing(true)
            .addAction(R.drawable.ic_launcher_foreground, getString(R.string.stop_recording_action),pendingIntent)
            .build()
        startForeground(1,notification)
        startRecording()
        running = true
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d(TAG, "Service destroyed")
        unregisterReceiver(receiver)
        stopRecording()
        running = false
        super.onDestroy()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.SD))
            .build()
        videoCapture = VideoCapture.withOutput(recorder)
        Log.d(TAG, "VideoCapture initialized: ${this::videoCapture.isInitialized}")

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider
                    .bindToLifecycle(this, cameraSelector, videoCapture)
            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(applicationContext))

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
                        this@CameraService,
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
                    is VideoRecordEvent.Pause -> {
                        Log.d(TAG, "Recording paused")
                    }
                }
            }

    }


    fun stopRecording() {
        activeRecording.stop()
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
                stopAction()
            }
        }

        fun stopAction() {
            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancelAll()
            this@CameraService.stopSelf()
        }
    }
}