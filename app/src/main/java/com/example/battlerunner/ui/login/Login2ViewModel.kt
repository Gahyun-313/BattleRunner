package com.example.battlerunner.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.battlerunner.data.repository.LoginRepository

class Login2ViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LoginRepository(application)

    val loginStatus = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()

    // 자체 로그인 요청 메서드
    fun performCustomLogin(userId: String, userPassword: String) {
        repository.performCustomLogin(userId, userPassword) { success, message ->
            if (success) {
                loginStatus.postValue(true)
            } else {
                errorMessage.postValue(message)
            }
        }
    }
}