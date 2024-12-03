package com.example.battlerunner

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import com.example.battlerunner.ui.battle.BattleViewModel
import com.example.battlerunner.ui.home.HomeViewModel
import com.google.firebase.FirebaseApp
import com.kakao.sdk.common.KakaoSdk

class GlobalApplication : Application() {

    // HomeViewModel 초기화
    val homeViewModel: HomeViewModel by lazy {
        ViewModelProvider.AndroidViewModelFactory.getInstance(this)
            .create(HomeViewModel::class.java)
    }

    // BattleViewModel 초기화
    val battleViewModel: BattleViewModel by lazy {
        ViewModelProvider.AndroidViewModelFactory.getInstance(this)
            .create(BattleViewModel::class.java)
    }

    override fun onCreate() {
        super.onCreate()
        // 다른 초기화 코드들

        // Kakao SDK 초기화
        KakaoSdk.init(this, "16005e7a061659542eeaf1f020717f46")
        FirebaseApp.initializeApp(this)
    }
}
