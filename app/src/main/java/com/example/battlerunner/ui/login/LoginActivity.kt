package com.example.battlerunner.ui.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.battlerunner.ui.main.MainActivity
import com.example.battlerunner.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class LoginActivity : AppCompatActivity() {

    private lateinit var viewModel: LoginViewModel
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 100 // 구글 로그인 요청 코드

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        // Google 로그인 옵션 설정
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // 자체 로그인 버튼 클릭 리스너
        findViewById<Button>(R.id.login_btn).setOnClickListener {
            // Login2Activity로 이동해 아이디와 비밀번호를 입력받음
            val intent = Intent(this, Login2Activity::class.java)
            startActivity(intent)
            finish()
        }

        // 카카오 로그인 버튼 클릭 리스너
        findViewById<ImageButton>(R.id.kakao_login_btn).setOnClickListener {
            viewModel.handleKakaoLogin(this)
        }

        // 구글 로그인 버튼 클릭 리스너
        findViewById<ImageButton>(R.id.google_login_btn).setOnClickListener {
            signInWithGoogle()
        }

        // 로그인 상태 관찰
        viewModel.loginStatus.observe(this, Observer { status ->
            if (status) moveToMainActivity()
        })

        // 오류 메시지 관찰
        viewModel.errorMessage.observe(this, Observer { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        })
    }

    // 구글 로그인 실행
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    // Google 로그인 결과 처리
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            viewModel.handleGoogleSignInResult(task)
        }
    }

    // 메인 화면으로 이동
    private fun moveToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}
