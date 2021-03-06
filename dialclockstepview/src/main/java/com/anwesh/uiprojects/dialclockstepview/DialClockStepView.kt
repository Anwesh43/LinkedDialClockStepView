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
val sizeFactor : Float = 2.4f
val strokeFactor : Int = 90
val foreColor : Int = Color.parseColor("#4527A0")
val backColor : Int = Color.parseColor("#BDBDBD")
val dialRFactor : Float = 3.8f

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
    val gap : Float = h / (nodes + 1)
    val size : Float = gap / sizeFactor
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    val gapDeg : Float = 360f / dials
    val dialR : Float = size / dialRFactor
    val kr : Float = size - dialR * 1.5f
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    paint.strokeCap = Paint.Cap.ROUND
    paint.color = foreColor
    save()
    translate(w/2, gap * (i + 1))
    drawDial(0f, 0f, size,360f * sc2, paint)
    for (j in 0..(dials - 1)) {
        val sc : Float = sc1.divideScale(j, dials)
        val currDeg : Float = -gapDeg / 10 + gapDeg * j
        val x : Float = kr * Math.cos(currDeg * Math.PI/180).toFloat()
        val y : Float = kr * Math.sin(currDeg * Math.PI/180).toFloat()
        save()
        drawDial(x, y, dialR, 360f * sc, paint)
        restore()
    }
    restore()
}

class DialClockStepView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scale.updateScale(dir, dials, 1)
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class DCNode(var i : Int, val state : State = State()) {

        private var next : DCNode? = null
        private var prev : DCNode? = null

        fun addNeighbor() {
            if (this.i < nodes - 1) {
                next = DCNode(i + 1)
                next?.prev = this
            }
        }

        init {
            addNeighbor()
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawDCNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : DCNode {
            var curr : DCNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class DialClockStep (var i : Int) {

        private val root : DCNode = DCNode(0)
        private var curr : DCNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : DialClockStepView) {
        private val animator : Animator = Animator(view)
        private val dcs : DialClockStep = DialClockStep(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(backColor)
            dcs.draw(canvas, paint)
            animator.animate {
                dcs.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            dcs.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity : Activity) : DialClockStepView {
            val view : DialClockStepView = DialClockStepView(activity)
            activity.setContentView(view)
            return view
        }
    }
}