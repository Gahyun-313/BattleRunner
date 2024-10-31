package com.example.battlerunner.ui.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.example.battlerunner.ui.main.MainActivity
import com.example.battlerunner.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class LoginActivity : AppCompatActivity() {

    private lateinit var viewModel: LoginViewModel
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        // Google 로그인 옵션 설정
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso) // GoogleSignIn Client 생성

        // 자체 로그인 버튼 클릭 리스너
        findViewById<Button>(R.id.login_btn).setOnClickListener {
            startActivity(Intent(this, Login2Activity::class.java))
            finish()
        }

        // 카카오 로그인 버튼 클릭 리스너
        findViewById<ImageButton>(R.id.kakao_login_btn).setOnClickListener {
            viewModel.performKakaoLogin()
        }

        // 구글 로그인 버튼 클릭 리스너
        findViewById<ImageButton>(R.id.google_login_btn).setOnClickListener {
            signInWithGoogle()
        }

        // ViewModel의 로그인 상태를 관찰하여 성공 시 메인 화면으로 이동
        viewModel.loginStatus.observe(this) { status ->
            if (status) moveToMainActivity()
        }

        // ViewModel의 오류 메시지를 관찰하여 Toast로 표시
        viewModel.errorMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    // Google 로그인 실행
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleLoginLauncher.launch(signInIntent)
    }

    // Google 로그인 결과 처리
    private val googleLoginLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            viewModel.performGoogleLogin(task)
        }

    // 메인 화면으로 이동
    private fun moveToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)  // 스택 지우기
        startActivity(intent)
        finish()
    }
}
