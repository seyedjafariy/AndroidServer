package com.worldsnas.androidserver

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class EmptyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startService(Intent(this, ServerService::class.java))
        finish()
    }
}