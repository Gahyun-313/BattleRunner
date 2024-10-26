package com.example.battlerunner

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.battlerunner.data.repository.LoginRepository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task

class LoginViewModel(application: Application) : AndroidViewModel(application) {  // AndroidViewModel을 상속합니다.

    private val repository: LoginRepository = LoginRepository(application)  // LoginRepository 인스턴스를 생성합니다.

    // 로그인 상태와 오류 메시지를 위한 LiveData를 선언합니다.
    val loginStatus = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()

    // 자체 로그인을 처리하는 메서드입니다.
    fun handleCustomLogin() {
        repository.performCustomLogin { success, message ->
            if (success) {
                loginStatus.postValue(true)  // 로그인 성공 시 로그인 상태를 true로 업데이트합니다.
            } else {
                errorMessage.postValue(message)  // 로그인 실패 시 오류 메시지를 업데이트합니다.
            }
        }
    }

    // 카카오 로그인을 처리하는 메서드입니다.
    fun handleKakaoLogin(activity: AppCompatActivity) {
        repository.performKakaoLogin(activity) { success, message ->
            if (success) {
                loginStatus.postValue(true)
            } else {
                errorMessage.postValue(message)
            }
        }
    }


    // Google 로그인 결과를 처리하는 메서드입니다.
    fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
        repository.performGoogleLogin(task) { success, message ->
            if (success) {
                loginStatus.postValue(true)  // 로그인 성공 시 로그인 상태를 true로 업데이트합니다.
            } else {
                errorMessage.postValue(message)  // 로그인 실패 시 오류 메시지를 업데이트합니다.
            }
        }
    }
}
