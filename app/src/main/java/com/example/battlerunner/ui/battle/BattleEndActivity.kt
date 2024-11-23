package com.example.battlerunner.ui.battle

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.battlerunner.GlobalApplication
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

    // BattleViewModel 가져오기
    private val battleViewModel: BattleViewModel by lazy {
        (application as GlobalApplication).battleViewModel
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBattleEndBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d("BattleViewModel", "BattleEndActivity ViewModel instance: $this")


        // MapFragment 초기화 및 설정
        supportFragmentManager.beginTransaction()
            .replace(R.id.mapFragmentContainer, mapFragment)
            .commitNow()

        val oppositName = intent.getStringExtra("oppositName")
        val userName = intent.getStringExtra("userName")
        val userId = intent.getStringExtra("userId")

        // 문자 설정
        binding.title.text = oppositName + "님과의 배틀 결과" // "{$oppositName}님과의 배틀 결과"
        // TODO: 승리, 패배 문구 설정
        binding.result.text = userName + "님의 승리"   // "{$userName}님의 승리/패배"

        // MapFragment 준비 후 그리드 표시
        mapFragment.setOnMapReadyCallback {
            mapFragment.enableMyLocation()
            mapFragment.moveToCurrentLocationImmediate()

            // BattleViewModel에서 소유권 데이터를 가져와 그리드 표시
            val gridData = battleViewModel.gridPolygons.value ?: emptyList()
            mapFragment.drawGridFromPolygons(gridData, battleViewModel.ownershipMap)
        }

        // 창닫기 버튼 클릭 리스너
        binding.closeBtn.setOnClickListener{
            // MainActivity -> MatchingFragment 표시하도록 요청
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP // 기존 MainActivity 인스턴스 재사용
                putExtra("showMatchingFragment", true) // MatchingFragment 표시 요청
            }
            battleViewModel.clearGrid()
            startActivity(intent)
            finish() // Activity 종료
        }
    }
}