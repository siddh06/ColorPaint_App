package com.example.colorpaintapp

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.GridView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import com.example.colorpaintapp.Adapter.ColorBoxAdapter
import com.example.colorpaintapp.Util.PreferenceUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ir.kotlin.kavehcolorpicker.KavehColorAlphaSlider
import ir.kotlin.kavehcolorpicker.KavehColorPicker
import ir.kotlin.kavehcolorpicker.KavehHueSlider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private var drawingView : DrawingView? = null
    private var selectBrush : ImageButton? = null
    private var selectColor : ImageButton? = null
    private var selectImage : ImageButton? = null
    private var currentColorPaint : ImageButton? = null
    private var undoDraw : ImageView? = null
    private var saveFile : ImageButton? = null
    private var share : ImageButton? = null
    private var redoBtn : ImageView? = null
    private var currentSaveFile : String = ""
    private var currentColor : String = ""
    private var colorPickerDialog : Dialog? = null
    private var brushDialog : Dialog? = null

    companion object{
        var neonInnerCheck = false
        var neonOuterCheck = false
        var neonSolidCheck = false
        var neonNormalCheck = false
    }

    var openGallaryLauncher : ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result ->
        if(result.resultCode == RESULT_OK && result.data!= null){
            val imageBackground : ImageView = findViewById(R.id.backgrounImg)
            imageBackground.setImageURI(result.data?.data)
        }
    }
    val requestPermission : ActivityResultLauncher<Array<String>>? = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
        permissions ->
        permissions.entries.forEach {
            val permissionName = it.key
            val isGranted = it.value
            if(isGranted){
                Toast.makeText(this,"Permission Granted", Toast.LENGTH_SHORT).show()
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
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
        redoBtn = findViewById(R.id.forward)
        val backBtn = findViewById<ImageView>(R.id.backBtn)
        drawingView?.setSizeForBrush(0.8f)

        val intent : Intent = getIntent()
        if(intent.hasExtra("imagePath")) {
            val value = intent.getStringExtra("imagePath")
            val bitmap: Bitmap = BitmapFactory.decodeFile(value)

            val imageBackground: ImageView = findViewById(R.id.backgrounImg)
            imageBackground.setImageBitmap(bitmap)
        }

        selectBrush?.setOnClickListener {
            showBrushDialog()
        }
        selectColor?.setOnClickListener {
            //startActivity(Intent(this@MainActivity, ColorPicker::class.java))
            colorPickerDialog()
        }
        selectImage?.setOnClickListener {
            requestStoragePermission()
        }

        undoDraw?.setOnClickListener {
            drawingView?.onClickUndo()
        }
        redoBtn?.setOnClickListener {
            drawingView?.onClickRedo()
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
                    val frameLayoutView : FrameLayout = findViewById(R.id.frameView)
                    val myBitmbap : Bitmap = getBitmapFromView(frameLayoutView)
                    currentSaveFile = saveBitmapFile(myBitmbap)
                }
            }
        }

        backBtn.setOnClickListener(View.OnClickListener {
            //startActivity(Intent(this@MainActivity, HomePageActivity::class.java))
            finish()
        })

        val myColorList : String? = PreferenceUtil.getInstance(this@MainActivity).getString("colorList")
        if(myColorList.equals("")){
            var arrayList : List<Int> = ArrayList<Int>()
            arrayList = arrayList + Color.BLACK
            arrayList = arrayList + Color.WHITE
            arrayList = arrayList + Color.GREEN
            arrayList = arrayList + Color.RED

            val myString : String = Gson().toJson(arrayList)

            PreferenceUtil.getInstance(this@MainActivity).setString("colorList", myString)
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
        if(brushDialog != null){
            brushDialog!!.show()
        }else{
            brushDialog = Dialog(this)
            brushDialog!!.window?.requestFeature(Window.FEATURE_NO_TITLE)
            brushDialog!!.setContentView(R.layout.brush_sample)
            brushDialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
            brushDialog!!.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            brushDialog!!.window?.setGravity(Gravity.BOTTOM)
            brushDialog!!.setTitle("Brush Size")
            val pencil : ImageView = brushDialog!!.findViewById(R.id.pencil)
            val pen : ImageView = brushDialog!!.findViewById(R.id.pen)
            val dash : ImageView = brushDialog!!.findViewById(R.id.dash)
            val circle : ImageView = brushDialog!!.findViewById(R.id.circle)
            val rectngale : ImageView = brushDialog!!.findViewById(R.id.rectangle)

            val neonInner : ImageView = brushDialog!!.findViewById(R.id.neonInner)
            val neonOuter : ImageView = brushDialog!!.findViewById(R.id.neonOuter)
            val neonSolid : ImageView = brushDialog!!.findViewById(R.id.neonSolid)
            val airBrush : ImageView = brushDialog!!.findViewById(R.id.airBrush)
            val line : ImageView = brushDialog!!.findViewById(R.id.line)
            val roundRect : ImageView = brushDialog!!.findViewById(R.id.roundRectangle)
            val oval : ImageView = brushDialog!!.findViewById(R.id.ovalBrush)
            val textDraw : ImageView = brushDialog!!.findViewById(R.id.textDraw)
            val fillCircle : ImageView = brushDialog!!.findViewById(R.id.fillCircle)
            val fillRect : ImageView = brushDialog!!.findViewById(R.id.fillRectangle)

            val seekBar : SeekBar = brushDialog!!.findViewById(R.id.seekBar)
            val progressCount : TextView = brushDialog!!.findViewById(R.id.progressCountTxt)
            pencil.setOnClickListener {
                drawingView!!.selectedBrushEffect(false, false, false, false, false, false,false,false,false, false)
                brushDialog!!.dismiss()
            }
            pen.setOnClickListener {
                drawingView!!.selectedBrushEffect(false, true, false, false, false, false,false,false,false,false)
                neonNormalCheck = true
                neonInnerCheck = false
                neonOuterCheck = false
                neonSolidCheck = false
                brushDialog!!.dismiss()
            }

            neonInner.setOnClickListener {
                drawingView!!.selectedBrushEffect(false, true, false, false, false, false,false,false,false,false)
                neonNormalCheck = false
                neonInnerCheck = true
                neonOuterCheck = false
                neonSolidCheck = false
                brushDialog!!.dismiss()
            }
            neonOuter.setOnClickListener {
                drawingView!!.selectedBrushEffect(false, true, false, false, false, false,false,false,false,false)
                neonNormalCheck = false
                neonInnerCheck = false
                neonOuterCheck = true
                neonSolidCheck = false
                brushDialog!!.dismiss()
            }
            neonSolid.setOnClickListener {
                drawingView!!.selectedBrushEffect(false, true, false, false, false, false,false,false,false,false)
                neonNormalCheck = false
                neonInnerCheck = false
                neonOuterCheck = false
                neonSolidCheck = true
                brushDialog!!.dismiss()
            }
            airBrush.setOnClickListener {
                drawingView!!.selectedBrushEffect(false, false, false, false, true, false,false,false,false,false)
                drawingView!!.setSizeForBrush(1.6f)
                brushDialog!!.dismiss()
            }

            dash.setOnClickListener {
                drawingView!!.selectedBrushEffect(true, false, false, false, false, false,false,false,false,false)
                brushDialog!!.dismiss()
            }
            circle.setOnClickListener {
                drawingView!!.selectedBrushEffect(false, false, true, false, false, false,false,false,false,false)
                brushDialog!!.dismiss()
            }
            rectngale.setOnClickListener {
                drawingView!!.selectedBrushEffect(false, false, false, true, false, false, false,false,false,false)
                brushDialog!!.dismiss()
            }
            line.setOnClickListener {
                drawingView!!.selectedBrushEffect(false, false, false, false, false, true, false,false,false,false)
                brushDialog!!.dismiss()
            }
            roundRect.setOnClickListener {
                drawingView!!.selectedBrushEffect(false, false, false, false, false, true,true,false,false,false)
                brushDialog!!.dismiss()
            }
            oval.setOnClickListener {
                drawingView!!.selectedBrushEffect(false, false, false, false, false, true,false,true,false,false)
                brushDialog!!.dismiss()
            }
            textDraw.setOnClickListener {
                drawingView!!.selectedBrushEffect(false, false, false, false, false, true,false,false,true,false)
                brushDialog!!.dismiss()
            }
            fillCircle.setOnClickListener {
                drawingView!!.selectedBrushEffect(false, false, true, false, false, false,false,false,false,true)
                brushDialog!!.dismiss()
            }
            fillRect.setOnClickListener {
                drawingView!!.selectedBrushEffect(false, false, false, true, false, false,false,false,false,true)
                brushDialog!!.dismiss()
            }
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    progressCount.setText(p1.toString())
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {

                }

                override fun onStopTrackingTouch(p0: SeekBar?) {
                    drawingView?.setSizeForBrush(p0!!.progress.toFloat())
                }

            })
            brushDialog!!.show()
        }
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
            //drawingView?.setColor(colorTag)
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

                    val cw = ContextWrapper(applicationContext)
                    val directory : File =cw.getDir("profile", Context.MODE_PRIVATE)
                    if(!directory.exists()){
                        directory.mkdir()
                    }
                    val file = File(directory,"${System.currentTimeMillis()}.png")
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

    fun colorPickerDialog() {
        if (colorPickerDialog != null) {
            colorPickerDialog!!.show()
        } else {
            colorPickerDialog = Dialog(this@MainActivity)
            colorPickerDialog!!.window?.requestFeature(Window.FEATURE_NO_TITLE)
            colorPickerDialog!!.setContentView(R.layout.activity_color_picker)
            colorPickerDialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            colorPickerDialog!!.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            val colorPicker = colorPickerDialog!!.findViewById<KavehColorPicker>(R.id.colorPickerView)
            val hueSlider = colorPickerDialog!!.findViewById<KavehHueSlider>(R.id.hueSlider)
            val colorAlphaSlider =
                colorPickerDialog!!.findViewById<KavehColorAlphaSlider>(R.id.colorAlphaSlider)
            val txtColor = colorPickerDialog!!.findViewById<TextView>(R.id.txtColor)
            val bgColor = colorPickerDialog!!.findViewById<CardView>(R.id.bgColor)
            val closeBtn = colorPickerDialog!!.findViewById<TextView>(R.id.closeBtn)
            val colorBoxGrid = colorPickerDialog!!.findViewById<GridView>(R.id.colorBoxGrid)
            val addColorBtn = colorPickerDialog!!.findViewById<ImageView>(R.id.addColorBtn)

            colorPicker.alphaSliderView = colorAlphaSlider
            colorPicker.hueSliderView = hueSlider

            try {
                val myColorListString: String? =
                    PreferenceUtil.getInstance(this@MainActivity).getString("colorList")

                val typeToken = object : TypeToken<List<Int>>() {}.type

                val arrayList = Gson().fromJson<List<Int>>(myColorListString, typeToken)

                val colorAdapter = ColorBoxAdapter(arrayList, this@MainActivity)
                colorBoxGrid.adapter = colorAdapter

                colorPicker.setOnColorChangedListener { color ->
                    val hex = Integer.toHexString(color)
                    txtColor.text = "#" + hex
                    currentColor = "#" + hex
                    bgColor.setCardBackgroundColor(color)
                    Log.e("ColorPicker", "onCreate: " + hex)
                }

                addColorBtn.setOnClickListener {
                    val colorToAdd = colorPicker.color
                    colorAdapter.addAll(colorToAdd)
                }

                closeBtn.setOnClickListener {
                    val selectColor =
                        PreferenceUtil.getInstance(this@MainActivity).getString("latestColor")
                    if (selectColor != null && selectColor != "") {
                        drawingView?.setColor(selectColor)
                        PreferenceUtil.getInstance(this@MainActivity).setString("latestColor", "")
                        colorPickerDialog!!.dismiss()
                    } else {
                        drawingView?.setColor(currentColor)
                        colorPickerDialog!!.dismiss()
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "colorPickerDialog: " + e.message)
            }

            colorPickerDialog!!.show()
        }
    }
}