package com.example.battlerunner.data.repository

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.battlerunner.data.local.DBHelper
import com.example.battlerunner.data.model.LoginInfo
import com.example.battlerunner.network.RetrofitInstance
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.tasks.Task
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.kakao.sdk.user.UserApiClient
import com.kakao.sdk.auth.model.OAuthToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginRepository(private val context: Context) {

    private val dbHelper: DBHelper = DBHelper.getInstance(context)

    // 회원 등록 기능
    fun registerUser(userId: String, password: String, username: String, callback: (Boolean, String?) -> Unit) {
        val isInserted = dbHelper.insertUserData(userId, password, username)

        if (isInserted) {
            val loginInfo = LoginInfo(userId, password, username)
            RetrofitInstance.loginApi.addLoginInfo(loginInfo).enqueue(object : Callback<LoginInfo> {
                override fun onResponse(call: Call<LoginInfo>, response: Response<LoginInfo>) {
                    if (response.isSuccessful) {
                        Log.d("LoginRepository", "서버에 회원 정보 전송 성공")
                        callback(true, null)
                    } else {
                        Log.e("LoginRepository", "서버 전송 실패: ${response.errorBody()?.string()}")
                        callback(false, "서버 전송 실패")
                    }
                }

                override fun onFailure(call: Call<LoginInfo>, t: Throwable) {
                    Log.e("LoginRepository", "서버 전송 중 오류 발생: ${t.message}")
                    callback(false, "서버 전송 중 오류 발생: ${t.message}")
                }
            })
        } else {
            callback(false, "로컬 DB에 회원 정보 저장 실패")
        }
    }

    // 자체 로그인
    fun performCustomLogin(userId: String, userPassword: String, callback: (Boolean, String?) -> Unit) {
        Log.d("performCustomLogin", "Custom login 시작")
        if (dbHelper.checkUserPass(userId, userPassword)) {
            dbHelper.saveLoginInfo(userId, userPassword, "custom")
            val loginInfo = LoginInfo(userId, userPassword, "custom")

            sendLoginInfoToServer(loginInfo).enqueue(object : Callback<LoginInfo> {
                override fun onResponse(call: Call<LoginInfo>, response: Response<LoginInfo>) {
                    if (response.isSuccessful) callback(true, null)
                    else callback(false, "서버 전송 실패")
                }

                override fun onFailure(call: Call<LoginInfo>, t: Throwable) {
                    callback(false, "로그인 처리 중 오류 발생: ${t.message}")
                }
            })
        } else {
            callback(false, "ID 또는 비밀번호가 잘못되었습니다.")
        }
    }

    // 카카오 로그인
    fun performKakaoLogin(activity: AppCompatActivity, callback: (Boolean, String?) -> Unit) {
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(activity)) {
            UserApiClient.instance.loginWithKakaoTalk(activity) { token, error ->
                if (error != null) loginWithKakaoAccount(activity, callback)
                else if (token != null) handleKakaoLoginSuccess(token, callback)
            }
        } else {
            loginWithKakaoAccount(activity, callback)
        }
    }

    private fun loginWithKakaoAccount(activity: AppCompatActivity, callback: (Boolean, String?) -> Unit) {
        UserApiClient.instance.loginWithKakaoAccount(activity) { token, error ->
            if (error != null) callback(false, "카카오 로그인 실패: ${error.message}")
            else if (token != null) handleKakaoLoginSuccess(token, callback)
        }
    }

    private fun handleKakaoLoginSuccess(token: OAuthToken, callback: (Boolean, String?) -> Unit) {
        UserApiClient.instance.me { user, userError ->
            if (userError != null) {
                callback(false, "사용자 정보 가져오기 실패: ${userError.message}")
            } else if (user != null) {
                val email = user.kakaoAccount?.email ?: "kakaoUserId"
                dbHelper.saveLoginInfo(email, token.accessToken ?: "", "kakao")
                sendLoginInfoToServer(LoginInfo(email, token.accessToken ?: "", "kakao")).enqueue(object : Callback<LoginInfo> {
                    override fun onResponse(call: Call<LoginInfo>, response: Response<LoginInfo>) {
                        callback(response.isSuccessful, if (response.isSuccessful) null else "서버 전송 실패")
                    }

                    override fun onFailure(call: Call<LoginInfo>, t: Throwable) {
                        callback(false, "로그인 처리 중 오류 발생: ${t.message}")
                    }
                })
            }
        }
    }
    //TODO 구글 로그인 시 마이페이지 업데이트
    // 구글 로그인
    fun performGoogleLogin(task: Task<GoogleSignInAccount>, callback: (Boolean, String?) -> Unit) {
        try {
            val account = task.getResult(ApiException::class.java)
            account?.let {
                val email = it.email ?: "googleUserId"
                dbHelper.saveLoginInfo(email, it.idToken ?: "", "google")
                sendLoginInfoToServer(LoginInfo(email, it.idToken ?: "", "google")).enqueue(object : Callback<LoginInfo> {
                    override fun onResponse(call: Call<LoginInfo>, response: Response<LoginInfo>) {
                        callback(response.isSuccessful, if (response.isSuccessful) null else "서버 전송 실패")
                    }

                    override fun onFailure(call: Call<LoginInfo>, t: Throwable) {
                        callback(false, "로그인 처리 중 오류 발생: ${t.message}")
                    }
                })
            }
        } catch (e: ApiException) {
            callback(false, "Google 로그인 실패: ${e.message}")
        }
    }

    // 자동 로그인 기능에서 서버의 정보를 가져와 일치 여부 확인
    fun performAutoLogin(callback: (Boolean) -> Unit) {
        val loginInfo = dbHelper.getLoginInfo()
        if (loginInfo != null) {
            when (dbHelper.getLoginType()) {
                "custom" -> verifyLoginInfoOnServer(loginInfo.first, callback)
                "kakao" -> performKakaoAutoLogin(callback)
                "google" -> performGoogleAutoLogin(callback)
                else -> callback(false)
            }
        } else {
            callback(false)
        }
    }

    // 서버에서 로그인 정보를 확인
    private fun verifyLoginInfoOnServer(userId: String, callback: (Boolean) -> Unit) {
        RetrofitInstance.loginApi.getLoginInfoById(userId).enqueue(object : Callback<LoginInfo> {
            override fun onResponse(call: Call<LoginInfo>, response: Response<LoginInfo>) {
                callback(response.isSuccessful && response.body()?.userId == userId)
            }

            override fun onFailure(call: Call<LoginInfo>, t: Throwable) {
                callback(false)
            }
        })
    }

    private fun performKakaoAutoLogin(callback: (Boolean) -> Unit) {
        UserApiClient.instance.accessTokenInfo { tokenInfo, error ->
            callback(error == null && tokenInfo != null)
        }
    }

    private fun performGoogleAutoLogin(callback: (Boolean) -> Unit) {
        val googleAccount = GoogleSignIn.getLastSignedInAccount(context)
        callback(googleAccount != null)
    }

    private fun sendLoginInfoToServer(loginInfo: LoginInfo): Call<LoginInfo> {
        return RetrofitInstance.loginApi.addLoginInfo(loginInfo)
    }
}
