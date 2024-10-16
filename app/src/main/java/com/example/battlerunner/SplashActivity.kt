package com.example.battlerunner

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = ContextCompat.getColor(this@SplashActivity, R.color.blue0) // 상태바 색상 변경

        setContentView(R.layout.activity_splash)

        // 3초 후에 LoginActivity로 이동
        lifecycleScope.launch {
            delay(2000L) // 2초 지연
            val intent = Intent(this@SplashActivity, LoginActivity::class.java)
            startActivity(intent)
            finish() // 스플래시 액티비티 종료
        }
    }
}
