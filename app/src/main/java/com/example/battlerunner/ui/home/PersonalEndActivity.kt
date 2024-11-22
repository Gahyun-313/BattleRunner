package com.example.battlerunner.ui.home

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.battlerunner.GlobalApplication
import com.example.battlerunner.R
import com.example.battlerunner.databinding.ActivityPersonalEndBinding
import com.example.battlerunner.ui.main.MainActivity
import com.example.battlerunner.ui.shared.MapFragment
import com.example.battlerunner.utils.MapUtils

class PersonalEndActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPersonalEndBinding
    private lateinit var mapFragment: MapFragment

    // MainActivity의 HomeViewModel 참조
    private val homeViewModel: HomeViewModel by lazy {
        (application as GlobalApplication).homeViewModel
    }

    @SuppressLint("DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        println("HomeViewModel Instance in PersonalEndActivity: ${homeViewModel.hashCode()}")

        binding = ActivityPersonalEndBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // MapFragment 초기화 및 설정
        mapFragment = MapFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.mapFragmentContainer, mapFragment)
            .commitNow()

        // Intent로 전달받은 데이터 복원 -> 소요 시간, 거리
        val elapsedTime = intent.getLongExtra("elapsedTime", 0L)
        val distance = intent.getFloatExtra("distance", 0f)

        // MapFragment 준비 후 경로 표시
        mapFragment.setOnMapReadyCallback {
            mapFragment.enableMyLocation()
            mapFragment.moveToCurrentLocationImmediate()

            // ViewModel의 pathPoints를 관찰하여 경로 그리기
            homeViewModel.pathPoints.observe(this) { pathPoints ->
                if (pathPoints.isNotEmpty()) {
                    println("Path Points in PersonalEndActivity: ${pathPoints.size}")
                    mapFragment.drawPath(pathPoints)
                } else {
                    println("Path Points in PersonalEndActivity: empty")
                }
            }
        }

        // UI 업데이트
        binding.todayTime.text = String.format(
            "%02d:%02d:%02d",
            elapsedTime / (1000 * 60 * 60),
            (elapsedTime / (1000 * 60)) % 60,
            (elapsedTime / 1000) % 60
        )
        binding.todayDistance.text = String.format("%.2f m", distance)


        // 창닫기 버튼 클릭 리스너
        binding.closeBtn.setOnClickListener {
            // 지도 초기화 전에 경로를 먼저 그리고 데이터를 초기화
            mapFragment.clearMapPath() // 지도에서 경로 제거
            homeViewModel.resetAllData() // ViewModel 데이터 초기화
            setResult(Activity.RESULT_OK) // HomeFragment에 결과 전달
            finish() // Activity 종료
        }

    }
}