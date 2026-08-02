package com.ly.pet

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.widget.FrameLayout

class PetActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = FrameLayout(this)
        layout.setBackgroundColor(Color.RED)
        setContentView(layout)
    }
}
