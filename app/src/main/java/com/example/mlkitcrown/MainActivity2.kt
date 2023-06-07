package com.example.mlkitcrown

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class MainActivity2 : AppCompatActivity(), GestureDetector.OnGestureListener {
    private var imageView: ImageView? = null
    private var gestureDetector: GestureDetector? = null
    private var animation: Animation? = null

    @SuppressLint("MissingInflatedId", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        imageView = findViewById<ImageView>(R.id.main2ImageView)
        gestureDetector = GestureDetector(this, this)
        animation = AnimationUtils.loadAnimation(this, R.anim.rotate_animation)
        imageView?.setOnTouchListener { v, event ->
            gestureDetector!!.onTouchEvent(event)
        }
    }

    override fun onDown(e: MotionEvent): Boolean {
        return true
    }

    @SuppressLint("ObjectAnimatorBinding")
   override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        val centerX: Float = imageView!!.getX() + imageView!!.getWidth() / 2
        val centerY: Float = imageView!!.getY() + imageView!!.getHeight() / 2
        val startAngle = 0f
        val endAngle = 360f
        val rotation: ObjectAnimator = ObjectAnimator.ofFloat(imageView, "rotation", startAngle, endAngle)
        rotation.setDuration(1000)
        val translationX: ObjectAnimator = ObjectAnimator.ofFloat(imageView, "translationX", centerX)
        val translationY: ObjectAnimator = ObjectAnimator.ofFloat(imageView, "translationY", centerY)
        val translation = AnimatorSet()
        translation.playTogether(translationX, translationY)
        translation.setDuration(1000)
        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(translation, rotation)
        animatorSet.start()
        return true
    }


    override  fun onLongPress(e: MotionEvent) {}

    override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        return false
    }

    override fun onShowPress(e: MotionEvent) {}

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return false
    }
}