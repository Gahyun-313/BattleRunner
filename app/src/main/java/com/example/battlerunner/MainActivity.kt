package com.example.battlerunner

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.battlerunner.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    val homeFragment = HomeFragment()
    val battleFragment = BattleFragment()
    val matchingFragment = MatchingFragment() // MatchingFragment 인스턴스 추가
    val myPageFragment = MyPageFragment()
    private lateinit var binding: ActivityMainBinding

    // 배틀 여부 및 매칭 상태 확인 변수
    private var isInBattle = false // 배틀 중 여부
    private var isMatched = false // 매칭 성공 여부를 저장

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // binding 초기화
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // BattleApplyActivity에서 전달받은 데이터 확인
        if (intent.getBooleanExtra("loadBattleFragment", false)) {
            val userName = intent.getStringExtra("userName")
            isMatched = true // 외부에서 매칭이 설정되었다고 가정
            val newBattleFragment = BattleFragment().apply {
                arguments = Bundle().apply {
                    putString("userName", userName)
                }
            }
            setFragment(newBattleFragment)
            binding.bottomNavigationMenu.selectedItemId = R.id.battle
        } else {
            // 앱 시작할 때, 프래그먼트, 홈바를 각각 home으로 시작
            setFragment(homeFragment)
            binding.bottomNavigationMenu.selectedItemId = R.id.home
        }

        // 홈바 클릭에 따른 프래그먼트 화면 전환
        binding.bottomNavigationMenu.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.home -> setFragment(homeFragment)
                R.id.battle -> navigateToBattleOrMatchingFragment() // 배틀 여부 및 매칭 상태에 따른 이동 설정
                R.id.myPage -> setFragment(myPageFragment)
            }
            true
        }
    }

    // 배틀 여부 및 매칭 상태에 따라 BattleFragment 또는 MatchingFragment로 이동하는 함수
    private fun navigateToBattleOrMatchingFragment() {
        val fragment = if (isInBattle || isMatched) {
            battleFragment // 배틀 중이거나 이미 매칭되었다면 BattleFragment로 이동
        } else {
            matchingFragment // 매칭되지 않은 경우 MatchingFragment로 이동
        }
        setFragment(fragment)
    }

    // 프래그먼트 전환 함수
    private fun setFragment(fragment: Fragment) {
        Log.d("MainActivity", "{$fragment}")
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentContainer, fragment)
            // 프래그먼트에 따른 상태바 색상 변경
            if (fragment == myPageFragment) {
                window.statusBarColor = ContextCompat.getColor(this@MainActivity, R.color.blue0)
            } else {
                window.statusBarColor = ContextCompat.getColor(this@MainActivity, R.color.white)
            }
            commit()
        }
    }

    // 외부에서 매칭 성공을 설정하는 메서드
    fun setMatched(matched: Boolean) {
        isMatched = matched
    }
}
