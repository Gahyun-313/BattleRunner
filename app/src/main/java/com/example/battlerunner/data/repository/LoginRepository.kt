package com.example.battlerunner.data.repository

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.battlerunner.data.local.DBHelper
import com.example.battlerunner.data.model.LoginInfo
import com.example.battlerunner.network.RetrofitInstance
import com.google.android.gms.tasks.Task
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.kakao.sdk.user.UserApiClient
import com.kakao.sdk.auth.model.OAuthToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginRepository(private val context: Context) {

    private val dbHelper: DBHelper = DBHelper.getInstance(context)

    // 서버에 회원가입 요청
    fun performServerSignUp(
        loginInfo: LoginInfo,
        callback: (Boolean, String?) -> Unit
    ) {
        RetrofitInstance.loginApi.addLoginInfo(loginInfo).enqueue(object : Callback<LoginInfo> {
            override fun onResponse(call: Call<LoginInfo>, response: Response<LoginInfo>) {
                if (response.isSuccessful) {
                    dbHelper.saveAutoLoginInfo(loginInfo) // 서버 응답 성공 시 SQLite에 저장
                    callback(true, null)
                } else {
                    Log.e("SignUp", "회원가입 실패: ${response.code()}")
                    callback(false, "회원가입 실패: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<LoginInfo>, t: Throwable) {
                Log.e("SignUp", "네트워크 오류: ${t.message}")
                callback(false, "네트워크 오류: ${t.message}")
            }
        })
    }

    // 서버에 로그인 요청
    fun performServerLogin(
        userId: String,
        password: String,
        callback: (Boolean, String?) -> Unit
    ) {
        val loginInfo = LoginInfo(userId, password)
        RetrofitInstance.loginApi.login(loginInfo).enqueue(object : Callback<LoginInfo> {
            override fun onResponse(call: Call<LoginInfo>, response: Response<LoginInfo>) {
                if (response.isSuccessful) {
                    val serverResponse = response.body()
                    if (serverResponse != null) {
                        dbHelper.saveAutoLoginInfo(serverResponse) // 서버 응답 데이터 SQLite에 저장
                        callback(true, null)
                    } else {
                        callback(false, "서버 응답이 없습니다.")
                    }
                } else {
                    Log.e("Login", "로그인 실패: ${response.code()}")
                    callback(false, "로그인 실패: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<LoginInfo>, t: Throwable) {
                Log.e("Login", "네트워크 오류: ${t.message}")
                callback(false, "네트워크 오류: ${t.message}")
            }
        })
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

                val loginInfo = LoginInfo(email, token.accessToken ?: "", name, "kakao")

                dbHelper.saveAutoLoginInfo(loginInfo)

                // 서버에 로그인 정보와 사용자 정보 전송
                CoroutineScope(Dispatchers.IO).launch {
                    sendLoginInfoToServer(loginInfo)
                }
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
                val email = account.email ?: "googleUserId"
                val name = account.displayName ?: "구글 사용자"

                val loginInfo = LoginInfo(email, account.idToken ?: "", name, "google")
                dbHelper.saveAutoLoginInfo(loginInfo)

                // 서버에 로그인 정보와 사용자 정보 전송
                CoroutineScope(Dispatchers.IO).launch {
                    sendLoginInfoToServer(loginInfo)
                }

                callback(true, null)
            }
        } catch (e: ApiException) {
            Log.e("GoogleLoginError", "Google 로그인 실패: ${e.statusCode} - ${e.message}")
            callback(false, "Google 로그인 실패: ${e.message}")
        }
    }

    // 자동 로그인 - 자체, 카카오, 구글 모두 이 방법 사용
    fun performAutoLogin(callback: (Boolean, String?) -> Unit) {
        val loginInfo = dbHelper.getLoginInfo() // SQLite에서 정보 가져오기
        if (loginInfo != null) {
            performServerLogin(loginInfo.first, loginInfo.second, callback) // loginInfo.first = userId, loginInfo.second = password
        } else {
            callback(false, "저장된 로그인 정보가 없습니다.")
        }
    }

    // 회원 가입 메서드 : 서버에 회원 정보 저장
    private fun sendLoginInfoToServer(loginInfo: LoginInfo) {

        // dbHelper에 로그인 정보 저장
        val isInserted = dbHelper.saveAutoLoginInfo(loginInfo)

        if (isInserted) {
            RetrofitInstance.loginApi.addLoginInfo(loginInfo).enqueue(object : Callback<LoginInfo> {
                override fun onResponse(call: Call<LoginInfo>, response: Response<LoginInfo>) {
                    if (response.isSuccessful) {
                        Log.d("LoginRepository", "서버에 회원 정보 전송 성공")
                        //callback(true, null)
                    } else {
                        Log.e("LoginRepository", "서버 전송 실패: ${response.errorBody()?.string()}")
                        //callback(false, "서버 전송 실패")
                    }
                }

                override fun onFailure(call: Call<LoginInfo>, t: Throwable) {
                    Log.e("LoginRepository", "서버 전송 중 오류 발생: ${t.message}")
                    //callback(false, "서버 전송 중 오류 발생: ${t.message}")
                }
            })
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
}