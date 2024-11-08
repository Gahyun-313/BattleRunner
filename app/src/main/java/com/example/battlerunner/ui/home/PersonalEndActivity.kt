package com.example.battlerunner

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.battlerunner.databinding.ActivityPersonalEndBinding

class PersonalEndActivity : AppCompatActivity() {

    // ViewBinding을 위한 변수 선언
    private lateinit var binding: ActivityPersonalEndBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalEndBinding.inflate(layoutInflater)
        setContentView(binding.root)  // 레이아웃을 화면에 표시
    }
}