package com.example.colorpaintapp.Adapter

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.colorpaintapp.MainActivity
import com.example.colorpaintapp.R
import com.example.colorpaintapp.Util.PreferenceUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.lang.reflect.Type

class CustomAdapter(private var listOfImage : ArrayList<String>, var context : Context) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    var myPopupWindow : PopupWindow? = null
    var add : TextView? = null
    var share : TextView? = null
    var delete : TextView? = null
    var edit : TextView? = null
    var likedList = ArrayList<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.rv_save_sample_list, parent,false)
        myPopupWindow = PopupWindow(context)
        setPopupWindow()
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listOfImage.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bitmap : Bitmap = BitmapFactory.decodeFile(listOfImage.get(position))
        holder.img.setImageBitmap(bitmap)
        val name = listOfImage.get(position).substringAfter("profile/")
        val file = File(listOfImage.get(position))
        val size = file.length()/1024
        holder.imgName.setText(name.replace(".png", ""))
        holder.imgSize.setText("Size : $size Kb")

        val storedList = PreferenceUtil.getInstance(context).getString("LikedList")
        if(!storedList.equals("")){
            val type = object : TypeToken<ArrayList<String>>(){}.type
            likedList = Gson().fromJson<ArrayList<String>>(storedList, type)

            if(likedList.contains(listOfImage[position])) {
                holder.likedBtn.setImageResource(R.drawable.heart_filled)
            }else{
                holder.likedBtn.setImageResource(R.drawable.heart);
            }
        }

        holder.img.setOnClickListener(View.OnClickListener {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("imagePath", listOfImage.get(position))
            context.startActivity(intent)
        })

        holder.likedBtn.setOnClickListener(View.OnClickListener {
            if(likedList.contains(listOfImage[position])) {
                likedList.remove(listOfImage[position])
                holder.likedBtn.setImageResource(R.drawable.heart)
                PreferenceUtil.getInstance(context).setString("LikedList", Gson().toJson(likedList))
            }else{
                likedList.add(listOfImage[position])
                holder.likedBtn.setImageResource(R.drawable.heart_filled)
                PreferenceUtil.getInstance(context).setString("LikedList", Gson().toJson(likedList))
            }
        })

        holder.popupMenu.setOnClickListener {
            try {
                setPopupWindow()
                myPopupWindow!!.showAsDropDown(it, -450, 50)

                myPopupWindow!!.contentView.setOnClickListener(View.OnClickListener {
                    add!!.setOnClickListener(View.OnClickListener {
                        /*val storedList = PreferenceUtil.getInstance(context).getString("LikedList")
                        var likedList = ArrayList<String>()
                        if(!storedList.equals("")){
                            val type = object : TypeToken<ArrayList<String>>(){}.type
                            likedList = Gson().fromJson<ArrayList<String>>(storedList, type)
                        }*/
                        if(!likedList.contains(listOfImage[position])) {
                            likedList.add(listOfImage[position])
                            PreferenceUtil.getInstance(context).setString("LikedList", Gson().toJson(likedList))
                            Toast.makeText(context, "Added", Toast.LENGTH_SHORT).show()
                            holder.likedBtn.setImageResource(R.drawable.heart_filled)
                        }else{
                            Toast.makeText(context, "Already Added", Toast.LENGTH_SHORT).show()
                        }
                        myPopupWindow!!.dismiss()
                    })

                    share!!.setOnClickListener(View.OnClickListener {
                        //Toast.makeText(context, "share", Toast.LENGTH_SHORT).show()
                        shareFile(listOfImage[position])
                    })

                    edit!!.setOnClickListener(View.OnClickListener {
                        val intent = Intent(context, MainActivity::class.java)
                        intent.putExtra("imagePath", listOfImage.get(position))
                        context.startActivity(intent)
                    })

                    delete!!.setOnClickListener(View.OnClickListener {
                        val imgName = listOfImage[position].substringAfter("profile/")
                        val cw = ContextWrapper(context)
                        val directory : File =cw.getDir("profile", Context.MODE_PRIVATE)
                        if(directory.exists()){
                            val file = File(directory,"$imgName")
                            val flag = file.delete()
                            if(flag){
                                listOfImage.removeAt(position)
                                notifyItemRemoved(position)
                                Toast.makeText(context, "File Deleted", Toast.LENGTH_SHORT).show()
                            }
                            myPopupWindow!!.dismiss()
                        }else{
                            Toast.makeText(context, "File Not Found", Toast.LENGTH_SHORT).show()
                        }
                    })
                })
               /* val contectWrapper = ContextThemeWrapper(context, R.style.PopupMenuOverlapAnchor)
                val popupMenu = PopupMenu(context,it,Gravity.CENTER_HORIZONTAL, com.google.android.material.R.attr.actionOverflowMenuStyle, R.style.MyPopupMenu)
                popupMenu.menuInflater.inflate(R.menu.option_menu, popupMenu.menu)
                popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->
                    Toast.makeText(context, "clickerd", Toast.LENGTH_SHORT).show()
                    true
                })
                popupMenu.show()*/
            }catch (e : Exception){
                Log.e("TAG", "onBindViewHolder: "+ e.message )
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var img = itemView.findViewById<ImageView>(R.id.imgPreview)
        var imgName = itemView.findViewById<TextView>(R.id.imgName)
        var imgSize = itemView.findViewById<TextView>(R.id.imgSize)
        var popupMenu = itemView.findViewById<ImageView>(R.id.popupMenu)
        var likedBtn = itemView.findViewById<ImageView>(R.id.likedBtn)
    }

    fun setPopupWindow(){
        val view = LayoutInflater.from(context).inflate(R.layout.popup_menu_layout, null)
        myPopupWindow!!.contentView = view
        myPopupWindow!!.width = 500
        myPopupWindow!!.height = WindowManager.LayoutParams.WRAP_CONTENT
        myPopupWindow!!.isFocusable= true
        myPopupWindow!!.elevation = 4.0f
        myPopupWindow!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        add = view.findViewById(R.id.addTo)
        share = view.findViewById(R.id.share)
        edit = view.findViewById(R.id.edit)
        delete = view.findViewById(R.id.delete)
    }

    private fun shareFile(result : String){
        try {
            val myFile = File(result)
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context, "com.example.colorpaintapp.fileprovider", myFile))
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            shareIntent.putExtra(Intent.EXTRA_TEXT, "ColorPaint App");
            shareIntent.setType("image/*")
            context.startActivity(Intent.createChooser(shareIntent, "share"))
        }catch (ex : Exception){
            Log.e("shareIntent", "share: "+ ex.message )
        }
    }

    fun dataChangeListen(list :ArrayList<String>){
        listOfImage.clear()
        listOfImage = list
        notifyDataSetChanged()
    }

}