package com.example.colorpaintapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val splashLayout = findViewById<View>(R.id.splashLayout)
        ViewCompat.setOnApplyWindowInsetsListener(splashLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        /*val imgGif : ImageView = findViewById(R.id.imgGif)

        Glide.with(this).load(R.drawable.color_blast).into(imgGif)*/

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(object : Runnable{
            override fun run() {
                startActivity(Intent(this@SplashScreen, HomePageActivity::class.java))
                finish()
            }

        }, 1000)
    }
}