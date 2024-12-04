package com.example.battlerunner.ui.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.battlerunner.data.repository.LoginRepository

class SplashViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LoginRepository = LoginRepository(application)

    // 자동 로그인 상태를 관리할 LiveData
    private val _autoLoginStatus = MutableLiveData<Boolean>()
    val autoLoginStatus: LiveData<Boolean> get() = _autoLoginStatus

    // 에러 상태를 관리할 LiveData
    private val _errorStatus = MutableLiveData<String?>()
    val errorStatus: LiveData<String?> get() = _errorStatus

    // 자동 로그인 여부 확인
    fun checkAutoLogin() {
        repository.performAutoLogin { isLoggedIn, errorMessage ->
            if (isLoggedIn) {
                _autoLoginStatus.postValue(true)
            } else {
                _autoLoginStatus.postValue(false)
                _errorStatus.postValue(errorMessage) // 에러 메시지 설정
            }
        }
    }
}