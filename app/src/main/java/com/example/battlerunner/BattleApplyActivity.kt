package com.example.battlerunner

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class BattleApplyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_battle_apply)

        // Intent로부터 전달된 사용자 이름 가져오기
        val userName = intent.getStringExtra("userName")

        // 가져온 이름을 텍스트뷰에 표시
        val userNameTextView = findViewById<TextView>(R.id.userNameTextView)
        userNameTextView.text = userName

        // 배틀 신청 버튼
        val applyButton = findViewById<Button>(R.id.battleApplyButton)

        applyButton.setOnClickListener {
            // MainActivity를 시작하면서 BattleFragment로 전환하도록 지시
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP // 기존 MainActivity를 스택 최상단으로
                putExtra("loadBattleFragment", true)
                putExtra("userName", userName)
            }
            startActivity(intent)
            finish()
        }
        // 닫기 버튼 설정
        val closeButton = findViewById<ImageButton>(R.id.closeBtn)
        closeButton.setOnClickListener {
            finish() // 이전 상태로 돌아가기
        }
    }
}