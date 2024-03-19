package com.example.colorpaintapp.Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.example.colorpaintapp.R
import com.example.colorpaintapp.Util.PreferenceUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

internal class ColorBoxAdapter(
    private var colorList : List<Int>,
    private val context: Context
) : BaseAdapter(){
    override fun getCount(): Int {
        return colorList.size
    }

    override fun getItem(p0: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, p1: View?, p2: ViewGroup?): View {
        val view = LayoutInflater.from(context).inflate(R.layout.color_box_sample, p2,false)

        val card = view.findViewById<CardView>(R.id.colorBoxCard)
        card.setCardBackgroundColor(colorList.get(position))

        card.setOnClickListener {
            Toast.makeText(context, "clicked"+ colorList.get(position), Toast.LENGTH_SHORT).show()
            val hexColor = Integer.toHexString(colorList.get(position))
            val myColor = "#"+hexColor
            PreferenceUtil.getInstance(context).setString("latestColor", myColor)
        }

        return view

    }

    fun addAll(color : Int) {
        if(colorList.contains(color)){
            Toast.makeText(context, "Already Exist Color", Toast.LENGTH_SHORT).show()
        }else {
            colorList = colorList + color
            notifyDataSetChanged()
            try {
                val myColor = PreferenceUtil.getInstance(context).getString("colorList")
                val typeToken = object : TypeToken<List<Int>>() {}.type
                var myList = Gson().fromJson<List<Int>>(myColor, typeToken)
                myList = myList + color
                val updateColor: String = Gson().toJson(myList)
                PreferenceUtil.getInstance(context).setString("colorList", updateColor)
            } catch (e: Exception) {
                Log.e("ColorBoxAdapter", "addAll: " + e.message)
            }
        }
    }

}