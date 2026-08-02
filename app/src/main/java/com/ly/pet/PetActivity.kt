package com.ly.pet

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class PetActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        
        val tv = TextView(this)
        tv.text = "桌宠"
        tv.textSize = 24f
        
        val btn = Button(this)
        btn.text = "启动"
        btn.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                // 没权限，去设置
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                startActivity(intent)
            } else {
                // 有权限，启动服务
                startService(Intent(this, OverlayService::class.java))
                finish()
            }
        }
        
        layout.addView(tv)
        layout.addView(btn)
        setContentView(layout)
    }
}
