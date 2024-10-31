package com.example.battlerunner.ui.mypage

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.Button
import android.widget.TextView
import com.example.battlerunner.R
import com.example.battlerunner.data.local.DBHelper
import com.example.battlerunner.ui.logout.LogoutActivity

class MyPageFragment : Fragment(R.layout.fragment_mypage) {

    private lateinit var dbHelper: DBHelper
    private lateinit var userIdTextView: TextView
    private lateinit var userNameTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mypage, container, false)

        dbHelper = DBHelper.getInstance(requireContext())
        userIdTextView = view.findViewById(R.id.userId)
        userNameTextView = view.findViewById(R.id.userName)

        // 프로필 정보 나타내기
        val userInfo = dbHelper.getUserInfo()
        if (userInfo != null) {
            userIdTextView.text = userInfo.first; userNameTextView.text = userInfo.second;
        } else {
            userIdTextView.text = "ID not found"; userNameTextView.text = "Name not found";
        }

        // 로그아웃 버튼 클릭 시 LogoutActivity로 이동
        val logoutButton = view.findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            val intent = Intent(requireContext(), LogoutActivity::class.java)
            startActivity(intent)
        }

        return view
    }
}
