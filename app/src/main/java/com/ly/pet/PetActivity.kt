package com.ly.pet

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView

class PetActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tv = TextView(this)
        tv.text = "桌宠测试"
        tv.setBackgroundColor(Color.RED)
        setContentView(tv)
    }
}
