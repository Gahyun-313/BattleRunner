package com.example.battlerunner.ui.battle

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.battlerunner.R
import com.example.battlerunner.databinding.ActivityBattleEndBinding
import com.example.battlerunner.ui.main.MainActivity
import com.example.battlerunner.ui.shared.MapFragment
import com.example.battlerunner.utils.MapUtils
import com.google.common.collect.Table
import com.google.gson.Gson

class BattleEndActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBattleEndBinding
    private var mapFragment: MapFragment = MapFragment()

    @SuppressLint("DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBattleEndBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 전달받은 Grid 데이터 JSON 복원
        val gridDataJson = intent.getStringExtra("gridData")
        val gridData = gridDataJson?.let { json ->
            Gson().fromJson(json, List::class.java) as List<Map<String, Any>> // JSON 복원
        } ?: emptyList()

        // MapFragment 초기화 및 설정
        supportFragmentManager.beginTransaction()
            .replace(R.id.mapFragmentContainer, mapFragment)
            .commitNow()

        // MapFragment 준비 후 그리드 표시
        mapFragment.setOnMapReadyCallback {
            mapFragment.enableMyLocation()
            mapFragment.moveToCurrentLocationImmediate()

            // BattleFragment의 그리드를 복원해 MapFragment에 표시
            mapFragment.drawGridFromData(gridData)
        }

        // 창닫기 버튼 클릭 리스너
        binding.closeBtn.setOnClickListener{
            // MainActivity -> MatchingFragment 표시하도록 요청
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP // 기존 MainActivity 인스턴스 재사용
                putExtra("showMatchingFragment", true) // MatchingFragment 표시 요청
            }
            startActivity(intent)
            finish() // Activity 종료
        }
    }
}