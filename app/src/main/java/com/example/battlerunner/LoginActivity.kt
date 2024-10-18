package com.example.battlerunner

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
// import com.kakao.sdk.auth.LoginClient // 카카오 로그인 클라이언트 불러오기
import com.kakao.sdk.auth.model.OAuthToken // 카카오 OAuthToken 모델 불러오기
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.AuthErrorCause.* // 인증 오류 원인 불러오기
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient

class LoginActivity : AppCompatActivity() {

    // DBHelper 싱글턴 인스턴스를 저장할 변수
    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login) // 로그인 화면 레이아웃 설정

        KakaoSdk.init(this, "16005e7a061659542eeaf1f020717f46")

        // DBHelper 싱글턴 인스턴스를 가져와 초기화
        dbHelper = DBHelper.getInstance(this)

        // (임시) temp 버튼 클릭 이벤트 처리
        findViewById<Button>(R.id.temp_btn).setOnClickListener {
            // MainActivity 이동
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // 로그인 버튼 클릭 이벤트 처리
        findViewById<Button>(R.id.login_btn).setOnClickListener {

            // Todo : SharedPreferences 에서 저장된 로그인 정보 확인
            // Todo : 저장된 정보가 있다면 해당 정보로 서버에 로그인 요청

            // (임시) 자체 DB에 저장된 정보로 자동 로그인-------------------------------------

            // DB에서 ID를 가져옴
            val userId = dbHelper.getId()
            if (userId == null) {
                // ID가 없을 경우 Login2Activity 이동
                val intent = Intent(this@LoginActivity, Login2Activity::class.java)
                startActivity(intent)
                finish()

            } else {
                // ID가 있으면 비밀번호도 가져옴
                val userPassword = dbHelper.getPassword()
                if (userPassword != null) {
                    // 가져온 ID와 비밀번호로 사용자 인증 (로그인)
                    val checkUserpass = dbHelper.checkUserpass(userId, userPassword)
                    if (checkUserpass) {
                        // 인증 성공 시 사용자 세션 정보 저장
                        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                        sharedPref.edit().putString("userId", userId).apply()
                        Toast.makeText(this@LoginActivity, "로그인 되었습니다.", Toast.LENGTH_SHORT).show()

                        // MainActivity로 이동
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // 인증 실패 시 경고 메시지 출력
                        Toast.makeText(this@LoginActivity, "ID 또는 비밀번호가 잘못되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                }

            }
            // --------------------------------------------------------------------------
        }

        // https://blog.naver.com/sfchamster/223379488728

        // 카카오 로그인 버튼 클릭 이벤트 처리
        findViewById<ImageButton>(R.id.kakao_login_btn).setOnClickListener {

            // 카카오로그인 키 해시
            val keyHash = Utility.getKeyHash(this)
            Log.i("kakao keyHash", "keyHash: $keyHash")

            // 로그인 콜백 정의
            val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
                if (error != null) {
                    Log.e("LoginError", "Error: ${error.javaClass}, Message: ${error.message}")
                    // 오류 처리 함수 호출
                    handleLoginError(error)
                } else if (token != null) {
                    Log.i("LoginSuccess", "카카오 로그인 성공")
                    // 로그인 성공 시 메인 화면 이동
                    moveToMainActivity()
                }
            }

            // 로그인 시도 전 토큰이 있는지 확인
            checkTokenAndLogin(callback)
        }

        // 구글 로그인 버튼 클릭 이벤트 처리
        findViewById<ImageButton>(R.id.google_login_btn).setOnClickListener {
            Toast.makeText(this, "구글 로그인 버튼 클릭", Toast.LENGTH_SHORT).show()

            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // 토큰 정보를 확인하고 없으면 로그인 시도
    private fun checkTokenAndLogin(callback: (OAuthToken?, Throwable?) -> Unit) {
        UserApiClient.instance.accessTokenInfo { tokenInfo, error ->
            if (error != null) {
                Log.e("TokenError", "토큰이 없거나 만료됨. 다시 로그인 시도: ${error.message}")
                if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
                    UserApiClient.instance.loginWithKakaoTalk(this, callback = callback)
                } else {
                    UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
                }
            } else if (tokenInfo != null) {
                Log.i("TokenInfo", "토큰이 유효함, 메인 화면으로 이동")
                // 토큰이 유효하면 바로 메인 화면으로 이동
                moveToMainActivity()
            }
        }
    }

    // 로그인 성공 후 메인 화면으로 이동하는 함수
    private fun moveToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        finish() // 현재 액티비티 종료
    }

    // 로그인 실패 시 오류를 처리하는 함수
    private fun handleLoginError(error: Throwable) {
        when {
            error.toString() == AccessDenied.toString() -> {
                Toast.makeText(this, "접근이 거부 됨(동의 취소)", Toast.LENGTH_SHORT).show()
            }
            error.toString() == InvalidClient.toString() -> {
                Toast.makeText(this, "유효하지 않은 앱", Toast.LENGTH_SHORT).show()
            }
            error.toString() == InvalidGrant.toString() -> {
                Toast.makeText(this, "인증 수단이 유효하지 않아 인증할 수 없는 상태", Toast.LENGTH_SHORT).show()
            }
            error.toString() == InvalidRequest.toString() -> {
                Toast.makeText(this, "요청 파라미터 오류", Toast.LENGTH_SHORT).show()
            }
            error.toString() == InvalidScope.toString() -> {
                Toast.makeText(this, "유효하지 않은 scope ID", Toast.LENGTH_SHORT).show()
            }
            error.toString() == Misconfigured.toString() -> {
                Toast.makeText(this, "설정이 올바르지 않음(android key hash)", Toast.LENGTH_SHORT).show()
            }
            error.toString() == ServerError.toString() -> {
                Toast.makeText(this, "서버 내부 에러", Toast.LENGTH_SHORT).show()
            }
            error.toString() == Unauthorized.toString() -> {
                Toast.makeText(this, "앱이 요청 권한이 없음", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(this, "기타 에러 : ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
