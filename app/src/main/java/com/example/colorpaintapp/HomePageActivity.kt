package com.example.colorpaintapp

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.GridView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.GONE
import androidx.recyclerview.widget.RecyclerView.VISIBLE
import com.example.colorpaintapp.Adapter.CustomAdapter
import com.example.colorpaintapp.Adapter.HomeAdapter
import com.example.colorpaintapp.Util.PreferenceUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.util.ArrayList

class HomePageActivity : AppCompatActivity() {

    companion object {
        var imagesListPath : ArrayList<String> = ArrayList()
    }
    private var layoutType : Boolean = false
    private var customAdapter : CustomAdapter? = null
    private var gridAdapter : HomeAdapter? = null
    private var likedSortBtn : TextView? = null
    private var likedListShow = false

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        val homeLayout = findViewById<View>(R.id.homeLayout)
        ViewCompat.setOnApplyWindowInsetsListener(homeLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })

        val gridLayout : GridView = findViewById(R.id.gridLayout)
        val rvLayout : RecyclerView = findViewById(R.id.rvLayout)
        val createNewBtn : TextView = findViewById(R.id.createNewBtn)
        val sortLayout : TextView = findViewById(R.id.sortLayout)
        likedSortBtn = findViewById(R.id.likedSortBtn)

        val cw = ContextWrapper(applicationContext)
        val directory : File =cw.getDir("profile", Context.MODE_PRIVATE)

        val files = directory.listFiles()?.filter { it.isFile }

        files?.forEach {
            imagesListPath.add(it.absolutePath)
            //imagesListPath = imagesListPath + it.absolutePath
        }

        gridLayout.visibility = GONE

        //val adapter = HomeAdapter(imagesListPath, this@HomePageActivity)
        customAdapter = CustomAdapter(imagesListPath, this@HomePageActivity)
        rvLayout.layoutManager = LinearLayoutManager(this)
        rvLayout.adapter = customAdapter

        createNewBtn.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this@HomePageActivity, MainActivity::class.java))
        })

        sortLayout.setOnClickListener(View.OnClickListener {
            val anim = AnimationUtils.loadAnimation(applicationContext, R.anim.blink_anim);
            sortLayout.startAnimation(anim)
            Log.e("CheckList", "onCreate: "+ imagesListPath.size )
            if(!layoutType) {
                layoutType = true
                gridAdapter = HomeAdapter(imagesListPath, this@HomePageActivity)
                gridLayout.adapter = gridAdapter
                val animation = AnimationUtils.loadAnimation(applicationContext, R.anim.fade_animation)
                gridLayout.startAnimation(animation)
                gridLayout.visibility = VISIBLE
                rvLayout.visibility = GONE
            }else{
                val animation = AnimationUtils.loadAnimation(applicationContext, R.anim.fade_animation)
                rvLayout.startAnimation(animation)
                layoutType = false
                rvLayout.visibility = VISIBLE
                gridLayout.visibility = GONE
            }
        })

    }

    override fun onStart() {
        super.onStart()

        likedSortBtn?.setOnClickListener {
            val anim = AnimationUtils.loadAnimation(applicationContext, R.anim.blink_anim)
            likedSortBtn!!.startAnimation(anim)

            val listOfFile : ArrayList<String> = ArrayList()

            val cw = ContextWrapper(applicationContext)
            val directory : File =cw.getDir("profile", Context.MODE_PRIVATE)

            val files = directory.listFiles()?.filter { it.isFile }

            files?.forEach {
                listOfFile.add(it.absolutePath)
            }

            try{
                if(!likedListShow) {
                    val storedList = PreferenceUtil.getInstance(this@HomePageActivity).getString("LikedList")
                    if (!storedList.equals("")) {
                        val type = object : TypeToken<ArrayList<String>>() {}.type
                        val list = Gson().fromJson<ArrayList<String>>(storedList, type)
                        val newList = ArrayList<String>()
                        list.forEach {
                            if(listOfFile.contains(it)){
                                newList.add(it)
                            }
                        }

                        if(newList.size > 0){
                            imagesListPath.clear()
                            imagesListPath = newList
                            likedSortBtn?.setText("ALL")
                            likedListShow = true
                            PreferenceUtil.getInstance(this@HomePageActivity).setString("LikedList", Gson().toJson(newList))
                            if(layoutType){
                                gridAdapter?.dataChangeListenr(newList)
                            }
                            customAdapter?.dataChangeListen(newList)
                        }else{
                            Toast.makeText(this@HomePageActivity, "No List Found", Toast.LENGTH_SHORT).show()
                        }
                    }else{
                        Toast.makeText(this@HomePageActivity, "No List Founds", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    imagesListPath.clear()
                    imagesListPath = listOfFile
                    likedSortBtn?.setText("Faviorate")
                    likedListShow = false
                    if(layoutType){
                        gridAdapter?.dataChangeListenr(imagesListPath)
                    }
                    customAdapter?.dataChangeListen(imagesListPath)
                }
            }catch (e : Exception){
                Log.e("HomePageActivity", "onStart: "+ e.message )
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val update = PreferenceUtil.getInstance(this@HomePageActivity).getBoolean("NewFileAdd")
        if(update) {
            PreferenceUtil.getInstance(this@HomePageActivity).setBoolean("NewFileAdd", false)
            Log.e("HomPageActivity", "Update: True")
            val cw = ContextWrapper(applicationContext)
            val directory: File = cw.getDir("profile", Context.MODE_PRIVATE)

            val files = directory.listFiles()?.filter { it.isFile }
            var flag = false

            files?.forEach {
                if (!imagesListPath.contains(it.absolutePath)) {
                    imagesListPath.add(it.absolutePath)
                    flag = true
                }
                //imagesListPath = imagesListPath + it.absolutePath
            }
            if (flag) {
                customAdapter?.notifyDataSetChanged()
            }
        }
    }
}