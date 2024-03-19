package com.example.colorpaintapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.EmbossMaskFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathEffect
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
    private var p1: Point2D? = null
    private var p2: Point2D? = null
    private val mPathList = ArrayList<CustomPath>()
    private val mUndoList = ArrayList<CustomPath>()
    private var blurMaskFilter : BlurMaskFilter? = null
    private var embossMaskFilter : EmbossMaskFilter? = null
    private var isBlurPathEffect = false
    private var isDashPathEffect = false
    private var isCirclePathEffect = false
    private var isRectanglePathEffect = false

    init {
        setUpDrawing()
    }

    private fun setUpDrawing() {
        mDrawPaint = Paint()
        mDrawPath = CustomPath(color, mBrushSize, isDashPathEffect, isBlurPathEffect, isCirclePathEffect, isRectanglePathEffect, p1, p2)
        mDrawPaint!!.color = color
        mDrawPaint!!.style = Paint.Style.STROKE
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND
        mDrawPaint!!.strokeWidth = 5F
        mCanvasPaint = Paint(Paint.DITHER_FLAG)
    }

    internal inner class CustomPath(val color : Int, val brushThikNess : Float, var isDashPath : Boolean, var isBlurPath : Boolean, val isCirclePath : Boolean, val isRectanglePath : Boolean, var p1 : Point2D?, var p2 : Point2D?) : Path(){

    }

    internal inner class Point2D(var x : Float, var y : Float){

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
            if(path.isDashPath){
                mDrawPaint!!.setPathEffect(DashPathEffect(floatArrayOf(10f, 50f), 0f))
            }else{
                mDrawPaint!!.setPathEffect(null)
            }
            if(path.isBlurPath){
                blurMaskFilter = BlurMaskFilter(4f, BlurMaskFilter.Blur.NORMAL)
                mDrawPaint!!.setMaskFilter(blurMaskFilter)
            }else{
                mDrawPaint!!.setMaskFilter(null)
            }
            if(path.isCirclePath){
                canvas.drawCircle(path.p1!!.x, path.p1!!.y, path.p2!!.x/2, mDrawPaint!!)
            }else if(path.isRectanglePath){
                canvas.drawRect(path.p1!!.x, path.p1!!.y, path.p2!!.x, path.p2!!.y, mDrawPaint!!)
            }else{
                canvas.drawPath(path, mDrawPaint!!)
            }
        }

        mDrawPaint!!.strokeWidth = mDrawPath!!.brushThikNess
        mDrawPaint!!.color = mDrawPath!!.color
        if(isDashPathEffect){
            mDrawPaint!!.setPathEffect(DashPathEffect(floatArrayOf(10f, 50f), 0f))
        }else{
            mDrawPaint!!.setPathEffect(null)
        }
        if(isCirclePathEffect && p1 != null && p2 != null){
            canvas.drawCircle(p1!!.x, p1!!.y, p2!!.x/2, mDrawPaint!!)
        }else if(isRectanglePathEffect && p1 != null && p2 != null){
            canvas.drawRect(p1!!.x, p1!!.y, p2!!.x, p2!!.y, mDrawPaint!!)
        }else{
            canvas.drawPath(mDrawPath!!,mDrawPaint!!)
        }

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y

        when(event?.action){
            MotionEvent.ACTION_DOWN ->{

                mDrawPath!!.reset()
                /* mDrawPath!!.brushThikNess = mBrushSize
                 mDrawPath!!.color = color
                 mDrawPath!!.isDashPath = isDashPathEffect
                 mDrawPath!!.isBlurPath = isBlurPathEffect
                 mDrawPath!!.isCirclePath = isCirclePathEffect
                 mDrawPath!!.isRectanglePath = isRectanglePathEffect*/

                p1 = Point2D(touchX!!, touchY!!)

                mDrawPath!!.moveTo(touchX, touchY)
            }
            MotionEvent.ACTION_MOVE ->{
                p2 = Point2D(touchX!!, touchY!!)
                mDrawPath!!.lineTo(touchX, touchY)
            }
            MotionEvent.ACTION_UP ->{
                if(isCirclePathEffect || isRectanglePathEffect){
                    mDrawPath = CustomPath(color, mBrushSize, isDashPathEffect, isBlurPathEffect, isCirclePathEffect, isRectanglePathEffect, p1, p2)
                    mPathList.add(mDrawPath!!)
                }else{
                    mDrawPath!!.isDashPath = isDashPathEffect
                    mDrawPath!!.isBlurPath = isBlurPathEffect
                    mPathList.add(mDrawPath!!)
                    mDrawPath = CustomPath(color, mBrushSize, isDashPathEffect, isBlurPathEffect, isCirclePathEffect, isRectanglePathEffect, p1, p2)
                }
                p1 = null
                p2 = null
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

    fun onClickRedo(){
        if(mUndoList.size > 0) {
            mPathList.add(mUndoList.removeAt(mUndoList.size-1))
            invalidate()
        }
    }

    fun selectedBrushEffect(dashPath : Boolean, blurPath : Boolean, circle : Boolean, rectangle : Boolean){
        isDashPathEffect = dashPath
        isBlurPathEffect = blurPath
        isCirclePathEffect = circle
        isRectanglePathEffect = rectangle
    }
}