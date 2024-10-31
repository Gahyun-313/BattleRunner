package com.example.battlerunner.ui.login

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.battlerunner.data.repository.LoginRepository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task

class LoginViewModel(application: Application) : AndroidViewModel(application) {  // AndroidViewModel을 상속합니다.

    private val repository: LoginRepository = LoginRepository(application)  // LoginRepository 인스턴스를 생성합니다.

    // 로그인 상태와 오류 메시지를 위한 LiveData를 선언
    val loginStatus = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()

    // 자동 로그인 확인
    fun checkAutoLogin() {
        repository.performAutoLogin { isLoggedIn ->
            loginStatus.postValue(isLoggedIn)
        }
    }

    // 카카오 로그인 요청
    fun performKakaoLogin() {
        repository.performKakaoLogin { success, message ->
            if (success) {
                loginStatus.postValue(true)
            } else {
                errorMessage.postValue(message)
            }
        }
    }

    // 구글 로그인 결과 처리
    fun performGoogleLogin(task: Task<GoogleSignInAccount>) {
        repository.performGoogleLogin(task) { success, message ->
            if (success) {
                loginStatus.postValue(true)
            } else {
                errorMessage.postValue(message)
            }
        }
    }
}