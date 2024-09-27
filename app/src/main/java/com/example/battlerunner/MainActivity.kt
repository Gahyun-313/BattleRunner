package com.example.battlerunner

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.battlerunner.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 여기서 binding을 바로 초기화
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}