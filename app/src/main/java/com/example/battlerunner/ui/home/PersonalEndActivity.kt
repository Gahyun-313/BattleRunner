package com.example.battlerunner.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.battlerunner.R
import com.example.battlerunner.databinding.ActivityPersonalEndBinding
import com.example.battlerunner.ui.shared.MapFragment
import com.example.battlerunner.utils.MapUtils
import com.example.battlerunner.utils.MapUtils.pathPoints

class PersonalEndActivity : AppCompatActivity() {

    // ViewBinding을 위한 변수 선언
    private lateinit var binding: ActivityPersonalEndBinding
    private var mapFragment: MapFragment = MapFragment()

    @SuppressLint("DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPersonalEndBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // MapFragment 초기화 및 설정
        supportFragmentManager.beginTransaction()
            .replace(R.id.mapFragmentContainer, mapFragment)
            .commitNow()

        // Intent로 전달받은 데이터 복원
        val elapsedTime = intent.getLongExtra("elapsedTime", 0L)
        val distance = intent.getFloatExtra("distance", 0f)

        val pathPointsJson = intent.getStringExtra("pathPoints")
        val pathPoints = pathPointsJson?.let { MapUtils.jsonToPathPoints(it) } ?: emptyList()

        // MapFragment 준비 후 경로 표시
        mapFragment.setOnMapReadyCallback {
            mapFragment.enableMyLocation()
            mapFragment.moveToCurrentLocationImmediate()
            mapFragment.drawPath(pathPoints) // 경로 표시
        }

        // UI 업데이트
        binding.todayTime.text = String.format("%02d:%02d:%02d",
            elapsedTime / (1000 * 60 * 60),
            (elapsedTime / (1000 * 60)) % 60,
            (elapsedTime / 1000) % 60)
        binding.todayDistance.text = String.format("%.2f m", distance)

        // 창닫기 버튼 클릭 리스너
        binding.closeBtn.setOnClickListener{
            finish() // 액티비티 종료
        }

    }


}