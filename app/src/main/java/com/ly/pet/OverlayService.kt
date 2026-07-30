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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Timer
import java.util.TimerTask
import org.json.JSONObject

class OverlayService : Service() {类 OverlayService：Service() {
    companion object {伴侣 对象 {
        private const val NOTIFICATION_ID = 1私有 常量 值 NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "pet_channel"私有 常量 值 CHANNEL_ID = "pet_channel"
        private val SUPABASE_URL = "https://giqjvwczceugdkteexhv.supabase.co"私有 值 SUPABASE_URL = "https://giqjvwczceugdkteexhv.supabase.co"
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
        log("Whisper rotation started")日志(“低语轮换已启动”)
    }

    private fun updateNotification() {私有 函数 更新通知() {
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
                .setContentText(whisper).setContentText(低语)
                .setSmallIcon(R.drawable.ic_pet)
                .setOngoing(true)
                .setSilent(true)
                .build()
        )
        log("Whisper updated: $whisper"“Whisper已更新：$whisper”)
    }

    override重写 fun onBind(intent: Intent?): IBinder? = null

    override重写 fun onDestroy() {
        overlayView?.let {
            windowManager?.removeView(it)
            it.destroy()它.销毁()
        }
        windowManager?.let {
            if (overlayView != null) windowManager?.removeView(overlayView)
        }
        whisperTimer?.cancel()
        super.onDestroy()
        log("Overlay service destroyed"“覆盖服务已销毁”)
    }

    private私有 fun log日志(msg: String) {(msg: 字符串) {
        Log.d("OverlayService"“叠加服务”, msg)
    }

    fun setState(key: String, value: String) {（key：String，value：String）{
        try尝试 {尝试 {
            val url = URL("$SUPABASE_URL/rest/v1/pet_state")
            val conn = url.openConnection() as HttpURLConnection
            conn.method = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("apikey", SUPABASE_KEY)
            conn.setRequestProperty("Authorization", "Bearer $SUPABASE_KEY")
            val body = JSONObject().apply {
                put("state_key", key)
                put("state_value", value)
                put("updated_at", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(Date()))
            }.toString()
            conn.outputStream.write(body.toByteArray())
            conn.responseCode
            conn.disconnect()
            log("State set: $key = $value")log("状态已设置：$key = $value")
        } catch (e: Exception) {        } 捕获(e: 异常) {
            log("Failed to set state: ${e.message}")log("设置状态失败：${e.message}")日志(“设置状态失败：${e.message}”)日志(“设置状态失败：${e.message}”)
        }
    }
}
```
