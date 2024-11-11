package com.example.battlerunner.ui.login

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.battlerunner.data.repository.LoginRepository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LoginRepository = LoginRepository(application)

    // 로그인 상태와 오류 메시지를 위한 LiveData
    val loginStatus = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()

    // 카카오 로그인
    fun handleKakaoLogin(activity: AppCompatActivity) {
        repository.performKakaoLogin(activity) { success, message ->
            if (success) loginStatus.postValue(true)
            else errorMessage.postValue(message)
        }
    }

    // Google 로그인
    suspend fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
        repository.performGoogleLogin(task) { success, message ->
            if (success) loginStatus.postValue(true)
            else errorMessage.postValue(message)
        }
    }
}