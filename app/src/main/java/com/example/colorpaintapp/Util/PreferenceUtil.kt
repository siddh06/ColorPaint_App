package com.example.colorpaintapp.Util

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor

class PreferenceUtil private constructor(context : Context){
    private val mySharedPreferences : SharedPreferences = context.getSharedPreferences("colorData", Context.MODE_PRIVATE)
    private val editor : SharedPreferences.Editor = mySharedPreferences.edit()

    companion object{
        private var instance : PreferenceUtil? = null
        fun getInstance(context: Context) : PreferenceUtil{
            if(instance == null){
                instance = PreferenceUtil(context)
            }
            return instance as PreferenceUtil
        }
    }

    fun setString(key : String , value: String){
        editor.putString(key, value)
        editor.apply()
    }

    fun getString(key: String?): String?{
        return mySharedPreferences.getString(key, "")
    }

}