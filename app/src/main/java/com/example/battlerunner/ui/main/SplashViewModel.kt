package com.example.battlerunner.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.battlerunner.data.repository.LoginRepository

class SplashViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LoginRepository = LoginRepository(application)

    // 자동 로그인 상태를 관리할 MutableLiveData
    private val _autoLoginStatus = MutableLiveData<Boolean>()
    val autoLoginStatus: LiveData<Boolean> get() = _autoLoginStatus

    // 자동 로그인 여부를 확인하는 메서드
    fun checkAutoLogin() {
        // LoginRepository의 performAutoLogin 메서드를 호출하여 자동 로그인 여부 확인
        repository.performAutoLogin { isLoggedIn ->
            _autoLoginStatus.postValue(isLoggedIn)  // 자동 로그인 결과를 LiveData에 업데이트
        }
    }
}