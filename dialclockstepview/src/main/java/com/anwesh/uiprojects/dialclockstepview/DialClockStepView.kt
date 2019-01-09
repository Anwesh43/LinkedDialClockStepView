package com.anwesh.uiprojects.dialclockstepview

/**
 * Created by anweshmishra on 09/01/19.
 */

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color

val nodes : Int = 5
val dials : Int = 3
val scGap : Float = 0.05f
val scDiv : Double = 0.51
val sizeFactor : Float = 2.6f
val strokeFactor : Int = 90
val foreColor : Int = Color.parseColor("#4527A0")
val backColor : Int = Color.parseColor("#BDBDBD")

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.scaleFactor() : Float = Math.floor(this / scDiv).toFloat()
fun Float.mirrorScale(a : Int, b : Int) : Float = (1 - scaleFactor()) * a.inverse() + scaleFactor() * b.inverse()
fun Float.updateScale(dir : Float, a : Int, b : Int) : Float = mirrorScale(a, b) * dir * scGap

fun Canvas.drawDial(x : Float, y : Float, r : Float, deg : Float, paint : Paint) {
    save()
    translate(x, y)
    drawCircle(0f, 0f, r, paint)
    rotate(deg)
    drawLine(0f, 0f, 0f,-2 * r / 3, paint)
    restore()

}

fun Canvas.drawDCNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = w / (nodes + 1)
    val size : Float = gap / sizeFactor
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    val gapDeg : Float = 360f / dials
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = Math.min(w, h) / 60
    paint.strokeCap = Paint.Cap.ROUND
    paint.color = foreColor
    save()
    translate(gap * (i + 1), h/2)
    drawDial(0f, 0f, size, 360f * sc2, paint)
    for (j in 0..(dials - 1)) {
        val sc : Float = sc1.divideScale(j, dials)
        save()
        rotate(gapDeg / 4 + (gapDeg) * j)
        drawDial(size, 0f, size/4, 360f * sc, paint)
        restore()
    }
    restore()
}

class DialClockStepView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }
}