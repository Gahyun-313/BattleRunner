package com.example.battlerunner.ui.login

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.battlerunner.data.local.DBHelper
import com.example.battlerunner.ui.main.MainActivity
import com.example.battlerunner.R
import com.example.battlerunner.data.repository.LoginRepository

class Login2Activity : AppCompatActivity() {

    // xml 내의 뷰를 다룰 변수 선언
    private lateinit var editTextId: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var viewModel: Login2ViewModel

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login2)

        viewModel = ViewModelProvider(this).get(Login2ViewModel::class.java)

        editTextId = findViewById(R.id.editTextId)
        editTextPassword = findViewById(R.id.editTextPassword)

        // 이전으로 돌아가기 버튼 클릭 이벤트 처리
        findViewById<Button>(R.id.goBackBtn).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // 회원가입 버튼 클릭 이벤트 처리
        findViewById<Button>(R.id.btnRegister).setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }

        // 로그인 버튼 클릭
        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            val userId = editTextId.text.toString()
            val userPassword = editTextPassword.text.toString()

            if (userId.isEmpty() || userPassword.isEmpty()) {
                Toast.makeText(this, "아이디와 비밀번호를 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                // ViewModel을 통해 자체 로그인 요청
                viewModel.performCustomLogin(userId, userPassword)
            }
        }

        // 로그인 상태 관찰
        viewModel.loginStatus.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        // 오류 메시지 관찰
        viewModel.errorMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}