package com.ly.pet

import android.app.Activity
import android.os.Bundle
import android.widget.Toast

class PetActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 最简单的测试：弹个提示
        Toast.makeText(this, "启动了", Toast.LENGTH_SHORT).show()
    }
}
