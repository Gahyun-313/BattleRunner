package com.example.battlerunner.ui.mypage

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.example.battlerunner.data.local.DBHelper
import com.example.battlerunner.ui.login.LoginActivity
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LogoutViewModel(
    application: Application,
    private val googleSignInClient: GoogleSignInClient,
    private val dbHelper: DBHelper
) : AndroidViewModel(application) {

    // CoroutineScope 설정
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    fun logout(context: Context, loginType: String) {
        coroutineScope.launch {
            when (loginType) {
                "kakao" -> logoutFromKakao(context)
                "google" -> logoutFromGoogle(context)
                "custom" -> logoutAndRedirect(context)
            }
        }
    }

    // 카카오 로그아웃 처리 메서드
    private fun logoutFromKakao(context: Context) {
        UserApiClient.instance.logout { error ->
            // 로그아웃 성공 시
            if (error == null) {
                logoutAndRedirect(context)  // 로컬 로그아웃 및 리다이렉트
                Toast.makeText(context, "카카오 로그아웃 성공", Toast.LENGTH_SHORT).show()  // 성공 메시지 출력

            } else {
                Toast.makeText(context, "카카오 로그아웃 실패", Toast.LENGTH_SHORT).show()  // 실패 메시지 출력
                Log.d("Logout", "카카오 로그아웃 실패 : ${error.message}")
            }
        }
    }
    // 구글 로그아웃 처리 메서드
    private fun logoutFromGoogle(context: Context) {
        googleSignInClient.signOut().addOnCompleteListener { task ->
            // 로그아웃 성공 시
            if (task.isSuccessful) {
                logoutAndRedirect(context)  // 로컬 로그아웃 및 리다이렉트
                Toast.makeText(context, "구글 로그아웃 성공", Toast.LENGTH_SHORT).show()  // 성공 메시지 출력

            } else {
                Toast.makeText(context, "구글 로그아웃 실패", Toast.LENGTH_SHORT).show()  // 실패 메시지 출력
                Log.d("Logout", "구글 로그아웃 실패 : ${task.exception?.message}")
            }
        }
    }

    // 로컬 로그아웃 및 리다이렉트 함수
    private fun logoutAndRedirect(context: Context) {
        dbHelper.deleteLoginInfo()  // 로컬 DB에서 로그인 정보 삭제

        // 로그아웃 후 로그인으로 리다이렉트
        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }
}
