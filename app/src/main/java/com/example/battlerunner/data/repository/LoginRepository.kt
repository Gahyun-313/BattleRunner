package com.example.battlerunner.data.repository

import android.content.Context
import android.content.Intent
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

    // 자체 로그인 처리 메서드
    fun performCustomLogin(userId: String, userPassword: String, callback: (Boolean, String?) -> Unit) {
        val userExists = dbHelper.checkUser(userId)  // 사용자가 DB에 있는지 확인

        if (!userExists) {
            callback(false, "저장된 로그인 정보가 없습니다. 회원가입을 진행해 주세요.")
        } else if (dbHelper.checkUserPass(userId, userPassword)) {
            dbHelper.saveLoginInfo(userId, userPassword, "custom")  // 로그인 정보 저장
            callback(true, null)  // 로그인 성공
        } else {
            callback(false, "아이디 또는 비밀번호가 잘못되었습니다.")  // 로그인 실패
        }
    }

    // 카카오 로그인 처리 메서드
    fun performKakaoLogin(callback: (Boolean, String?) -> Unit) {
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            // 카카오톡 설치 시, 앱 연결 시도
            UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                if (error != null) { // 에러 발생 시
                    loginWithKakaoAccount(callback) // 로그인 시도
                } else if (token != null) {
                    saveKakaoUserInfo(token, callback)  // 로그인 정보 저장
                }
            }
        } else {
            // 카카오톡 미설치 시, 로그인 시도
            loginWithKakaoAccount(callback)
        }
    }

    // 카카오톡 계정 로그인 시도
    private fun loginWithKakaoAccount(callback: (Boolean, String?) -> Unit) {
        UserApiClient.instance.loginWithKakaoAccount(context) { token, error ->
            if (error != null) { // 에러 발생 시
                callback(false, "카카오 로그인 실패: ${error.message}")
            } else if (token != null) { // 성공
                saveKakaoUserInfo(token, callback)  // 로그인 정보 저장
            }
        }
    }

    // 카카오 사용자 정보 저장
    private fun saveKakaoUserInfo(token: OAuthToken, callback: (Boolean, String?) -> Unit) {
        UserApiClient.instance.me { user, userError ->
            if (userError == null && user != null) {
                val email = user.kakaoAccount?.email ?: "kakaoUserId"
                val nickname = user.kakaoAccount?.profile?.nickname ?: "카카오 사용자"

                // login_info 및 users 테이블에 정보 저장
                dbHelper.saveLoginInfo(email, token.accessToken, "kakao")
                dbHelper.insertUserData(email, "", nickname)

                callback(true, null)
            } else {
                callback(false, userError?.message ?: "사용자 정보 가져오기 실패")
            }
        }
    }

    // 구글 로그인 결과 처리 메서드
    fun performGoogleLogin(task: Task<GoogleSignInAccount>, callback: (Boolean, String?) -> Unit) {
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                saveGoogleUserInfo(account, callback)
            }
        } catch (e: ApiException) {
            callback(false, "Google 로그인 실패: ${e.message}")
        }
    }

    // 구글 사용자 정보 저장
    private fun saveGoogleUserInfo(account: GoogleSignInAccount, callback: (Boolean, String?) -> Unit) {
        val email = account.email ?: "googleUserId"
        val displayName = account.displayName ?: "구글 사용자"
        val token = account.idToken ?: ""

        // login_info 및 users 테이블에 정보 저장
        dbHelper.saveLoginInfo(email, token, "google")
        dbHelper.insertUserData(email, "", displayName)

        callback(true, null)
    }

    // 자동 로그인 확인 메서드
    fun performAutoLogin(callback: (Boolean) -> Unit) {
        val loginInfo = dbHelper.getLoginInfo()  // 저장된 로그인 정보 가져오기
        if (loginInfo != null) {
            val (userId, token) = loginInfo
            when (dbHelper.getLoginType()) {
                "custom" -> callback(dbHelper.checkUserPass(userId, token))  // 자체 로그인 체크
                "kakao" -> performKakaoAutoLogin(callback)  // 카카오 로그인 체크
                "google" -> performGoogleAutoLogin(callback)  // 구글 로그인 체크
                else -> callback(false)  // 로그인 정보가 없을 경우 실패 처리
            }
        } else {
            callback(false)
        }
    }

    // 카카오 자동 로그인 확인 메서드
    private fun performKakaoAutoLogin(callback: (Boolean) -> Unit) {
        UserApiClient.instance.accessTokenInfo { tokenInfo, error ->
            callback(tokenInfo != null && error == null)  // 유효한 토큰이 있는 경우 성공 처리
        }
    }

    // 구글 자동 로그인 확인 메서드
    private fun performGoogleAutoLogin(callback: (Boolean) -> Unit) {
        val googleAccount = GoogleSignIn.getLastSignedInAccount(context)
        callback(googleAccount != null)  // 계정이 유효하면 성공 처리
    }
}

