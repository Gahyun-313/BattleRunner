package com.example.battlerunner

import android.content.Context
import com.example.battlerunner.DBHelper
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

class LoginRepository(private val context: Context) {

    private val dbHelper: DBHelper = DBHelper.getInstance(context)  // DBHelper 인스턴스를 가져옵니다.

    // 자체 로그인 처리 메서드
    fun performCustomLogin(callback: (Boolean, String?) -> Unit) {
        val userId = dbHelper.getId()  // 사용자 ID 가져오기
        val password = dbHelper.getPassword()  // 사용자 비밀번호 가져오기

        if (userId != null && password != null && dbHelper.checkUserPass(userId, password)) {  // ID와 비밀번호 확인
            callback(true, null)  // 로그인 성공 시 콜백 호출
        } else {
            callback(false, "ID 또는 비밀번호가 잘못되었습니다.")  // 로그인 실패 시 콜백에 오류 메시지 전달
        }
    }

    // 카카오 로그인 처리 메서드
    fun performKakaoLogin(callback: (Boolean, String?) -> Unit) {
        // Kakao SDK 로그인 처리 로직 (DBHelper 사용)
        // 성공 시 callback(true, null), 실패 시 callback(false, 오류 메시지)
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
}

