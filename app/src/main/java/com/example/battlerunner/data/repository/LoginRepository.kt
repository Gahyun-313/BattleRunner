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

    private val dbHelper: DBHelper = DBHelper.getInstance(context) // SQLite DBHelper 인스턴스 생성

    /**
     * 회원가입 요청을 서버로 전송
     */
    fun performServerSignUp(
        userId: String,
        password: String,
        name: String,
        loginType: String, // 로그인 타입 포함
        callback: (Boolean, String?) -> Unit
    ) {
        val loginInfo = LoginInfo(userId, password, name, loginType)

        // 회원가입 요청
        RetrofitInstance.loginApi.register(loginInfo).enqueue(object : Callback<LoginInfo> {
            override fun onResponse(call: Call<LoginInfo>, registerResponse: Response<LoginInfo>) {
                if (registerResponse.isSuccessful && registerResponse.body() != null) {
                    val registerResponseInfo = registerResponse.body()!!
                    dbHelper.saveAutoLoginInfo(registerResponseInfo)
                    callback(true, null)
                } else {
                    callback(false, "회원가입 실패: ${registerResponse.message()}")
                }
            }

            override fun onFailure(call: Call<LoginInfo>, t: Throwable) {
                callback(false, "네트워크 오류: ${t.message}")
            }
        })
    }

    /**
     * 서버에 로그인 요청
     */
    fun performServerLogin(
        userId: String,
        password: String?,
        loginType: String,
        callback: (Boolean, String?) -> Unit
    ) {
        val loginInfo = LoginInfo(userId, password ?: "", null, loginType)

        RetrofitInstance.loginApi.login(loginInfo).enqueue(object : Callback<LoginInfo> {
            override fun onResponse(call: Call<LoginInfo>, response: Response<LoginInfo>) {
                if (response.isSuccessful && response.body() != null) {
                    val serverResponse = response.body()!!
                    dbHelper.saveAutoLoginInfo(serverResponse) // 자동 로그인 정보 저장
                    callback(true, null) // 성공 콜백 호출
                } else {
                    callback(false, "로그인 실패: ${response.message()}") // 실패 콜백 호출
                }
            }

            override fun onFailure(call: Call<LoginInfo>, t: Throwable) {
                Log.e("Login", "네트워크 오류: ${t.message}")
            }
        })

    }

    /**
     * 카카오 로그인 처리
     */
    fun performKakaoLogin(activity: AppCompatActivity, callback: (Boolean, String?) -> Unit) {
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(activity)) {
            UserApiClient.instance.loginWithKakaoTalk(activity) { token, error ->
                if (error != null) {
                    loginWithKakaoAccount(activity, callback)
                } else if (token != null) {
                    handleKakaoLoginSuccess(token, callback)
                }
            }
        } else {
            loginWithKakaoAccount(activity, callback)
        }
    }

    /**
     * 카카오 계정 로그인 (카카오톡 미설치 시 사용)
     */
    private fun loginWithKakaoAccount(activity: AppCompatActivity, callback: (Boolean, String?) -> Unit) {
        UserApiClient.instance.loginWithKakaoAccount(activity) { token, error ->
            if (error != null) {
                callback(false, "카카오 로그인 실패: ${error.message}")
            } else if (token != null) {
                handleKakaoLoginSuccess(token, callback)
            }
        }
    }

    /**
     * 카카오 로그인 성공 처리
     */
    private fun handleKakaoLoginSuccess(token: OAuthToken, callback: (Boolean, String?) -> Unit) {
        UserApiClient.instance.me { user, userError ->
            if (userError != null) {
                callback(false, "사용자 정보 가져오기 실패: ${userError.message}")
            } else if (user != null) {
                val email = user.kakaoAccount?.email ?: "kakaoUserId"
                val name = user.kakaoAccount?.profile?.nickname ?: "카카오 사용자"
                val loginInfo = LoginInfo(email, "kakaoToken", name, "kakao")

                registerOrLogin(loginInfo, callback)
            }
        }
    }

    /**
     * 구글 로그인 처리
     */
    fun performGoogleLogin(task: Task<GoogleSignInAccount>, callback: (Boolean, String?) -> Unit) {
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                val email = account.email ?: "googleUserId"
                val name = account.displayName ?: "구글 사용자"
                val loginInfo = LoginInfo(email, "googleToken", name, "google")

                registerOrLogin(loginInfo, callback)
            }
        } catch (e: ApiException) {
            callback(false, "Google 로그인 실패: ${e.message}")
        }
    }

    /**
     * 서버로 로그인 정보를 전송 (회원가입/로그인 통합 처리)
     */
    private fun registerOrLogin(loginInfo: LoginInfo, callback: (Boolean, String?) -> Unit) {
        val userId = loginInfo.userId
        val password = loginInfo.password ?: ""  // 비밀번호가 null이면 빈 문자열로 대체
        val name = loginInfo.name ?: "Anonymous" // 이름이 null이면 기본값 설정
        val loginType = loginInfo.loginType ?: "custom" // 로그인 타입이 null이면 기본값 설정

        // Retrofit 요청
        RetrofitInstance.loginApi.login(LoginInfo(userId, password, name, loginType))
            .enqueue(object : Callback<LoginInfo> {
                override fun onResponse(call: Call<LoginInfo>, response: Response<LoginInfo>) {
                    if (response.isSuccessful && response.body() != null) {
                        val serverResponse = response.body()!!
                        dbHelper.saveAutoLoginInfo(serverResponse)
                        callback(true, null)
                    } else if (response.code() == 401) { // 사용자 없음
                        performServerSignUp(userId, password, name, loginType, callback)
                    } else {
                        callback(false, "로그인 실패: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<LoginInfo>, t: Throwable) {
                    callback(false, "네트워크 오류: ${t.message}")
                }
            })
    }


    /**
     * 자동 로그인 처리
     */
    fun performAutoLogin(callback: (Boolean, String?) -> Unit) {
        val loginInfo = dbHelper.getLoginInfo()
        if (loginInfo != null) {
            performServerLogin(loginInfo.first, loginInfo.second, dbHelper.getLoginType() ?: "custom", callback)
        } else {
            callback(false, "저장된 로그인 정보가 없습니다.") // SQLite에 데이터가 없을 경우
        }
    }
}
