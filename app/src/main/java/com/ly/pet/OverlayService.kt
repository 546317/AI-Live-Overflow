package com.ly.pet

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import java.net.HttpURLConnection
import java.net.URL
import java.net.JSONObject

class OverlayService : Service() {
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "pet_channel"
        private val SUPABASE_URL = "https://giqjvwczceugdkteexhv.supabase.co"
        private val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImdpcWp2d2N6Y2V1Z2RrdGVleGh2Iiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc4NTIyMDk0NCwiZXhwIjoyMTAwNzk2OTQ0fQ.yd-Ejpx9AeFCiHuBzUpIUfiIwqmXTUgWhA25dyHnsG4"
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
        log("Overlay service created")
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
            180,  // width in pixels
            240,  // height in pixels
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
            setWebViewClient(object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinish(view, url)
                    Log.d("OverlayService", "Page loaded")
                }
            })
            loadUrl("file:///android_asset/pet.html")
        }

        windowManager?.addView(overlayView, params)
        log("Overlay view added")
    }

    private fun startWhisperRotation() {
        whisperTimer = Timer().apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    runOnUiThread {
                        updateNotification()
                    }
                }
            }, 0, 3600_000)
        }
        log("Whisper rotation started")
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
        log("Whisper updated: $whisper")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        overlayView?.let {
            windowManager?.removeView(it)
            it.destroy()
        }
        windowManager?.let {
            if (overlayView != null) windowManager?.removeView(overlayView)
        }
        whisperTimer?.cancel()
        super.onDestroy()
        log("Overlay service destroyed")
    }

    private fun log(msg: String) {
        Log.d("OverlayService", msg)
    }

    fun setState(key: String, value: String) {
        try {
            val url = URL("$SUPABASE_URL/rest/v1/pet_state")
            conn(url) {
                method = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("apikey", SUPABASE_KEY)
                setRequestProperty("Authorization", "Bearer $SUPABASE_KEY")
                val body = JSONObject().apply {
                    put("state_key", key)
                    put("state_value", value)
                    put("updated_at", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).format(Date()))
                }.toString()
                outputStream.write(body.toByteArray())
                responseCode
                disconnect()
            }
            log("State set: $key = $value")
        } catch (e: Exception) {
            log("Failed to set state: ${e.message}")
        }
    }

    private fun conn(block: HttpURLConnection.() -> Unit): HttpURLConnection {
        val url = URL(it)
        val conn = url.openConnection() as HttpURLConnection
        conn.block()
        return conn
    }
}