// LogoutActivity.kt
package com.example.battlerunner.ui.logout

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModelProvider
import com.example.battlerunner.R
import com.example.battlerunner.data.local.DBHelper
import com.example.battlerunner.ui.login.LoginActivity
import com.example.battlerunner.ui.mypage.LogoutViewModel
import com.example.battlerunner.ui.mypage.LogoutViewModelFactory
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class LogoutActivity : AppCompatActivity() {

    private lateinit var viewModel: LogoutViewModel
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)  // Google 로그인 옵션 설정
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso) // GoogleSignIn Client 생성

        val dbHelper = DBHelper.getInstance(applicationContext) // DBHelper 인스턴스 생성
        val factory = LogoutViewModelFactory(application, googleSignInClient, dbHelper) // LogoutViewModelFactory 초기화

        viewModel = ViewModelProvider(this, factory).get(LogoutViewModel::class.java)

        // 로그인 타입 가져옴
        val loginType = dbHelper.getLoginType() ?: ""  // 로그인 유형 가져옴 (없으면 "")

        // 로그아웃 실행
        viewModel.logout(this, loginType)
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}