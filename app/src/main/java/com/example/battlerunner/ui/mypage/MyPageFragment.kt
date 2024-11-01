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
import androidx.lifecycle.ViewModelProvider
import com.example.battlerunner.R
import com.example.battlerunner.data.local.DBHelper
import com.example.battlerunner.ui.logout.LogoutActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class MyPageFragment : Fragment(R.layout.fragment_mypage) {

    private lateinit var dbHelper: DBHelper
    private lateinit var userIdTextView: TextView
    private lateinit var userNameTextView: TextView
    private lateinit var logoutViewModel: LogoutViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // fragment_mypage 레이아웃을 inflate하여 view에 저장
        val view = inflater.inflate(R.layout.fragment_mypage, container, false)

        // DBHelper 인스턴스 초기화
        dbHelper = DBHelper.getInstance(requireContext())
        userIdTextView = view.findViewById(R.id.userId)  // 사용자 ID 표시할 TextView
        userNameTextView = view.findViewById(R.id.userName)  // 사용자 이름 표시할 TextView

        // Google 로그인 설정을 위한 옵션 생성
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))  // Firebase ID 토큰 요청
            .requestEmail()  // 이메일 정보 요청
            .build()

        // LogoutViewModel 초기화 (GoogleSignInClient와 DBHelper 전달)
        logoutViewModel = ViewModelProvider(
            this,
            LogoutViewModelFactory(requireActivity().application, GoogleSignIn.getClient(requireActivity(), gso), dbHelper)
        )[LogoutViewModel::class.java]

        // DBHelper를 통해 사용자 정보 가져와 화면에 표시
        val userInfo = dbHelper.getUserInfo()
        if (userInfo != null) {
            userIdTextView.text = userInfo.first
            userNameTextView.text = userInfo.second
        } else {
            userIdTextView.text = "ID not found"
            userNameTextView.text = "Name not found"
        }

        // 로그아웃 버튼 클릭 시 ViewModel을 통해 로그아웃 실행
        val logoutButton = view.findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            // SharedPreferences에서 loginType 가져와 ViewModel에 전달
            val sharedPref = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
            val loginType = sharedPref.getString("loginType", "custom") ?: "custom"
            logoutViewModel.logout(requireContext(), loginType)
        }

        return view
    }
}
