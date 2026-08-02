package com.ly.pet

import android.app.Activity
import android.graphics.PixelFormat
import android.os.Bundle
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient

class PetActivity : Activity() {
    private var windowManager: WindowManager? = null
    private var overlayView: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val params = WindowManager.LayoutParams(
            180,
            240,
            WindowManager.TYPE_APPLICATION,
            WindowManager.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            x = 50
            y = 300
        }

        overlayView = WebView(this).apply {
            setBackgroundColor(0x00000000)
            settings.javaScriptEnabled = true
            setWebViewClient(WebViewClient())
            loadUrl("file:///android_asset/pet.html")
        }

        windowManager?.addView(overlayView, params)
    }

    override fun onDestroy() {
        overlayView?.let {
            windowManager?.removeView(it)
            it.destroy()
        }
        super.onDestroy()
    }
}
