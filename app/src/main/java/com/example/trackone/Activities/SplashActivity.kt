package com.example.trackone.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.trackone.R

class SplashActivity : AppCompatActivity() {
    private val splashDelay: Long = 2000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Handler().postDelayed({
            val intent = Intent(this, Signin::class.java)
            startActivity(intent)
            finish()
        }, splashDelay)

    }
}