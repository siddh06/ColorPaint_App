package com.example.colorpaintapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import com.bumptech.glide.Glide

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        /*val imgGif : ImageView = findViewById(R.id.imgGif)

        Glide.with(this).load(R.drawable.color_blast).into(imgGif)*/

        val handler = Handler()
        handler.postDelayed(object : Runnable{
            override fun run() {
                startActivity(Intent(this@SplashScreen, HomePageActivity::class.java))
                finish()
            }

        }, 1000)
    }
}