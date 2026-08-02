package com.ly.pet

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class PetActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tv = TextView(this)
        tv.text = "在"
        setContentView(tv)
    }
}
