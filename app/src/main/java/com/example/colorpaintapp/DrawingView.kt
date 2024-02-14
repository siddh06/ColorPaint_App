package com.example.colorpaintapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

class DrawingView(context : Context, attrs : AttributeSet) : View(context, attrs) {

    private var mDrawPath : CustomPath? = null
    private var mCanvasBitmap : Bitmap? = null
    private var mDrawPaint : Paint? = null
    private var mCanvasPaint : Paint? = null
    private var mBrushSize : Float = 0.toFloat()
    private var color = Color.BLACK
    private var canvas : Canvas? = null
    private val mPathList = ArrayList<CustomPath>()
    private val mUndoList = ArrayList<CustomPath>()

    init {
        setUpDrawing()
    }

    private fun setUpDrawing() {
        mDrawPaint = Paint()
        mDrawPath = CustomPath(color, mBrushSize)
        mDrawPaint!!.color = color
        mDrawPaint!!.style = Paint.Style.STROKE
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND
        mCanvasPaint = Paint(Paint.DITHER_FLAG)
        //mBrushSize = 20.toFloat()
    }

    internal inner class CustomPath(var color : Int, var brushThikNess : Float) : Path(){

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvasBitmap!!)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(mCanvasBitmap!!,0f,0f, mCanvasPaint)
        for(path in mPathList){
            mDrawPaint!!.strokeWidth = path.brushThikNess
            mDrawPaint!!.color = path.color
            canvas.drawPath(path,mDrawPaint!!)
        }
        mDrawPaint!!.strokeWidth = mDrawPath!!.brushThikNess
        mDrawPaint!!.color = mDrawPath!!.color
        canvas.drawPath(mDrawPath!!,mDrawPaint!!)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y

        when(event?.action){
            MotionEvent.ACTION_DOWN ->{
                mDrawPath!!.brushThikNess = mBrushSize
                mDrawPath!!.color = color

                mDrawPath!!.reset()
                mDrawPath!!.moveTo(touchX!!, touchY!!)
            }
            MotionEvent.ACTION_MOVE ->{
                mDrawPath!!.lineTo(touchX!!, touchY!!)
            }
            MotionEvent.ACTION_UP ->{
                mPathList.add(mDrawPath!!)
                mDrawPath = CustomPath(color, mBrushSize)
            }
            else ->{
                return false
            }
        }
        invalidate()
        return true
    }

    fun setSizeForBrush(newSize : Float){
        mBrushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,newSize,resources.displayMetrics)
        mDrawPaint!!.strokeWidth = mBrushSize
    }

    fun setColor(newColor : String){
        color = Color.parseColor(newColor)
        mDrawPaint!!.color = color
    }

    fun onClickUndo(){
        if(mPathList.size > 0){
            mUndoList.add(mPathList.removeAt(mPathList.size-1))
            invalidate()
        }
    }
}