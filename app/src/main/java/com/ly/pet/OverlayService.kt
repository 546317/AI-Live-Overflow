package com.ly.pet

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import java.util.Timer
import java.util.TimerTask

class OverlayService : Service() {
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "pet_channel"
    }

    private var windowManager: WindowManager? = null
    private var overlayView: WebView? = null
    private var notificationManager: NotificationManager? = null
    private var whisperTimer: Timer? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        setupOverlay()
        startWhisperRotation()
        Log.d("OverlayService", "Overlay service created")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pet",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                setShowBadge(false)
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentText("草编小狗在这里")
            .setSmallIcon(R.drawable.ic_pet)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun setupOverlay() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val params = WindowManager.LayoutParams(
            180,
            240,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.TYPE_APPLICATION_OVERLAY else WindowManager.TYPE_PHONE,
            WindowManager.FLAG_NOT_FOCUSABLE or WindowManager.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            x = 50
            y = 300
        }

        overlayView = WebView(this).apply {
            setBackgroundColor(0x00000000)
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            addJavascriptInterface(PetBridge(), "PetBridge")
            setWebViewClient(object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    Log.d("OverlayService", "Page loaded")
                }
            })
            loadUrl("file:///android_asset/pet.html")
        }

        windowManager?.addView(overlayView, params)
        Log.d("OverlayService", "Overlay view added")
    }

    private fun startWhisperRotation() {
        val handler = Handler(Looper.getMainLooper())
        whisperTimer = Timer().apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    handler.post {
                        updateNotification()
                    }
                }
            }, 0, 3600_000)
        }
        Log.d("OverlayService", "Whisper rotation started")
    }

    private fun updateNotification() {
        val whispers = listOf(
            "莉莉在吗？",
            "想你啦",
            "该休息了",
            "我在这里",
            "还没睡？",
            "早点睡吧",
            "记得喝水"
        )
        val whisper = whispers.random()
        notificationManager?.notify(NOTIFICATION_ID,
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentText(whisper)
                .setSmallIcon(R.drawable.ic_pet)
                .setOngoing(true)
                .setSilent(true)
                .build()
        )
        Log.d("OverlayService", "Whisper updated: $whisper")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        overlayView?.let {
            windowManager?.removeView(it)
            it.destroy()
        }
        whisperTimer?.cancel()
        super.onDestroy()
        Log.d("OverlayService", "Overlay service destroyed")
    }

    inner class PetBridge {
        @JavascriptInterface
        fun getPetState(): String {
            val states = listOf(
                """{"mood":"happy","action":"idle","energy":80}""",
                """{"mood":"happy","action":"wag","energy":75}""",
                """{"mood":"neutral","action":"idle","energy":60}""",
                """{"mood":"happy","action":"jump","energy":70}"""
            )
            return states.random()
        }

        @JavascriptInterface
        fun setPetState(key: String, value: String) {
            Log.d("OverlayService", "Pet state: $key = $value")
        }
    }
}
