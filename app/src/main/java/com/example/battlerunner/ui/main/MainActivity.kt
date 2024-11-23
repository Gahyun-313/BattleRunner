package com.example.battlerunner.ui.main

import android.annotation.SuppressLint
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
import androidx.lifecycle.ViewModelProvider
import com.example.battlerunner.GlobalApplication
import com.example.battlerunner.R
import com.example.battlerunner.data.local.DBHelper
import com.example.battlerunner.databinding.ActivityMainBinding
import com.example.battlerunner.ui.battle.BattleFragment
import com.example.battlerunner.ui.battle.BattleViewModel
import com.example.battlerunner.ui.battle.MatchingFragment
import com.example.battlerunner.ui.community.CommunityFragment
import com.example.battlerunner.ui.home.HomeFragment
import com.example.battlerunner.ui.home.HomeViewModel
import com.example.battlerunner.ui.mypage.MyPageFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val homeFragment by lazy { HomeFragment() }
    private val battleFragment by lazy { BattleFragment() }
    private val matchingFragment by lazy { MatchingFragment() }
    private val myPageFragment by lazy { MyPageFragment() }
    private val communityFragment by lazy { CommunityFragment() }

    val homeViewModel: HomeViewModel by lazy {
        (application as GlobalApplication).homeViewModel
    }

    val battleViewModel: BattleViewModel by lazy {
        (application as GlobalApplication).battleViewModel
    }

    // [ 배틀 매칭 ]
    private var isInBattle = false // 배틀 중 여부를 저장
    // isMatched 삭제 -> isInBattle과 합침

    var startPathDrawing: (() -> Unit)? = null  // Home 경로 그리기 시작 콜백
    var stopPathDrawing: (() -> Unit)? = null   // Home 경로 그리기 중지 콜백
    var resetPathDrawing: (() -> Unit)? = null  // Home 경로 초기화 콜백
    var startTracking: (() -> Unit)? = null     // Battle 소유권 추적 시작 콜백
    var stopTracking: (() -> Unit)? = null      // Battle 소유권 추적 중지 콜백

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setStatusBarTransparent() // 상태바를 투명하게 설정

        // BattleApplyActivity 에서 데이터 전달 시 처리
        if (intent.getBooleanExtra("loadBattleFragment", false)) {
            val userName = intent.getStringExtra("userName") // 배틀 상대 이름
            isInBattle = true // 배틀(매칭) 상태 설정

            val newBattleFragment = BattleFragment() // BattleFragment 인스턴스 생성
            newBattleFragment.arguments = Bundle().apply {
                putString("userName", userName) // Battle Fragment 배틀 상대 이름 전달
            }

            showFragment(newBattleFragment, "BattleFragment") // BattleFragment로 전환
            binding.bottomNavigationMenu.selectedItemId = R.id.battle // 네비게이션 메뉴 업데이트

        } else if (intent.getBooleanExtra("showMatchingFragment", false)) {
            isInBattle = false // 배틀(매칭) 상태를 초기화

            showFragment(matchingFragment, "MatchingFragment") // MatchingFragment로 전환
            binding.bottomNavigationMenu.selectedItemId = R.id.battle // 네비게이션 메뉴 업데이트
        } else {
            // 앱 시작할 때 초기 프래그먼트 설정
            if (savedInstanceState == null) {
                supportFragmentManager.beginTransaction()
                    .add(R.id.fragmentContainer, homeFragment, "HomeFragment")
                    .commit()
            }
            // 초기 네비게이션 홈바 설정
            binding.bottomNavigationMenu.selectedItemId = R.id.home

        }

        // 네비게이션 클릭에 따른 프래그먼트 화면 전환
        binding.bottomNavigationMenu.setOnItemSelectedListener{
            when(it.itemId) {
                R.id.home -> showFragment(homeFragment, "HomeFragment")
                R.id.battle -> navigateToBattleOrMatchingFragment() // 배틀 여부 및 매칭 상태에 따른 이동 설정
                R.id.community -> showFragment(communityFragment, "CommunityFragment")
                R.id.myPage -> showFragment(myPageFragment, "MyPageFragment")
            }
            true
        }
    }

    // 프래그먼트 전환 메서드
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

    // 배틀 상태에 따라 Battle Fragment 또는 Matching Fragment로 이동
    private fun navigateToBattleOrMatchingFragment() {
        val existingBattleFragment = supportFragmentManager.findFragmentByTag("BattleFragment")

        val fragment = if (isInBattle) { // 배틀 중인지 확인
            if (existingBattleFragment != null) {
                existingBattleFragment as BattleFragment // 기존 BattleFragment 재사용
            } else {
                BattleFragment().apply { // 새 BattleFragment 생성
                    arguments = Bundle().apply {
                        putString("userName", intent.getStringExtra("userName")) // 배틀 상대 이름 전달
                    }
                }
            }
        } else {
            matchingFragment // MatchingFragment 사용
        }

        showFragment(fragment,
            if (isInBattle) "BattleFragment"
            else "MatchingFragment"
        )
    }

    // 상태바 투명 설정 함수
    private fun Activity.setStatusBarTransparent() {
        window.apply {
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            statusBarColor = Color.TRANSPARENT
        }
    }

    // 경로 그리기 요청 메서드 (Battle -> Home)
    fun notifyPathDrawing(boolean: Boolean) {
        if (boolean) {
            startPathDrawing?.invoke() // 경로 그리기 시작
        } else {
            stopPathDrawing?.invoke() // 경로 그리기 중지
        }
    }
    // 그렸던 경로 지우는(리셋) 요청 메서드 (Battle -> Home)
    fun notifyPathReset() {
        resetPathDrawing?.invoke()
    }

    // 그리드 소유권 추적 요청 메서드 (Home -> Battle)
    fun notifyTracking(boolean: Boolean) {
        if (boolean) {
            startTracking?.invoke() // 그리드 소유권 추적 시작
        } else {
            stopTracking?.invoke() // 그리드 소유권 추적 중지
        }
    }
}