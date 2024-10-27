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

    // 자체 로그인 처리 메서드
    fun performCustomLogin(callback: (Boolean, String?) -> Unit) {
        val loginInfo = dbHelper.getLoginInfo()  // 로그인 정보 확인
        if (loginInfo != null) {
            val (userId, userPassword) = loginInfo
            if (dbHelper.checkUserPass(userId, userPassword)) {
                dbHelper.saveLoginInfo(userId, "", "custom")  // 로그인 정보 저장 (토큰은 빈 값으로 설정)
                callback(true, null)  // 로그인 성공
            } else {
                callback(false, "ID 또는 비밀번호가 잘못되었습니다.")  // 로그인 실패
            }
        } else {
            callback(false, "저장된 로그인 정보가 없습니다. 회원가입을 진행해 주세요.")  // 로그인 정보가 없을 경우
        }
    }

    fun performKakaoLogin(activity: AppCompatActivity, callback: (Boolean, String?) -> Unit) {
        // 카카오톡 앱이 설치된 경우 카카오톡을 통해 로그인 시도
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(activity)) {
            UserApiClient.instance.loginWithKakaoTalk(activity) { token, error ->
                if (error != null) {
                    // 카카오톡 로그인 실패 시, 계정 로그인으로 대체
                    loginWithKakaoAccount(activity, callback)
                } else if (token != null) {
                    handleKakaoLoginSuccess(token, callback)
                }
            }
        } else {
            // 카카오톡 미설치 시 계정 로그인 시도
            loginWithKakaoAccount(activity, callback)
        }
    }

    // 카카오 계정 로그인을 통해 로그인 처리하는 메서드
    private fun loginWithKakaoAccount(activity: AppCompatActivity, callback: (Boolean, String?) -> Unit) {
        UserApiClient.instance.loginWithKakaoAccount(activity) { token, error ->
            if (error != null) {
                callback(false, "카카오 로그인 실패: ${error.message}")
            } else if (token != null) {
                handleKakaoLoginSuccess(token, callback)
            }
        }
    }

    private fun handleKakaoLoginSuccess(token: OAuthToken, callback: (Boolean, String?) -> Unit) {
        UserApiClient.instance.me { user, userError ->
            if (userError != null) {
                callback(false, "사용자 정보 가져오기 실패: ${userError.message}")
            } else if (user != null) {
                val email = user.kakaoAccount?.email ?: "kakaoUserId"

                // DB에 user_id로 이메일, token으로 토큰을 저장
                dbHelper.saveLoginInfo(email, token.accessToken, "kakao")

                // users 테이블에 사용자 정보 저장
                dbHelper.insertUserData(
                    id = email,
                    password = "",  // 비밀번호는 사용하지 않음
                    nick = user.kakaoAccount?.profile?.nickname ?: "카카오 사용자"
                )
                callback(true, null)
            }
        }
    }


    // 구글 로그인 처리 메서드
    fun performGoogleLogin(task: Task<GoogleSignInAccount>, callback: (Boolean, String?) -> Unit) {
        try {
            val account = task.getResult(ApiException::class.java)  // Google 로그인 계정 가져오기
            if (account != null) {
                // login_info 테이블에 Google 로그인 정보 저장
                dbHelper.saveLoginInfo(account.email ?: "googleUserId", account.idToken ?: "", "google")

                // users 테이블에 Google 사용자 정보 저장
                dbHelper.insertUserData(
                    id = account.email ?: "googleUserId",  // id 컬럼에 email 사용
                    password = "",  // Google 로그인에서는 비밀번호를 사용하지 않음
                    nick = account.displayName ?: "구글 사용자"  // 닉네임을 displayName으로 설정
                )

                callback(true, null)  // 로그인 성공 시 콜백 호출
            }
        } catch (e: ApiException) {
            callback(false, "Google 로그인 실패: ${e.message}")  // 로그인 실패 시 콜백에 오류 메시지 전달
        }
    }

    //Todo: 자체 로그인 자동 로그인 안 됨
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

