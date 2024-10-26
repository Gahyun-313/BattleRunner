package com.example.battlerunner.data.repository

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.example.battlerunner.DBHelper
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
                callback(true, null)  // 로그인 성공
            } else {
                callback(false, "ID 또는 비밀번호가 잘못되었습니다.")  // 로그인 실패
            }
        } else {
            callback(false, "저장된 로그인 정보가 없습니다. 회원가입을 진행해 주세요.")  // 로그인 정보가 없을 경우
        }
    }

    // 카카오 로그인 처리 메서드
    fun performKakaoLogin(activity: AppCompatActivity, callback: (Boolean, String?) -> Unit) {
        val loginCallback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                callback(false, "카카오 로그인 실패: ${error.message}")  // 로그인 실패
            } else if (token != null) {
                dbHelper.saveLoginInfo(token.accessToken, "", "kakao")  // 로그인 정보 저장
                callback(true, null)  // 로그인 성공
            }
        }

        // 로그인 가능 여부 확인 후, 가능한 방법으로 로그인 시도
        UserApiClient.instance.accessTokenInfo { tokenInfo, error ->
            if (error != null) {
                if (UserApiClient.instance.isKakaoTalkLoginAvailable(activity)) {
                    UserApiClient.instance.loginWithKakaoTalk(activity, callback = loginCallback)
                } else {
                    UserApiClient.instance.loginWithKakaoAccount(activity, callback = loginCallback)
                }
            } else {
                callback(true, null)  // 토큰 유효 시 로그인 성공 처리
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

    // 자동 로그인 여부 확인 메서드 (위 코드 포함)
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

