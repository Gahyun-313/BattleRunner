package com.example.battlerunner.data.repository

import android.content.Context
import android.util.Log
import android.widget.Toast
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

    private val dbHelper: DBHelper = DBHelper.getInstance(context) // SQLite DBHelper 인스턴스 생성

    // 서버에 회원가입 요청
    fun performServerSignUp(
        userId: String,
        password: String,
        name: String,
        loginType: String, // 로그인 타입 추가
        callback: (Boolean, String?) -> Unit
    ) {
        // LoginInfo 객체 생성 (loginType 포함)
        val loginInfo = LoginInfo(
            userId = userId,
            password = password,
            name = name,
            loginType = loginType
        )

        // 회원가입 요청
        RetrofitInstance.loginApi.addLoginInfo(loginInfo).enqueue(object : Callback<LoginInfo> {
            override fun onResponse(call: Call<LoginInfo>, response: Response<LoginInfo>) {
                if (response.isSuccessful && response.body() != null) {
                    val loginInfoResponse = response.body()!!
                    Log.d("LoginRepository", "Received LoginInfo from server: $loginInfoResponse")

                    val isSaved = dbHelper.saveAutoLoginInfo(loginInfoResponse)
                    if (isSaved) {
                        callback(true, null)
                    } else {
                        callback(false, "SQLite 저장 실패")
                    }
                } else {
                    Log.e("SignUp", "회원가입 실패: ${response.code()}")
                    callback(false, "회원가입 실패: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<LoginInfo>, t: Throwable) {
                Log.e("SignUp", "네트워크 오류: ${t.message}")
                callback(false, "네트워크 오류: ${t.message}")
            }
        })
    }


    // 서버에 로그인 요청 -> 로그인 타입에 따라 처리
    fun performServerLogin(userId: String, password: String?, loginType: String, callback: (Boolean, String?) -> Unit) {
        val loginInfo = LoginInfo(
            userId = userId,
            password = password ?: "", // 커스텀 로그인 외에는 null 처리
            loginType = loginType
        )

        RetrofitInstance.loginApi.login(loginInfo).enqueue(object : Callback<LoginInfo> {
            override fun onResponse(call: Call<LoginInfo>, response: Response<LoginInfo>) {
                if (response.isSuccessful) {
                    val serverResponse = response.body()
                    if (serverResponse != null) {
                        dbHelper.saveAutoLoginInfo(serverResponse) // SQLite에 저장
                        callback(true, "로그인 성공")
                    } else {
                        callback(false, "서버 응답이 없습니다.")
                    }
                } else {
                    callback(false, "로그인 실패: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<LoginInfo>, t: Throwable) {
                callback(false, "네트워크 오류: ${t.message}")
            }
        })
    }


    // 카카오 로그인 (인증)
    fun performKakaoLogin(activity: AppCompatActivity, callback: (Boolean, String?) -> Unit) {
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(activity)) {
            // 카카오톡 앱을 통한 로그인
            UserApiClient.instance.loginWithKakaoTalk(activity) { token, error ->
                if (error != null) {
                    loginWithKakaoAccount(activity, callback) // 실패 시 계정 로그인 시도
                } else if (token != null) {
                    // 카카오 인증 시 처리 => 로그인
                    handleKakaoLoginSuccess(token, callback)
                }
            }
        } else {
            loginWithKakaoAccount(activity, callback) // 앱 미설치 시 계정 로그인
        }
    }

    // 카카오 자체 로그인 (카카오 계정 로그인)
    private fun loginWithKakaoAccount(activity: AppCompatActivity, callback: (Boolean, String?) -> Unit) {
        UserApiClient.instance.loginWithKakaoAccount(activity) { token, error ->
            if (error != null) {
                callback(false, "카카오 로그인 실패: ${error.message}")
            } else if (token != null) {
                handleKakaoLoginSuccess(token, callback)
            }
        }
    }

    // 카카오 로그인 처리
    private fun handleKakaoLoginSuccess(token: OAuthToken, callback: (Boolean, String?) -> Unit) {
        UserApiClient.instance.me { user, userError ->
            if (userError != null) {
                callback(false, "사용자 정보 가져오기 실패: ${userError.message}")
            } else if (user != null) {
                val email = user.kakaoAccount?.email ?: "kakaoUserId"
                val name = user.kakaoAccount?.profile?.nickname ?: "카카오 사용자"
                val loginInfo = LoginInfo(email, token.accessToken ?: "", name, "kakao") // loginType 포함

                CoroutineScope(Dispatchers.IO).launch {
                    sendLoginInfoToServer(loginInfo, callback) // 서버로 전송
                }
            }
        }
    }

    // 구글 로그인
    fun performGoogleLogin(task: Task<GoogleSignInAccount>, callback: (Boolean, String?) -> Unit) {
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                val email = account.email ?: "googleUserId"
                val name = account.displayName ?: "구글 사용자"
                val loginInfo = LoginInfo(email, account.idToken ?: "", name, "google") // loginType 포함

                CoroutineScope(Dispatchers.IO).launch {
                    sendLoginInfoToServer(loginInfo, callback) // 서버로 전송
                }
            }
        } catch (e: ApiException) {
            callback(false, "Google 로그인 실패: ${e.message}")
        }
    }

    // 자동 로그인 처리
    fun performAutoLogin(callback: (Boolean, String?) -> Unit) {
        val loginInfo = dbHelper.getLoginInfo() // SQLite에서 자동 로그인 정보 조회
        if (loginInfo != null) {
            performServerLogin(loginInfo.first, loginInfo.second, "custom") { isLoggedIn, errorMessage ->
                callback(isLoggedIn, errorMessage)
            }
        } else {
            callback(false, "저장된 로그인 정보가 없습니다.")
        }
    }

    // 로그인 정보를 서버로 전송
    private fun sendLoginInfoToServer(loginInfo: LoginInfo, callback: (Boolean, String?) -> Unit) {
        RetrofitInstance.loginApi.addLoginInfo(loginInfo).enqueue(object : Callback<LoginInfo> {
            override fun onResponse(call: Call<LoginInfo>, response: Response<LoginInfo>) {
                if (response.isSuccessful) {
                    callback(true, null) // 성공 콜백
                } else {
                    callback(false, "서버 전송 실패: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<LoginInfo>, t: Throwable) {
                callback(false, "서버 전송 중 오류 발생: ${t.message}")
            }
        })
    }
}
