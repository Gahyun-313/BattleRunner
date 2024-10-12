package com.example.battlerunner

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

// https://velog.io/@hyhy0623/%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-%EC%8A%A4%ED%8A%9C%EB%94%94%EC%98%A4-%EC%BD%94%ED%8B%80%EB%A6%B0-%EA%B8%B0%EB%B3%B8%EC%A0%81%EC%9D%B8-%EB%A1%9C%EA%B7%B8%EC%9D%B8-%ED%9A%8C%EC%9B%90%EA%B0%80%EC%9E%85-%EA%B5%AC%ED%98%84%ED%95%98%EA%B8%B0

class Login2Activity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login2)

        // 회원가입 버튼 클릭 이벤트 처리
        findViewById<Button>(R.id.signupBtn).setOnClickListener {
            // SignUpActivity 이동
            val intent = Intent(this@Login2Activity, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }

        // 이전으로 돌아가기 버튼 클릭 이벤트 처리
        findViewById<Button>(R.id.goBackBtn).setOnClickListener {
            // Login2Activity 이동
            val intent = Intent(this@Login2Activity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}