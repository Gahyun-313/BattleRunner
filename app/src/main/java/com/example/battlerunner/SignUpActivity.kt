package com.example.battlerunner

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import java.util.regex.Pattern

class SignUpActivity : AppCompatActivity() {

    // DBHelper 싱글턴 인스턴스를 저장할 변수
    private lateinit var dbHelper: DBHelper

    // xml 내의 뷰를 다룰 변수 선언
    lateinit var editTextId: EditText
    lateinit var editTextPassword: EditText
    lateinit var editTextRePassword: EditText
    lateinit var editTextNick: EditText
    lateinit var btnRegister: Button
    lateinit var btnCheckId: Button
    var checkId: Boolean = false

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // DBHelper 싱글턴 인스턴스를 가져와 초기화
        dbHelper = DBHelper.getInstance(this)

        // 각 변수에 xml 내의 뷰 연결
        editTextId = findViewById(R.id.editTextId)
        editTextPassword = findViewById(R.id.editTextPassword)
        editTextRePassword = findViewById(R.id.editTextRePassword)
        editTextNick = findViewById(R.id.editTextNick)
        btnRegister = findViewById(R.id.btnRegister)
        btnCheckId = findViewById(R.id.btnCheckId)

        // 이전으로 돌아가기 버튼 클릭 이벤트 처리
        findViewById<Button>(R.id.goBackBtn).setOnClickListener {
            val intent = Intent(this@SignUpActivity, Login2Activity::class.java)
            startActivity(intent)
            finish()
        }

        // 아이디 중복확인
        btnCheckId.setOnClickListener {
            val user = editTextId.text.toString()
            val idPattern = "^(?=.*[A-Za-z])(?=.*[0-9])[A-Za-z[0-9]]{6,15}$"

            if (user.isEmpty()) {
                Toast.makeText(this@SignUpActivity, "아이디를 입력해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                if (Pattern.matches(idPattern, user)) {
                    val checkUsername = dbHelper.checkUser(user) // 중복 아이디 존재: true, 미존재: false
                    Log.d("check user name", "$checkUsername")
                    if (!checkUsername) { // 중복 아이디 미존재
                        checkId = true // 중복 확인 완료
                        Toast.makeText(this@SignUpActivity, "사용 가능한 아이디입니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@SignUpActivity, "이미 존재하는 아이디입니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@SignUpActivity, "아이디 형식이 옳지 않습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 회원가입 완료 버튼 클릭 시
        btnRegister.setOnClickListener {
            val user = editTextId.text.toString()
            val pass = editTextPassword.text.toString()
            val repass = editTextRePassword.text.toString()
            val nick = editTextNick.text.toString()
            val pwPattern = "^(?=.*[A-Za-z])(?=.*[0-9])[A-Za-z[0-9]]{8,15}$" // 비밀번호 조건 설정

            // 사용자 입력이 비었을 때
            if (user.isEmpty() || pass.isEmpty() || repass.isEmpty() || nick.isEmpty()) {
                Toast.makeText(this@SignUpActivity, "회원정보를 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                // 아이디 중복 확인이 됐을 때
                if (checkId) {
                    // 비밀번호 형식이 맞을 때
                    if (Pattern.matches(pwPattern, pass)) {
                        // 비밀번호 재확인 성공
                        if (pass == repass) {
                            val insert = dbHelper.insertData(user, pass, nick)

                            // insert 잘 됐는지 확인
                            if (insert) {
                                Log.d("DBInsert", "Data inserted successfully: {$user, $nick}")
                                Toast.makeText(this@SignUpActivity, "회원가입 성공", Toast.LENGTH_SHORT).show()

                                // SharedPreferences에 userId 저장
                                val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                                val editor = sharedPref.edit()
                                editor.putString("userId", user)  // userId에 사용자 ID 저장
                                editor.apply()

                                // MainActivity 이동
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()  // 현재 Activity 종료
                            } else {
                                Toast.makeText(this@SignUpActivity, "가입 실패하였습니다.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@SignUpActivity, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@SignUpActivity, "비밀번호 형식이 옳지 않습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@SignUpActivity, "아이디 중복확인을 해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
