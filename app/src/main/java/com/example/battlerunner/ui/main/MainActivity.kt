package com.example.battlerunner.ui.main

import android.app.Activity
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.battlerunner.R
import com.example.battlerunner.data.local.DBHelper
import com.example.battlerunner.databinding.ActivityMainBinding
import com.example.battlerunner.ui.battle.BattleFragment
import com.example.battlerunner.ui.battle.MatchingFragment
import com.example.battlerunner.ui.community.CommunityFragment
import com.example.battlerunner.ui.home.HomeFragment
import com.example.battlerunner.ui.mypage.MyPageFragment

class MainActivity : AppCompatActivity() {

    private val homeFragment by lazy { HomeFragment() }
    private val battleFragment by lazy { BattleFragment() }
    private val matchingFragment by lazy { MatchingFragment() }
    private val myPageFragment by lazy { MyPageFragment() }
    private val communityFragment by lazy { CommunityFragment() }

    // HomeFragment에서 경로 그리기를 시작하도록 콜백 설정
    var startPathDrawing: (() -> Unit)? = null
    var stopPathDrawing: (() -> Unit)? = null

    // BattleFragment에서 소유권 업데이트 메서드 시작하도록 콜백 설정
    var startTracking: (() -> Unit)? = null
    var stopTracking: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setStatusBarTransparent() // 상태바를 투명하게 설정

        // 초기 프래그먼트 설정
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragmentContainer, homeFragment, "HomeFragment")
                .commit()
        }
        // 초기 네비게이션 홈바 설정
        binding.bottomNavigationMenu.selectedItemId = R.id.home

        //네비게이션 클릭에 따른 프래그먼트 화면 전환
        binding.bottomNavigationMenu.setOnItemSelectedListener{
            when(it.itemId) {
                R.id.home -> showFragment(homeFragment, "HomeFragment")
                R.id.battle -> showFragment(battleFragment, "BattleFragment")
                R.id.community -> showFragment(communityFragment, "CommunityFragment")
                R.id.myPage -> showFragment(myPageFragment, "MyPageFragment")
            }
            true
        }
    }

    // 프래그먼트를 전환하는 함수
    private fun showFragment(fragment: Fragment, tag: String) {
        val transaction = supportFragmentManager.beginTransaction()

        // 이미 추가된 프래그먼트인지 확인
        supportFragmentManager.fragments.forEach { transaction.hide(it) }

        if (supportFragmentManager.findFragmentByTag(tag) != null) {
            // 이미 추가된 경우 show
            transaction.show(fragment)
        } else {
            // 처음 추가하는 경우 add
            transaction.add(R.id.fragmentContainer, fragment, tag)
        }
        transaction.commit()
    }

    // Battle -> Home 경로 그리기 요청 메서드
    fun notifyPathDrawing(boolean: Boolean) {
        if (boolean) { // true -> 러닝 경로 그리기
            startPathDrawing?.invoke()
        } else { // true -> 러닝 경로 그리지 않기
            stopPathDrawing?.invoke()
        }
    }
    // Home -> Battle 그리드 소유권 추적 요청 메서드
    fun notifyTracking(boolean: Boolean) {
        if (boolean) { // true -> 그리드 소유권 추적 시작
            startTracking?.invoke()
        } else { // false -> 그리드 소유권 추적 중지
            stopTracking?.invoke()
        }
    }

    // 상태바 투명 설정 함수
    private fun Activity.setStatusBarTransparent() {
        window.apply {
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            statusBarColor = Color.TRANSPARENT
        }
    }
}