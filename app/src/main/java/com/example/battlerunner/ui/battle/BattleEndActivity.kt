package com.example.battlerunner.ui.battle

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.battlerunner.R
import com.example.battlerunner.databinding.ActivityBattleEndBinding
import com.example.battlerunner.ui.shared.MapFragment
import com.example.battlerunner.utils.MapUtils

class BattleEndActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBattleEndBinding
    private var mapFragment: MapFragment = MapFragment()

    @SuppressLint("DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBattleEndBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 창닫기 버튼 클릭 리스너
        binding.closeBtn.setOnClickListener{
            finish() // 액티비티 종료
        }
    }
}