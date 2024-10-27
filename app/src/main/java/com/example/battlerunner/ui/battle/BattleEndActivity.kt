package com.example.battlerunner.ui.battle

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.battlerunner.MapFragment
import com.example.battlerunner.R
import com.example.battlerunner.databinding.ActivityBattleEndBinding

class BattleEndActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBattleEndBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBattleEndBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // X 버튼 클릭 리스너 설정
        binding.closeBtn.setOnClickListener {
            val intent = Intent()
            setResult(RESULT_OK, intent)
            this.finish()
        }

        // MapFragment 추가
        val supportMapFragment = MapFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.mapFragmentContainer, supportMapFragment)
            .commit()

        // BattleFragment에서 전달된 경과 시간 받기
        val elapsedTime = intent.getLongExtra("elapsedTime", 0)

        // 경과된 시간, 분, 초 계산
        val seconds = (elapsedTime / 1000) % 60
        val minutes = (elapsedTime / (1000 * 60)) % 60
        val hours = (elapsedTime / (1000 * 60 * 60))

        // 경과 시간을 텍스트뷰에 표시 (시:분:초 형식)
        binding.todayTime.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}
