// LogoutViewModelFactory.kt
package com.example.battlerunner.ui.mypage

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.battlerunner.data.local.DBHelper
import com.google.android.gms.auth.api.signin.GoogleSignInClient

class LogoutViewModelFactory(
    private val application: Application,
    private val googleSignInClient: GoogleSignInClient,
    private val dbHelper: DBHelper
) : ViewModelProvider.Factory {

    // LogoutViewModel 인스턴스를 생성하여 반환
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LogoutViewModel::class.java)) {
            return LogoutViewModel(application, googleSignInClient, dbHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")  // ViewModel 클래스가 맞지 않을 때 예외 발생
    }
}
