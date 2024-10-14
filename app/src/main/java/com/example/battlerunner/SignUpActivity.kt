package com.example.battlerunner

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import java.util.regex.Pattern
import kotlin.math.log

class SignUpActivity : AppCompatActivity() {

    var DB:DBHelper?=null
    // xml 내의 뷰를 다룰 변수 선언
    lateinit var editTextId: EditText
    lateinit var editTextPassword: EditText
    lateinit var editTextRePassword: EditText
    lateinit var editTextNick: EditText
    lateinit var btnRegister: Button
    lateinit var btnCheckId: Button
    var CheckId:Boolean=false

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        DB = DBHelper(this)
        // 각 변수에 xml 내의 뷰 연결
        editTextId = findViewById(R.id.editTextId)
        editTextPassword = findViewById(R.id.editTextPassword)
        editTextRePassword = findViewById(R.id.editTextRePassword)
        editTextNick = findViewById(R.id.editTextNick)
        btnRegister = findViewById(R.id.btnRegister)
        btnCheckId = findViewById(R.id.btnCheckId)

        // 이전으로 돌아가기 버튼 클릭 이벤트 처리
        findViewById<Button>(R.id.goBackBtn).setOnClickListener {
            // Login2Activity 이동
            val intent = Intent(this@SignUpActivity, Login2Activity::class.java)
            startActivity(intent)
            finish()
        }

        // 아이디 중복확인
        btnCheckId.setOnClickListener {
            val user = editTextId.text.toString()
            val idPattern = "^(?=.*[A-Za-z])(?=.*[0-9])[A-Za-z[0-9]]{6,15}$"

            if (user == "") {
                Toast.makeText(this@SignUpActivity, "아이디를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
            else {
                if (Pattern.matches(idPattern, user)) {
                    val checkUsername = DB!!.checkUser(user)
                    if(checkUsername == false){
                        CheckId = true
                        Toast.makeText(this@SignUpActivity, "사용 가능한 아이디입니다.", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        Toast.makeText(this@SignUpActivity, "이미 존재하는 아이디입니다.", Toast.LENGTH_SHORT).show()
                    }
                }
                else {
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
            val pwPattern = "^(?=.*[A-Za-z])(?=.*[0-9])[A-Za-z[0-9]]{8,15}$" //password 조건 설정

            // 사용자 입력이 비었을 때
            if (user == "" || pass == "" || repass == "" || nick == "") {
                Toast.makeText(this@SignUpActivity, "회원정보를 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
            else {
                // 아이디 중복 확인이 됐을 때
                if (CheckId == true) {
                    // 비밀번호 형식이 맞을 때
                    if (Pattern.matches(pwPattern, pass)) {
                        // 비밀번호 재확인 성공
                        if (pass == repass) {
                            val insert = DB!!.insertData(user, pass, nick)

                            // insert 잘 됐는지 확인
                            if (insert) {
                                Log.d("DBInsert", "Data inserted successfully")
                                Log.d("DBInsert", "{$user, $nick}")
                            } else {
                                Log.d("DBInsert", "Data insertion failed")
                            }

                            // 가입 성공 시
                            if (insert == true) {
                                Toast.makeText(this@SignUpActivity, "가입되었습니다.", Toast.LENGTH_SHORT).show()
                                // MainActivity 이동
                                val intent = Intent(applicationContext, MainActivity::class.java)
                                startActivity(intent)
                            }
                            // 가입 실패 시
                            else {
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