package com.example.battlerunner

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

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
    }
}