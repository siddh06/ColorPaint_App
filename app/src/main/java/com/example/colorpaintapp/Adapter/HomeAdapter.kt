package com.example.colorpaintapp.Adapter

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import androidx.cardview.widget.CardView
import com.example.colorpaintapp.MainActivity
import com.example.colorpaintapp.R

class HomeAdapter(private var imageList: List<String>,
private var context: Context) : BaseAdapter(){

    override fun getCount(): Int {
        return imageList.size
    }

    override fun getItem(p0: Int): Any? {
       return null
    }

    override fun getItemId(p0: Int): Long {
      return p0.toLong()
    }

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
      val view = LayoutInflater.from(context).inflate(R.layout.image_sample, p2, false)

        val image : ImageView = view.findViewById(R.id.imgShow)
        val cardBtn : CardView = view.findViewById(R.id.cardBtn)

        val bitmap : Bitmap = BitmapFactory.decodeFile(imageList.get(p0))

        image.setImageBitmap(bitmap)

        cardBtn.setOnClickListener(View.OnClickListener {
            var intent = Intent(context, MainActivity::class.java)
            intent.putExtra("imagePath", imageList.get(p0))
            context.startActivity(intent)
        })

        return view
    }
}