package com.example.battlerunner.data.repository

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.battlerunner.data.local.DBHelper
import com.example.battlerunner.data.model.LoginInfo
import com.example.battlerunner.data.model.User
import com.example.battlerunner.network.RetrofitInstance
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.tasks.Task
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.kakao.sdk.user.UserApiClient
import com.kakao.sdk.auth.model.OAuthToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.awaitResponse

class LoginRepository(private val context: Context) {

    private val dbHelper: DBHelper = DBHelper.getInstance(context)

    // 자체 로그인
    fun performCustomLogin(userId: String, userPassword: String, callback: (Boolean, String?) -> Unit) {
        if (dbHelper.checkUserPass(userId, userPassword)) {
            dbHelper.saveLoginInfo(userId, userPassword, name = "customUser", "custom")

            // 서버에 로그인 정보와 사용자 정보 전송
            CoroutineScope(Dispatchers.IO).launch {
                val loginInfo = LoginInfo(userId, userPassword, name = "customUser", "kakao")
                sendLoginInfoToServer(loginInfo)
            }
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

    private fun loginWithKakaoAccount(
        activity: AppCompatActivity,
        callback: (Boolean, String?) -> Unit
    ) {
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
                val name = user.kakaoAccount?.profile?.nickname ?: "카카오 사용자"

                dbHelper.saveLoginInfo(email, token.accessToken ?: "", name, "kakao")

                // 서버에 로그인 정보와 사용자 정보 전송
                CoroutineScope(Dispatchers.IO).launch {
                    val loginInfo = LoginInfo(email, token.accessToken ?: "", name, "kakao")
                    sendLoginInfoToServer(loginInfo)
                }
                callback(true, null)
            }
        }
    }

    // Google 로그인
    suspend fun performGoogleLogin(task: Task<GoogleSignInAccount>, callback: (Boolean, String?) -> Unit) {
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                Log.d("GoogleLogin", "Google account obtained: ${account.email}")
                val email = account.email ?: "googleUserId"
                val name = account.displayName ?: "구글 사용자"

                dbHelper.saveLoginInfo(email, account.idToken ?: "", name, "google")

                // 서버에 로그인 정보와 사용자 정보 전송
                CoroutineScope(Dispatchers.IO).launch {
                    val loginInfo = LoginInfo(email, account.idToken ?: "",name, "google")
                    sendLoginInfoToServer(loginInfo)
                }

                callback(true, null)
            }
        } catch (e: ApiException) {
            Log.e("GoogleLoginError", "Google 로그인 실패: ${e.statusCode} - ${e.message}")
            callback(false, "Google 로그인 실패: ${e.message}")
        }
    }

    // 자동 로그인
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
        UserApiClient.instance.accessTokenInfo { tokenInfo, error ->
            callback(error == null && tokenInfo != null)
        }
    }

    // 구글 자동 로그인
    private fun performGoogleAutoLogin(callback: (Boolean) -> Unit) {
        val googleAccount = GoogleSignIn.getLastSignedInAccount(context)
        callback(googleAccount != null)
    }

    // 회원 가입 메서드 : 서버에 회원 정보 저장
    fun registerUser(userId: String, password: String, username: String, loginType: String, callback: (Boolean, String?) -> Unit) {
        // dbHelper에 로그인 정보 저장
        val isInserted = dbHelper.saveLoginInfo(userId, password, username, loginType)

        if (isInserted) {
            val loginInfo = LoginInfo(userId, password, username, loginType)
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

    private fun sendLoginInfoToServer(loginInfo: LoginInfo): Call<LoginInfo> {
        return RetrofitInstance.loginApi.addLoginInfo(loginInfo)
    }
}