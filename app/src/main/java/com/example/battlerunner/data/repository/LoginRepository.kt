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
    fun performServerSignUp(loginInfo: LoginInfo, callback: (Boolean, String?) -> Unit) {
        RetrofitInstance.loginApi.addLoginInfo(loginInfo).enqueue(object : Callback<LoginInfo> {
            override fun onResponse(call: Call<LoginInfo>, response: Response<LoginInfo>) {
                if (response.isSuccessful) {
                    dbHelper.saveAutoLoginInfo(loginInfo) // 성공 시 SQLite에 저장
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

    //TODO: 서버에 로그인 타입 안 보내짐, 로컬 테이블에는 있음
    // 서버에 로그인 요청
    fun performServerLogin(userId: String, password: String, callback: (Boolean, String?) -> Unit) {
        val loginInfo = LoginInfo(userId, password) // 로그인 정보 생성
        RetrofitInstance.loginApi.login(loginInfo).enqueue(object : Callback<LoginInfo> {
            override fun onResponse(call: Call<LoginInfo>, response: Response<LoginInfo>) {
                if (response.isSuccessful) {
                    val serverResponse = response.body()
                    if (serverResponse != null) {
                        dbHelper.saveAutoLoginInfo(serverResponse) // 성공 시 SQLite에 저장
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
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(activity)) {
            // 카카오톡 앱을 통한 로그인
            UserApiClient.instance.loginWithKakaoTalk(activity) { token, error ->
                if (error != null) {
                    loginWithKakaoAccount(activity, callback) // 실패 시 계정 로그인 시도
                } else if (token != null) {
                    handleKakaoLoginSuccess(token, callback) // 성공 시 처리
                }
            }
        } else {
            loginWithKakaoAccount(activity, callback) // 앱 미설치 시 계정 로그인
        }
    }

    private fun loginWithKakaoAccount(activity: AppCompatActivity, callback: (Boolean, String?) -> Unit) {
        // 카카오 계정 로그인
        UserApiClient.instance.loginWithKakaoAccount(activity) { token, error ->
            if (error != null) {
                callback(false, "카카오 로그인 실패: ${error.message}")
            } else if (token != null) {
                handleKakaoLoginSuccess(token, callback)
            }
        }
    }

    private fun handleKakaoLoginSuccess(token: OAuthToken, callback: (Boolean, String?) -> Unit) {
        // 카카오 로그인 성공 후 사용자 정보 가져오기
        UserApiClient.instance.me { user, userError ->
            if (userError != null) {
                callback(false, "사용자 정보 가져오기 실패: ${userError.message}")
            } else if (user != null) {
                val email = user.kakaoAccount?.email ?: "kakaoUserId"
                val name = user.kakaoAccount?.profile?.nickname ?: "카카오 사용자"
                val loginInfo = LoginInfo(email, token.accessToken ?: "", name, "kakao")

                dbHelper.saveAutoLoginInfo(loginInfo) // SQLite에 저장
                CoroutineScope(Dispatchers.IO).launch {
                    sendLoginInfoToServer(loginInfo, callback) // 서버 전송
                }
            }
        }
    }

    // 구글 로그인
    fun performGoogleLogin(task: Task<GoogleSignInAccount>, callback: (Boolean, String?) -> Unit) {
        try {
            val account = task.getResult(ApiException::class.java) // 로그인 결과 가져오기
            if (account != null) {
                val email = account.email ?: "googleUserId"
                val name = account.displayName ?: "구글 사용자"
                val loginInfo = LoginInfo(email, account.idToken ?: "", name, "google")

                dbHelper.saveAutoLoginInfo(loginInfo) // SQLite에 저장
                CoroutineScope(Dispatchers.IO).launch {
                    sendLoginInfoToServer(loginInfo, callback) // 서버 전송
                }
            }
        } catch (e: ApiException) {
            callback(false, "Google 로그인 실패: ${e.message}")
        }
    }

    // 자동 로그인 처리
    fun performAutoLogin(callback: (Boolean, String?) -> Unit) {
        val loginInfo = dbHelper.getLoginInfo() // SQLite에서 정보 가져오기
        if (loginInfo != null) {
            performServerLogin(loginInfo.first, loginInfo.second, callback) // 서버 로그인 요청
        } else {
            callback(false, "저장된 로그인 정보가 없습니다.")
        }
    }

//    // 로그인 정보를 서버로 전송
//    private fun sendLoginInfoToServer(loginInfo: LoginInfo, callback: (Boolean, String?) -> Unit) {
//        RetrofitInstance.loginApi.addLoginInfo(loginInfo).enqueue(object : Callback<LoginInfo> {
//            override fun onResponse(call: Call<LoginInfo>, response: Response<LoginInfo>) {
//                if (response.isSuccessful) {
//                    callback(true, null) // 성공 콜백
//                } else {
//                    callback(false, "서버 전송 실패: ${response.errorBody()?.string()}")
//                }
//            }
//
//            override fun onFailure(call: Call<LoginInfo>, t: Throwable) {
//                callback(false, "서버 전송 중 오류 발생: ${t.message}")
//            }
//        })
//    }
    // 로그인 정보를 서버로 전송하는 부분에서 loginType을 추가
    fun sendLoginInfoToServer(loginInfo: LoginInfo, callback: (Boolean, String?) -> Unit) {
        val loginType = dbHelper.getLoginType() ?: "" // DB에서 로그인 유형 가져오기
        val loginInfoWithType = loginInfo.copy(loginType = loginType) // 로그인 정보에 loginType 추가

        RetrofitInstance.loginApi.addLoginInfo(loginInfoWithType).enqueue(object : Callback<LoginInfo> {
            override fun onResponse(call: Call<LoginInfo>, response: Response<LoginInfo>) {
                if (response.isSuccessful) {
                    callback(true, null) // 서버로 성공적으로 전송
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
