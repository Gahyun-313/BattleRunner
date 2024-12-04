package com.example.battlerunner.ui.main

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.battlerunner.R
import com.example.battlerunner.ui.login.LoginActivity
import com.example.battlerunner.utils.LocationUtils
import com.example.battlerunner.utils.MapUtils
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var viewModel: SplashViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // 상태바 색상 변경
        window.statusBarColor = ContextCompat.getColor(this, R.color.blue0)
        // 내비게이션 바 색상 변경
        window.navigationBarColor = ContextCompat.getColor(this, R.color.blue0)

        // ViewModel 초기화
        viewModel = ViewModelProvider(this, SplashViewModelFactory(application)).get(SplashViewModel::class.java)

        // 2초 지연 후 자동 로그인 확인
        lifecycleScope.launch {
            delay(2000L)  // 2초 지연
            viewModel.checkAutoLogin()  // 자동 로그인 여부 확인
        }

        // 자동 로그인 상태 관찰
        observeAutoLoginStatus()
    }

    // 자동 로그인 상태 관찰
    private fun observeAutoLoginStatus() {
        viewModel.autoLoginStatus.observe(this) { isLoggedIn ->
            val targetIntent = if (isLoggedIn) {
                Intent(this, MainActivity::class.java)
            } else {
                Intent(this, LoginActivity::class.java)
            }
            targetIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK) // 스택 초기화
            startActivity(targetIntent)
            finish()
        }
    }

    // 필요시 SQLite DB 초기화
    private fun deleteDatabaseFile() {
        deleteDatabase("Login.db")
    }

}

