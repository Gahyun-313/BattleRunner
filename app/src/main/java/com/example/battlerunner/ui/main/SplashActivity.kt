package com.example.battlerunner.ui.main

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.battlerunner.R
import com.example.battlerunner.ui.login.LoginActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var viewModel: SplashViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        window.statusBarColor = ContextCompat.getColor(this, R.color.blue0)

        // ViewModel 초기화
        viewModel = ViewModelProvider(this, SplashViewModelFactory(application)).get(SplashViewModel::class.java)

        // 2초 지연 후 자동 로그인 확인
        lifecycleScope.launch {
            delay(2000L)  // 2초 지연
            viewModel.checkAutoLogin()  // 자동 로그인 여부 확인
        }

        // 자동 로그인 상태를 관찰하여 화면 전환
        viewModel.autoLoginStatus.observe(this) { isLoggedIn ->
            if (isLoggedIn) {
                moveToMainActivity()  // 자동 로그인 성공 시 메인 화면으로 이동
            } else {
                moveToLoginActivity()  // 자동 로그인 실패 시 로그인 화면으로 이동
            }
        }
    }

    private fun moveToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)  // 스택 지우기
        startActivity(intent)
        finish()
    }

    private fun moveToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)  // 스택 지우기
        startActivity(intent)
        finish()
    }
}

