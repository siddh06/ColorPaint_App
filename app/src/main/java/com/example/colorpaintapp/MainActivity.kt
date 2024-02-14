package com.example.colorpaintapp

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class MainActivity : AppCompatActivity() {

    private var drawingView : DrawingView? = null
    private var selectBrush : ImageButton? = null
    private var selectColor : ImageButton? = null
    private var selectImage : ImageButton? = null
    private var currentColorPaint : ImageButton? = null
    private var undoDraw : ImageButton? = null
    private var saveFile : ImageButton? = null
    private var share : ImageButton? = null
    private var currentSaveFile : String = ""
    var openGallaryLauncher : ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result ->
        if(result.resultCode == RESULT_OK && result.data!= null){
            var imageBackground : ImageView = findViewById(R.id.backgrounImg)
            imageBackground.setImageURI(result.data?.data)
        }
    }
    val requestPermission : ActivityResultLauncher<Array<String>>? = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
        permissions ->
        permissions.entries.forEach {
            var permissionName = it.key
            var isGranted = it.value
            if(isGranted){
                Toast.makeText(this,"Permission Granted", Toast.LENGTH_SHORT).show()
                var intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                openGallaryLauncher.launch(intent)
            }else{
                if(permissionName == Manifest.permission.READ_EXTERNAL_STORAGE){
                    Toast.makeText(this,"Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawingView = findViewById(R.id.drawing_view)
        selectBrush = findViewById(R.id.selectBrush)
        selectColor = findViewById(R.id.selectColor)
        selectImage = findViewById(R.id.selectImg)
        undoDraw = findViewById(R.id.undo)
        saveFile = findViewById(R.id.saveImg)
        share = findViewById(R.id.share)
        drawingView?.setSizeForBrush(10f)
        selectBrush?.setOnClickListener {
            showBrushDialog()
        }
        selectColor?.setOnClickListener {
            showColorsDialog()
        }
        selectImage?.setOnClickListener {
            requestStoragePermission()
        }

        undoDraw?.setOnClickListener {
            drawingView?.onClickUndo()
        }
        share?.setOnClickListener {
            if(!currentSaveFile.equals("")){
                shareFile(currentSaveFile)
            }else{
                Toast.makeText(this, "Save The Painted File", Toast.LENGTH_SHORT).show()
            }
        }

        saveFile?.setOnClickListener {
            if(isReadStoragePermissionAllow()){
                lifecycleScope.launch {
                    var frameLayoutView : FrameLayout = findViewById(R.id.frameView)
                    var myBitmbap : Bitmap = getBitmapFromView(frameLayoutView)
                    currentSaveFile = saveBitmapFile(myBitmbap)
                }
            }
        }
    }

    private fun isReadStoragePermissionAllow(): Boolean{
        val result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
            showRationalDialog("ColorPain App", "Access Storage Permission")
        }else{
            requestPermission?.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE))
        }
    }

    private fun showBrushDialog(){
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_brush_size)
        dialog.setTitle("Brush Size")
        var smallBtn : ImageButton = dialog.findViewById(R.id.small)
        var mediumBtn : ImageButton = dialog.findViewById(R.id.medium)
        var largeBtn : ImageButton = dialog.findViewById(R.id.large)
        smallBtn.setOnClickListener {
            drawingView?.setSizeForBrush(5f)
            dialog.dismiss()
        }
        mediumBtn.setOnClickListener {
            drawingView?.setSizeForBrush(10f)
            dialog.dismiss()
        }
        largeBtn.setOnClickListener {
            drawingView?.setSizeForBrush(15f)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showColorsDialog(){
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.paint_brush_color)
        dialog.setTitle("select color")
        val layoutIn : LayoutInflater = this.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view : View = layoutIn.inflate(R.layout.paint_brush_color, null)
        val linearColorPan : LinearLayout = view.findViewById(R.id.colorPaints)
        Log.e("linear", "showColorsDialog: "+ linearColorPan.childCount )
        currentColorPaint = linearColorPan[1] as ImageButton
        dialog.window?.setGravity(Gravity.TOP)
        dialog.show()
    }

    fun onColorSelect(view : View){
        if(view !== currentColorPaint){
            val imageButton = view as ImageButton
            val colorTag = imageButton.tag.toString()
            drawingView?.setColor(colorTag)
            imageButton.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.pallet_select)
            )

            currentColorPaint?.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.pallet_normal)
            )

            currentColorPaint = view

        }
        Toast.makeText(this, "color selected", Toast.LENGTH_LONG).show()
    }

    private fun showRationalDialog(title : String , message : String){
        val builder : AlertDialog.Builder = AlertDialog.Builder(this)

        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("ok"){Dialog, _ -> Dialog.dismiss()}
        builder.create().show()
    }

    private fun getBitmapFromView(view: View): Bitmap{
        val returnBitmapValue = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnBitmapValue)
        val imgBackground = view.background
        if(imgBackground != null){
            imgBackground.draw(canvas)
        }else{
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return returnBitmapValue
    }

    private suspend fun saveBitmapFile(mBitmap : Bitmap?): String{
        var result = ""
        withContext(Dispatchers.IO){
            try {
                if (mBitmap != null){

                    var cw = ContextWrapper(applicationContext)
                    var directory : File =cw.getDir("profile", Context.MODE_PRIVATE)
                    if(!directory.exists()){
                        directory.mkdir()
                    }
                    var file = File(directory,"${System.currentTimeMillis()}.png")
                    var fOut : FileOutputStream? = null
                    fOut = FileOutputStream(file)

                    mBitmap.compress(Bitmap.CompressFormat.PNG, 90,fOut)

                    fOut.close()

                    result = file.absolutePath

                    runOnUiThread {
                        if(result.isNotEmpty()){
                            Log.e("ImageLocation", "saveBitmapFile: "+ result)
                            Toast.makeText(applicationContext, "Image Store Successfully", Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(applicationContext, "Image Save Failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }catch (e : Exception){
                e.printStackTrace()
            }
        }
        return result
    }

    private fun shareFile(result : String){
        try {
            val myFile = File(result)
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(applicationContext, "com.example.colorpaintapp.fileprovider", myFile))
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            shareIntent.putExtra(Intent.EXTRA_TEXT, "your title");
            shareIntent.setType("image/*")
            startActivity(Intent.createChooser(shareIntent, "share"))
        }catch (ex : Exception){
            Log.e("shareIntent", "share: "+ ex.message )
        }
    }
}