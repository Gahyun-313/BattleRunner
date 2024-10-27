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
    val myPageFragment = MyPageFragment()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // binding 초기화
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // BattleApplyActivity에서 전달받은 데이터 확인
        if (intent.getBooleanExtra("loadBattleFragment", false)) {
            val userName = intent.getStringExtra("userName")
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
                R.id.battle -> setFragment(battleFragment)
                R.id.myPage -> setFragment(myPageFragment)
            }
            true
        }
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
}