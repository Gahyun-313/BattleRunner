package com.example.battlerunner.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.battlerunner.DBHelper
import com.example.battlerunner.data.repository.LoginRepository

class SplashViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: LoginRepository = LoginRepository(application)
    private val dbHelper: DBHelper = DBHelper.getInstance(application)  // DBHelper 인스턴스 생성


    // 자동 로그인 상태를 관리할 MutableLiveData
    private val _autoLoginStatus = MutableLiveData<Boolean>()
    val autoLoginStatus: LiveData<Boolean> get() = _autoLoginStatus

    // 자동 로그인 여부를 확인하는 메서드
    fun checkAutoLogin() {
        val loginInfo = dbHelper.getLoginInfo()  // login_info 테이블에서 로그인 정보 가져오기
        val loginType = dbHelper.getLoginType()  // 로그인 유형 확인
        // 로그인 정보가 있고, login_type이 "custom"이면 자동 로그인 성공 상태로 업데이트
        _autoLoginStatus.value = loginInfo != null && loginType == "custom"
    }
}
