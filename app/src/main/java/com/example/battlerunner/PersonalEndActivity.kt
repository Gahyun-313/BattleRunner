package com.example.battlerunner

import MapFragment
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

        // HomeFragment에서 전달된 경과 시간 받기
        val elapsedTime = intent.getLongExtra("elapsedTime", 0)

        // 경과된 시간, 분, 초 계산
        val seconds = (elapsedTime / 1000) % 60  // 초 계산
        val minutes = (elapsedTime / (1000 * 60)) % 60  // 분 계산
        val hours = (elapsedTime / (1000 * 60 * 60))  // 시 계산

        // 경과 시간을 텍스트뷰에 표시 (시:분:초 형식)
        binding.todayTime.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)

        // X 버튼 클릭 리스너 추가
        binding.closeBtn.setOnClickListener {
            // 현재 액티비티를 종료하여 HomeFragment로 돌아가게 함
            finish()
        }

        // MapFragment를 FragmentContainerView에 설정
        val mapFragment = MapFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.mapFragmentContainer, mapFragment)
            .commit()
    }
}
