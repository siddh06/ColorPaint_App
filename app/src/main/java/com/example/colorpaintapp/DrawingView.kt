package com.example.colorpaintapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.EmbossMaskFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import kotlin.random.Random


class DrawingView(context : Context, attrs : AttributeSet) : View(context, attrs) {

    private var mDrawPath : CustomPath? = null
    private var mDrawPath2 : CustomPath? = null
    private var mCanvasBitmap : Bitmap? = null
    private var mDrawPaint : Paint? = null
    private var mCanvasPaint : Paint? = null
    private var mBrushSize : Float = 0.toFloat()
    private var color = Color.BLACK
    private var canvas : Canvas? = null
    private var p1: Point2D? = null
    private var p2: Point2D? = null
    private var mPathList = ArrayList<CustomPath>()
    private val mUndoList = ArrayList<CustomPath>()
    private var blurMaskFilter : BlurMaskFilter? = null
    private var embossMaskFilter : EmbossMaskFilter? = null
    private var isBlurPathEffect = false
    private var isDashPathEffect = false
    private var isCirclePathEffect = false
    private var isRectanglePathEffect = false
    private var isAirBrushPathEffect = false
    private var isLinePathEffect = false
    private var isRoundRectangleEffect = false
    private var isOvalPathEffect = false
    private var isDrawTextEffect = false
    private var fillColorEffect = false
    private var neonType = ""
    private var random = Random
    private val SPRAY_DENSITY = 20
    private val SPRAY_RADIUS = 60
    private val GRID_SIZE = 10
    private var textureBitmap : Bitmap? = null
    private var shader : Shader? = null
    private var listOfPoints : ArrayList<Point2D>? = null
    private val listOfOffset : List<Point2D> = listOf(Point2D(-15.7673f, 25.8601f),Point2D(-29.3538f, -20.3201f),Point2D(27.0940f, 0.6251f),Point2D(-13.3688f, 4.5874f),Point2D(22.0719f, -6.8271f),Point2D(11.4501f, -19.3021f),Point2D(-6.4934f, -11.8761f),Point2D(28.9544f, 3.68709f),Point2D(-21.7519f, -0.1487f),Point2D(7.7333f, 29.7220f),Point2D(-27.0243f, -28.1590f),Point2D(15.4588f, 3.8495f),Point2D(-23.0933f, 1.4113f),Point2D(2.3942f, -6.4784f),Point2D(19.8658f, -1.3017f),Point2D(-1.9361f, -29.5329f),Point2D(12.8851f, -10.1953f),Point2D(3.1470f, -23.1165f),Point2D(-25.7862f, -20.7603f),Point2D(-19.3675f, 10.4210f))

    init {
        setUpDrawing()
    }

    private fun setUpDrawing() {
        mDrawPaint = Paint()
        mDrawPath = CustomPath(color, mBrushSize, isDashPathEffect, isBlurPathEffect, isCirclePathEffect, isRectanglePathEffect, isAirBrushPathEffect, isLinePathEffect, isRoundRectangleEffect, isOvalPathEffect, isDrawTextEffect, fillColorEffect, p1, p2, null)
        mDrawPath2 = CustomPath(color, mBrushSize, isDashPathEffect, isBlurPathEffect, isCirclePathEffect, isRectanglePathEffect, isAirBrushPathEffect, isLinePathEffect, isRoundRectangleEffect, isOvalPathEffect, isDrawTextEffect, fillColorEffect, p1, p2, null)
        mDrawPaint!!.color = color
        mDrawPaint!!.style = Paint.Style.STROKE
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND
        mDrawPaint!!.strokeWidth = 0.8F
        mDrawPaint!!.isAntiAlias = true
        mCanvasPaint = Paint(Paint.DITHER_FLAG)
        listOfPoints = ArrayList()
        mDrawPaint!!.setShadowLayer(10f, 5f, 5f, Color.GRAY);
        mDrawPaint!!.isDither = true
        //textureBitmap = BitmapFactory.decodeResource(resources, R.drawable.pen)
        //shader = BitmapShader(textureBitmap!!, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        //mDrawPaint!!.shader = shader
    }

    internal inner class CustomPath(var color : Int, var brushThikNess : Float, var isDashPath : Boolean, var isBlurPath : Boolean,
                                    val isCirclePath : Boolean, val isRectanglePath : Boolean, val isAirBrushPath : Boolean,
                                    val isLinePath : Boolean,val isRoundRectanglePath : Boolean,val isOvalPath : Boolean,
                                    val isDrawTextPath : Boolean,val fillColor : Boolean, var p1 : Point2D?, var p2 : Point2D?, val pointList : List<Point2D>?,
                                    var neonType : String = "") : Path(){

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

        /*for(path in mPathList){
            mDrawPaint!!.strokeWidth = path.brushThikNess
            mDrawPaint!!.color = path.color
            if(path.isDashPath){
                mDrawPaint!!.setPathEffect(DashPathEffect(floatArrayOf(10f, 50f), 0f))
            }else{
                mDrawPaint!!.setPathEffect(null)
            }
            if(path.isBlurPath){
                if(MainActivity.neonSolidCheck){
                    blurMaskFilter = BlurMaskFilter(20f, BlurMaskFilter.Blur.SOLID)
                }else if(MainActivity.neonInnerCheck){
                    blurMaskFilter = BlurMaskFilter(20f, BlurMaskFilter.Blur.INNER)
                }else if(MainActivity.neonOuterCheck){
                    blurMaskFilter = BlurMaskFilter(20f, BlurMaskFilter.Blur.OUTER)
                }else{
                    blurMaskFilter = BlurMaskFilter(20f, BlurMaskFilter.Blur.NORMAL)
                }
                mDrawPaint!!.setMaskFilter(blurMaskFilter)
            }else{
                mDrawPaint!!.setMaskFilter(null)
            }
            if(path.isCirclePath){
                canvas.drawCircle(path.p1!!.x, path.p1!!.y, path.p2!!.x/2, mDrawPaint!!)
            }else if(path.isRectanglePath){
                canvas.drawRect(path.p1!!.x, path.p1!!.y, path.p2!!.x, path.p2!!.y, mDrawPaint!!)
            }*//*else if(path.isAirBrushPath && path.pointList!!.isNotEmpty()){
                val list = path.pointList
                for (i in 0 until list.size) {
                    val pp2 = list.get(i)
                    sprayPaint(pp2.x, pp2.y)
                }
            }*//*else{
                canvas.drawPath(path, mDrawPaint!!)
            }
        }*/

        mDrawPaint!!.strokeWidth = mBrushSize
        mDrawPaint!!.color = color
        if(fillColorEffect){
            mDrawPaint!!.style = Paint.Style.FILL
        }else{
            mDrawPaint!!.style = Paint.Style.STROKE
        }
        if(isDashPathEffect){
            mDrawPaint!!.setPathEffect(DashPathEffect(floatArrayOf(10f + mBrushSize, 50f + mBrushSize), 0f))
        }else{
            mDrawPaint!!.setPathEffect(null)
        }
        if(isBlurPathEffect){
            if(MainActivity.neonSolidCheck){
                blurMaskFilter = BlurMaskFilter(20f, BlurMaskFilter.Blur.SOLID)
            }else if(MainActivity.neonInnerCheck){
                blurMaskFilter = BlurMaskFilter(20f, BlurMaskFilter.Blur.INNER)
            }else if(MainActivity.neonOuterCheck){
                blurMaskFilter = BlurMaskFilter(20f, BlurMaskFilter.Blur.OUTER)
            }else{
                blurMaskFilter = BlurMaskFilter(20f, BlurMaskFilter.Blur.NORMAL)
            }
            mDrawPaint!!.setMaskFilter(blurMaskFilter)
        }else{
            mDrawPaint!!.setMaskFilter(null)
        }

        if(isCirclePathEffect && p1 != null && p2 != null){
            canvas.drawCircle(p1!!.x, p1!!.y, p2!!.x/2, mDrawPaint!!)
        }else if(isRectanglePathEffect && p1 != null && p2 != null){
            canvas.drawRect(p1!!.x, p1!!.y, p2!!.x, p2!!.y, mDrawPaint!!)
        }else if(isRoundRectangleEffect && p1 != null && p2 != null){
            var rectF = RectF(p1!!.x, p1!!.y, p2!!.x, p2!!.y)
            canvas.drawRoundRect(rectF,50f,50f,mDrawPaint!!)
        }else if(isOvalPathEffect && p1 != null && p2 != null){
            var rectF = RectF(p1!!.x, p1!!.y, p2!!.x, p2!!.y)
            canvas.drawOval(rectF, mDrawPaint!!)
        }else if(isDrawTextEffect && p1 != null && p2 != null){
            canvas.drawText("Siddhesh", p2!!.x, p2!!.y, mDrawPaint!!)
        }else if(isLinePathEffect && p1 != null && p2 != null){
            canvas.drawLine(p1!!.x,p1!!.y,p2!!.x,p2!!.y,mDrawPaint!!)
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
                mDrawPath2!!.reset()
                /* mDrawPath!!.brushThikNess = mBrushSize
                 mDrawPath!!.color = color
                 mDrawPath!!.isDashPath = isDashPathEffect
                 mDrawPath!!.isBlurPath = isBlurPathEffect
                 mDrawPath!!.isCirclePath = isCirclePathEffect
                 mDrawPath!!.isRectanglePath = isRectanglePathEffect*/

                p1 = Point2D(touchX!!, touchY!!)

                if(isCirclePathEffect || isRectanglePathEffect || isLinePathEffect){
                    mDrawPath2!!.moveTo(touchX, touchY)
                }else {
                    mDrawPath!!.moveTo(touchX, touchY)
                }

                if(isAirBrushPathEffect){
                    sprayPaint(touchX, touchY)
                }
            }
            MotionEvent.ACTION_MOVE ->{
                p2 = Point2D(touchX!!, touchY!!)
                if(isCirclePathEffect || isRectanglePathEffect || isAirBrushPathEffect|| isLinePathEffect) {
                    mDrawPath2!!.lineTo(touchX, touchY)
                }else{
                    mDrawPath!!.lineTo(touchX, touchY)
                }
                //spray(touchX, touchY)
                if(isAirBrushPathEffect){
                    sprayPaint(touchX, touchY)
                    listOfPoints!!.add(p2!!)
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP ->{

                if(isCirclePathEffect || isRectanglePathEffect || isAirBrushPathEffect || isLinePathEffect || isRoundRectangleEffect || isOvalPathEffect || isDrawTextEffect){
                    val drawList = listOfPoints!!.toList()
                    mDrawPath2 = CustomPath(
                        color,
                        mBrushSize,
                        isDashPathEffect,
                        isBlurPathEffect,
                        isCirclePathEffect,
                        isRectanglePathEffect,
                        isAirBrushPathEffect,
                        isLinePathEffect,
                        isRoundRectangleEffect,
                        isOvalPathEffect,
                        isDrawTextEffect,
                        fillColorEffect,
                        p1,
                        p2,
                        drawList
                    )
                    mPathList.add(mDrawPath2!!)
                    listOfPoints!!.clear()
                }else{
                    val isBlur = isBlurPathEffect
                    mDrawPath!!.brushThikNess = mBrushSize
                    mDrawPath!!.color = color
                    mDrawPath!!.isDashPath = isDashPathEffect
                    mDrawPath!!.isBlurPath = isBlur
                    if(isBlurPathEffect){
                        if(MainActivity.neonSolidCheck){
                            neonType = "SOLID"
                        }else if(MainActivity.neonInnerCheck){
                            neonType = "INNER"
                        }else if(MainActivity.neonOuterCheck){
                            neonType = "OUTER"
                        }else{
                            neonType = "NORMAL"
                        }
                    }
                    mDrawPath!!.neonType = neonType
                    mPathList.add(mDrawPath!!)
                    mDrawPath = CustomPath(color, mBrushSize, isDashPathEffect, isBlur, isCirclePathEffect, isRectanglePathEffect, isAirBrushPathEffect, isLinePathEffect, isRoundRectangleEffect, isOvalPathEffect, isDrawTextEffect, fillColorEffect, p1, p2, null, neonType)
                }

                setDrawing()
                p1 = null
                p2 = null
            }
            else ->{
                return false
            }
        }
        if(!isAirBrushPathEffect) {
            invalidate()
        }
        return true
    }

    fun setSizeForBrush(newSize : Float){
        //mBrushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,newSize,resources.displayMetrics)
        mBrushSize = newSize
        mDrawPaint!!.strokeWidth = mBrushSize
    }

    fun setColor(newColor : String){
        color = Color.parseColor(newColor)
        mDrawPaint!!.color = color
    }

    fun onClickUndo(){
        if(mPathList.size > 0){
            mUndoList.add(mPathList.removeAt(mPathList.size-1))
            setDrawing()
            invalidate()
        }
    }

    fun onClickRedo(){
        if(mUndoList.size > 0) {
            mPathList.add(mUndoList.removeAt(mUndoList.size-1))
            setDrawing()
            invalidate()
        }
    }

    fun selectedBrushEffect(dashPath : Boolean, blurPath : Boolean, circle : Boolean, rectangle : Boolean, airBrush : Boolean, linePath : Boolean, roundReact : Boolean, oval : Boolean, textDraw : Boolean, fillCircleOrRect : Boolean){
        isDashPathEffect = dashPath
        isBlurPathEffect = blurPath
        isCirclePathEffect = circle
        isRectanglePathEffect = rectangle
        isAirBrushPathEffect = airBrush
        isLinePathEffect = linePath
        isRoundRectangleEffect = roundReact
        isOvalPathEffect = oval
        isDrawTextEffect = textDraw
        fillColorEffect = fillCircleOrRect
    }

    private fun sprayPaint(x: Float, y: Float, pathColor: Int = 0) {
        //Log.e("pointArray", "call: " )
        for (i in 0 until listOfOffset.size) {
           /* val offsetX = (random.nextFloat() - 0.5f) * SPRAY_RADIUS
            val offsetY = (random.nextFloat() - 0.5f) * SPRAY_RADIUS*/
            val point = listOfOffset.get(i)
            val offsetX = point.x
            val offsetY = point.y
            //Log.e("pointArray", "offset X: "+ offsetX )
            //Log.e("pointArray", "offset Y: "+ offsetY )
            if(pathColor == 0) {
                mDrawPaint!!.color = color
            }else{
                mDrawPaint!!.color = pathColor
            }
            canvas!!.drawPoint(x + offsetX, y + offsetY, mDrawPaint!!)
        }
    }

    fun setDrawing(){
        canvas!!.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        for(path in mPathList){
            mDrawPaint!!.strokeWidth = path.brushThikNess
            mDrawPaint!!.color = path.color
            if(path.fillColor){
                mDrawPaint!!.style = Paint.Style.FILL
            }else{
                mDrawPaint!!.style = Paint.Style.STROKE
            }
            if(path.isDashPath){
                mDrawPaint!!.setPathEffect(DashPathEffect(floatArrayOf(10f + path.brushThikNess, 50f + path.brushThikNess), 0f))
            }else{
                mDrawPaint!!.setPathEffect(null)
            }
            if(path.isBlurPath){
                if(path.neonType.contentEquals("SOLID")){
                    blurMaskFilter = BlurMaskFilter(20f, BlurMaskFilter.Blur.SOLID)
                }else if(path.neonType.contentEquals("INNER")){
                    blurMaskFilter = BlurMaskFilter(20f, BlurMaskFilter.Blur.INNER)
                }else if(path.neonType.contentEquals("OUTER")){
                    blurMaskFilter = BlurMaskFilter(20f, BlurMaskFilter.Blur.OUTER)
                }else{
                    blurMaskFilter = BlurMaskFilter(20f, BlurMaskFilter.Blur.NORMAL)
                }
                mDrawPaint!!.setMaskFilter(blurMaskFilter)
            }else{
                mDrawPaint!!.setMaskFilter(null)
            }
            if(path.isCirclePath){
                canvas!!.drawCircle(path.p1!!.x, path.p1!!.y, path.p2!!.x/2, mDrawPaint!!)
            }else if(path.isRectanglePath){
                canvas!!.drawRect(path.p1!!.x, path.p1!!.y, path.p2!!.x, path.p2!!.y, mDrawPaint!!)
            }else if(path.isRoundRectanglePath && path.p1 != null && path.p2 != null){
                val rectF = RectF(path.p1!!.x, path.p1!!.y, path.p2!!.x, path.p2!!.y)
                canvas!!.drawRoundRect(rectF,50f,50f,mDrawPaint!!)
            }else if(path.isOvalPath && path.p1 != null && path.p2 != null){
                val rectF = RectF(path.p1!!.x, path.p1!!.y, path.p2!!.x, path.p2!!.y)
                canvas!!.drawOval(rectF, mDrawPaint!!)
            }else if(path.isDrawTextPath && path.p1 != null && path.p2 != null){
                canvas!!.drawText("Siddhesh", path.p2!!.x, path.p2!!.y, mDrawPaint!!)
            }else if(path.isLinePath){
                canvas!!.drawLine(path.p1!!.x, path.p1!!.y, path.p2!!.x, path.p2!!.y,mDrawPaint!!)
            }else if(path.isAirBrushPath && path.pointList!!.isNotEmpty()){
                val list = path.pointList
                for (i in 0 until list.size) {
                    val pp2 = list.get(i)
                    sprayPaint(pp2.x, pp2.y, path.color)
                }
            }else{
                canvas!!.drawPath(path, mDrawPaint!!)
            }
        }
    }

    private fun spray(x: Float, y: Float) {
        for (i in 0 until GRID_SIZE) {
            for (j in 0 until GRID_SIZE) {
                val offsetX = ((i - GRID_SIZE / 2.0) * (SPRAY_RADIUS / GRID_SIZE)).toFloat()
                val offsetY = ((j - GRID_SIZE / 2.0) * (SPRAY_RADIUS / GRID_SIZE)).toFloat()
                mDrawPath!!.moveTo(x + offsetX, y + offsetY)
                mDrawPath!!.lineTo(x + offsetX + 1, y + offsetY + 1)
            }
        }
    }
}