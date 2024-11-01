package com.example.battlerunner.data.repository

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.battlerunner.data.local.DBHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.tasks.Task
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.kakao.sdk.user.UserApiClient
import com.kakao.sdk.auth.model.OAuthToken

class LoginRepository(private val context: Context) {

    private val dbHelper: DBHelper = DBHelper.getInstance(context)

    // 자체 로그인
    fun performCustomLogin(userId: String, userPassword: String, callback: (Boolean, String?) -> Unit) {
        if (dbHelper.checkUserPass(userId, userPassword)) {
            dbHelper.saveLoginInfo(userId, userPassword, "custom")
            callback(true, null)
        } else {
            callback(false, "ID 또는 비밀번호가 잘못되었습니다.")
        }
    }


    // 카카오 로그인
    fun performKakaoLogin(activity: AppCompatActivity, callback: (Boolean, String?) -> Unit) {
        Log.d("KakaoLogin", "Attempting Kakao login")
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(activity)) {
            UserApiClient.instance.loginWithKakaoTalk(activity) { token, error ->
                if (error != null) {
                    Log.e("KakaoLoginError", "Kakao Talk login error: ${error.message}")
                    loginWithKakaoAccount(activity, callback)
                } else if (token != null) {
                    Log.d("KakaoLogin", "Kakao token obtained: ${token.accessToken}")
                    handleKakaoLoginSuccess(token, callback)
                }
            }
        } else {
            loginWithKakaoAccount(activity, callback)
        }
    }

    private fun loginWithKakaoAccount(activity: AppCompatActivity, callback: (Boolean, String?) -> Unit) {
        Log.d("KakaoLogin", "Attempting Kakao Account login")
        UserApiClient.instance.loginWithKakaoAccount(activity) { token, error ->
            if (error != null) {
                Log.e("KakaoLoginError", "Kakao Account login error: ${error.message}")
                callback(false, "카카오 로그인 실패: ${error.message}")
            } else if (token != null) {
                Log.d("KakaoLogin", "Kakao token obtained: ${token.accessToken}")
                handleKakaoLoginSuccess(token, callback)
            }
        }
    }

    private fun handleKakaoLoginSuccess(token: OAuthToken, callback: (Boolean, String?) -> Unit) {
        UserApiClient.instance.me { user, userError ->
            if (userError != null) callback(false, "사용자 정보 가져오기 실패: ${userError.message}")
            else if (user != null) {
                val email = user.kakaoAccount?.email ?: "kakaoUserId"
                dbHelper.saveLoginInfo(email, token.accessToken, "kakao")
                dbHelper.insertUserData(
                    id = email,
                    password = token.accessToken,
                    name = user.kakaoAccount?.profile?.nickname ?: "카카오 사용자"
                )
                callback(true, null)
            }
        }
    }

    // Google 로그인
    fun performGoogleLogin(task: Task<GoogleSignInAccount>, callback: (Boolean, String?) -> Unit) {
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                Log.d("GoogleLogin", "Google account obtained: ${account.email}")
                dbHelper.saveLoginInfo(account.email ?: "googleUserId", account.idToken ?: "", "google")
                dbHelper.insertUserData(
                    id = account.email ?: "googleUserId",
                    password = account.idToken,
                    name = account.displayName ?: "구글 사용자"
                )
                callback(true, null)
            }
        } catch (e: ApiException) {
            Log.e("GoogleLoginError", "Google 로그인 실패: ${e.statusCode} - ${e.message}")
            callback(false, "Google 로그인 실패: ${e.message}")
        }
    }


    fun performAutoLogin(callback: (Boolean) -> Unit) {
        val loginInfo = dbHelper.getLoginInfo()  // 로그인 정보 가져오기
        if (loginInfo != null) {
            val (userId, password) = loginInfo
            when (dbHelper.getLoginType()) {
                "custom" -> callback(dbHelper.checkUserPass(userId, password))  // 자체 로그인 체크 후 바로 콜백 호출
                "kakao" -> performKakaoAutoLogin(callback)  // 카카오 자동 로그인
                "google" -> performGoogleAutoLogin(callback)  // 구글 자동 로그인
                else -> callback(false)
            }
        } else {
            callback(false)
        }
    }

    // 카카오 자동 로그인
    private fun performKakaoAutoLogin(callback: (Boolean) -> Unit) {
        try {
            UserApiClient.instance.accessTokenInfo { tokenInfo, error ->
                if (error != null) {
                    callback(false)  // 에러가 발생하면 자동 로그인 실패 처리
                } else {
                    callback(tokenInfo != null)  // 유효한 토큰이 있으면 true 반환
                }
            }
        } catch (e: Exception) {
            callback(false)  // 예외 발생 시 자동 로그인 실패 처리
        }
    }

    // 구글 자동 로그인
    private fun performGoogleAutoLogin(callback: (Boolean) -> Unit) {
        try {
            val googleAccount = GoogleSignIn.getLastSignedInAccount(context)
            callback(googleAccount != null)  // 유효한 구글 계정이 있으면 true 반환
        } catch (e: Exception) {
            callback(false)  // 예외 발생 시 자동 로그인 실패 처리
        }
    }


}
