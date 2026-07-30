package com.ly.pet

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class PetActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startService(Intent(this, OverlayService::class.java))
        finish()
    }
}