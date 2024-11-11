//MainActivity
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
import com.example.battlerunner.BattleFragment
import com.example.battlerunner.MatchingFragment
import com.example.battlerunner.databinding.ActivityMainBinding
import com.example.battlerunner.ui.community.CommunityFragment
import com.example.battlerunner.ui.home.HomeFragment
import com.example.battlerunner.ui.mypage.MyPageFragment


class MainActivity : AppCompatActivity() {

    private val homeFragment by lazy { HomeFragment() }
    private val battleFragment by lazy { BattleFragment() }
    private val matchingFragment by lazy { MatchingFragment() } // 추가된 MatchingFragment
    private val myPageFragment by lazy { MyPageFragment() }
    private val communityFragment by lazy { CommunityFragment() }

    // HomeFragment에서 경로 그리기를 시작하도록 콜백 설정
    var startPathDrawing: (() -> Unit)? = null

    private lateinit var binding: ActivityMainBinding

    // 배틀 여부 및 매칭 상태 확인 변수
    private var isInBattle = false // 배틀 중 여부
    private var isMatched = false // 매칭 성공 여부를 저장

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // binding 초기화
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setStatusBarTransparent() // 상태바를 투명하게 설정

        // BattleApplyActivity에서 전달받은 데이터 확인
        if (intent.getBooleanExtra("loadBattleFragment", false)) {
            val userName = intent.getStringExtra("userName")
            isMatched = true // 외부에서 매칭이 설정되었다고 가정
            val newBattleFragment = BattleFragment() // BattleFragment 인스턴스 생성
            newBattleFragment.arguments = Bundle().apply {
                putString("userName", userName)
            }
            // setFragment 호출 시 tag를 함께 전달
            setFragment(newBattleFragment, "BattleFragment")
            binding.bottomNavigationMenu.selectedItemId = R.id.battle
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
        binding.bottomNavigationMenu.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> setFragment(homeFragment, "HomeFragment")
                R.id.battle -> navigateToBattleOrMatchingFragment() // 배틀 여부 및 매칭 상태에 따른 이동 설정
                R.id.community -> setFragment(communityFragment, "CommunityFragment")
                R.id.myPage -> setFragment(myPageFragment, "MyPageFragment")
            }
            true
        }
    }

    // 배틀 여부 및 매칭 상태에 따라 BattleFragment 또는 MatchingFragment로 이동하는 함수
    private fun navigateToBattleOrMatchingFragment() {
        val existingBattleFragment = supportFragmentManager.findFragmentByTag("BattleFragment")
        val fragment = if (isInBattle || isMatched) {
            if (existingBattleFragment != null) {
                // 이미 BattleFragment가 존재하면 해당 프래그먼트를 재사용
                existingBattleFragment as BattleFragment
            } else {
                // BattleFragment가 없으면 새로 생성
                BattleFragment().apply {
                    arguments = Bundle().apply {
                        putString("userName", intent.getStringExtra("userName"))
                    }
                }
            }
        } else {
            matchingFragment
        }
        setFragment(fragment, if (isInBattle || isMatched) "BattleFragment" else "MatchingFragment")
    }




    // 프래그먼트를 전환하는 함수
    private fun setFragment(fragment: Fragment, tag: String) {
        val transaction = supportFragmentManager.beginTransaction()

        // 모든 프래그먼트를 숨기고 필요한 프래그먼트만 보여줌
        supportFragmentManager.fragments.forEach { transaction.hide(it) }

        if (supportFragmentManager.findFragmentByTag(tag) != null) {
            // 이미 존재하는 프래그먼트를 보여줌
            transaction.show(fragment)
        } else {
            // 존재하지 않으면 새로 추가
            transaction.add(R.id.fragmentContainer, fragment, tag)
        }

        // 프래그먼트에 따른 상태바 색상 변경
        if (fragment == myPageFragment) {
            window.statusBarColor = ContextCompat.getColor(this@MainActivity, R.color.blue0)
        } else {
            window.statusBarColor = ContextCompat.getColor(this@MainActivity, R.color.white)
        }
        transaction.commit()
    }


    // BattleFragment에서 경로 그리기 요청 시 호출할 메서드
    fun notifyStartPathDrawing() {
        Log.d("MainActivity", "notifyStartPathDrawing invoked")
        startPathDrawing?.invoke()
    }

    // 외부에서 매칭 성공을 설정하는 메서드
    fun setMatched(matched: Boolean) {
        isMatched = matched
    }

    // 상태바 투명 설정 함수
    private fun Activity.setStatusBarTransparent() {
        window.apply {
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            statusBarColor = Color.TRANSPARENT
        }
    }
}
