package com.example.colorpaintapp

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.GridView
import com.example.colorpaintapp.Adapter.HomeAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File

class HomePageActivity : AppCompatActivity() {

    private var imagesListPath : List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        val gridLayout : GridView = findViewById(R.id.gridLayout)
        val addButton : FloatingActionButton = findViewById(R.id.floatingButton)

        val cw = ContextWrapper(applicationContext)
        val directory : File =cw.getDir("profile", Context.MODE_PRIVATE)

        val files = directory.listFiles()?.filter { it.isFile }

        files?.forEach {
            imagesListPath = imagesListPath + it.absolutePath
        }

        val adapter = HomeAdapter(imagesListPath, this@HomePageActivity)
        gridLayout.adapter = adapter

        addButton.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this@HomePageActivity, MainActivity::class.java))
        })

    }

    override fun onBackPressed() {
        finish()
    }
}