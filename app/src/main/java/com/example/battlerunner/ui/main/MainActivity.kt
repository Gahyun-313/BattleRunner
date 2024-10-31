package com.example.battlerunner.ui.main

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.battlerunner.R
import com.example.battlerunner.databinding.ActivityMainBinding
import com.example.battlerunner.ui.battle.BattleFragment
import com.example.battlerunner.ui.home.HomeFragment
import com.example.battlerunner.ui.mypage.MyPageFragment

class MainActivity : AppCompatActivity() {

    private val homeFragment = HomeFragment()
    private val battleFragment = BattleFragment()
    private val myPageFragment = MyPageFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //앱 시작할 때 > 프래그먼트, 홈바를 각각 home으로 시작
        setFragment(homeFragment)
        binding.bottomNavigationMenu.selectedItemId = R.id.home

        //홈바 클릭에 따른 프래그먼트 화면 전환
        binding.bottomNavigationMenu.setOnItemSelectedListener{
            when(it.itemId) {
                R.id.home -> setFragment(homeFragment)
                R.id.battle -> setFragment(battleFragment)
                R.id.myPage -> setFragment(myPageFragment)
            }
            true
        }
    }



    // 프래그먼트 전환 함수
    private fun setFragment (fragment: Fragment) {
        Log.d("MainActivity", "{$fragment}")
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentContainer, fragment)   // 첫 번째 인자에 두 번째 인자를 보여준다는 뜻

            // 프래그먼트에 따른 상태바 색상 변경
            if (fragment == myPageFragment) {
                window.statusBarColor = ContextCompat.getColor(this@MainActivity, R.color.blue0)
            }

            commit()
        }
    }
}