package com.example.mlkitcrown

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mlkitcrown.ml_kit.DetectObjectFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction().replace(
            R.id.fragmentContainer, DetectObjectFragment(), "DetectObjectFragment"
        ).commit()
    }
}