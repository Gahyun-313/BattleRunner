package com.example.battlerunner.ui.login

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.battlerunner.data.local.DBHelper
import com.example.battlerunner.ui.main.MainActivity
import com.example.battlerunner.R

class Login2Activity : AppCompatActivity() {

    // xml 내의 뷰를 다룰 변수 선언
    lateinit var btnLogin: Button
    lateinit var editTextId: EditText
    lateinit var editTextPassword: EditText
    lateinit var btnRegister: Button

    // DBHelper 싱글턴 인스턴스를 저장할 변수
    private lateinit var dbHelper: DBHelper

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login2)

        // DBHelper 싱글턴 인스턴스를 가져와 초기화
        dbHelper = DBHelper.getInstance(this)

        // 각 변수에 xml 내의 뷰 연결
        btnLogin = findViewById(R.id.btnLogin)
        editTextId = findViewById(R.id.editTextId)
        editTextPassword = findViewById(R.id.editTextPassword)
        btnRegister = findViewById(R.id.btnRegister)

        // 이전으로 돌아가기 버튼 클릭 이벤트 처리
        findViewById<Button>(R.id.goBackBtn).setOnClickListener {
            // Login2Activity 이동
            val intent = Intent(this@Login2Activity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // 회원가입 버튼 클릭 이벤트 처리
        findViewById<Button>(R.id.btnRegister).setOnClickListener {
            // SignUpActivity 이동
            val intent = Intent(this@Login2Activity, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }

        // 로그인 버튼 클릭
        btnLogin.setOnClickListener {
            val user = editTextId.text.toString() // 이게 ID임
            val pass = editTextPassword.text.toString()

            // 빈칸 제출시 Toast
            if (user == "" || pass == "") {
                Toast.makeText(this@Login2Activity, "아이디와 비밀번호를 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
            else {
                // DB에서 사용자 인증 (로그인 정보 확인)
                val checkUserpass = dbHelper.checkUserPass(user, pass)

                // id 와 password 일치시 (로그인 성공 시)
                if (checkUserpass == true) {

                    // SharedPreferences에 userId 저장
                    val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                    val editor = sharedPref.edit()
                    editor.putString("userId", user)  // 로그인한 사용자 ID를 저장
                    editor.apply()

                    Toast.makeText(this@Login2Activity, "로그인 되었습니다.", Toast.LENGTH_SHORT).show()

                    // MainActivity 이동
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
                else {
                    Toast.makeText(this@Login2Activity, "아이디와 비밀번호를 확인해 주세요.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}