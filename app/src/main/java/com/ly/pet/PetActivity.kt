package com.ly.pet

import android.app.Activity
import android.os.Bundle
import android.widget.Toast

class PetActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Toast.makeText(this, "桌宠启动了", Toast.LENGTH_LONG).show()
    }
}
