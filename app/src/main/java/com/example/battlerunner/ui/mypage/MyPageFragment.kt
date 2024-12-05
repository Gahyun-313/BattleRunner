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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.battlerunner.R
import com.example.battlerunner.data.local.DBHelper
import com.example.battlerunner.data.model.BattleRecord
import com.example.battlerunner.ui.logout.LogoutActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

/**
 * 마이페이지 프래그먼트
 * ㅇ 사용자 정보 확인
 * ㅇ 개인 러닝 기록 확인
 * ㅇ 배틀 기록 확인
 * ㅇ 로그아웃
 */

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

        // DBHelper 초기화
        dbHelper = DBHelper.getInstance(requireContext())

        userIdTextView = view.findViewById(R.id.userId)  // 사용자 ID 표시할 TextView
        userNameTextView = view.findViewById(R.id.userName)  // 사용자 이름 표시할 TextView

        // Google 로그인 옵션 초기화
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))  // Firebase ID 토큰 요청
            .requestEmail()  // 이메일 정보 요청
            .build()

        // LogoutViewModel 초기화 (GoogleSignInClient와 DBHelper 전달)
        logoutViewModel = ViewModelProvider(
            this,
            LogoutViewModelFactory(requireActivity().application, GoogleSignIn.getClient(requireActivity(), gso), dbHelper)
        )[LogoutViewModel::class.java]

        // 사용자 정보 가져오기
        val userId = dbHelper.getUserId()
        val userName = dbHelper.getUserName()
        userIdTextView.text = userId   // ID 표시
        userNameTextView.text = userName // 이름 표시

        // <개인 러닝 기록> 캘린더 버튼 클릭 리스너
        val calendarButton = view.findViewById<Button>(R.id.calendarBtn)
        calendarButton.setOnClickListener {
            val intent = Intent(requireContext(), CalendarActivity::class.java)
            startActivity(intent)
        }

        // <배틀 기록> 기록 버튼 클릭 리스너
        val battleRecordBtn = view.findViewById<Button>(R.id.battleRecordBtn)
        battleRecordBtn.setOnClickListener {
            val battleRecords = dbHelper.getBattleRecords() // 배틀 기록 가져오기

            if (battleRecords.isEmpty()) {
                Toast.makeText(requireContext(), "배틀 기록이 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // 배틀 기록 팝업 표시
            showBattleRecordsPopup(battleRecords)
        }

        // <로그아웃> 로그아웃 버튼 클릭 시 ViewModel을 통해 로그아웃 실행
        val logoutButton = view.findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {

            // SharedPreferences에서 loginType 가져와 ViewModel에 전달
            val sharedPref = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
            val loginType = sharedPref.getString("loginType", "custom") ?: "custom"
            logoutViewModel.logout(requireContext(), loginType)
        }

        return view
    }

    // 배틀 기록 팝업 표시 메서드 (battleRecords 배틀 기록 리스트)
    private fun showBattleRecordsPopup(battleRecords: List<BattleRecord>) {
        // 팝업 레이아웃을 inflate
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.popup_battle_records, null)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.battleRecordsRecyclerView)

        // RecyclerView 설정
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = BattleRecordAdapter(battleRecords) { selectedRecord ->
            // 배틀 기록 클릭 시 상세 화면으로 이동
            showBattleRecordDetails(selectedRecord)
        }

        // 팝업 표시
        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("닫기", null)
            .show()
    }

    // 선택한 배틀 기록의 상세 정보를 표시하는 메서드 (record 선택된 배틀 기록)
    private fun showBattleRecordDetails(record: BattleRecord) {
        // BattleDetailActivity로 이동하며 기록 정보 전달
        val intent = Intent(requireContext(), BattleDetailActivity::class.java).apply {
            putExtra("battleDate", record.date)
            putExtra("opponentName", record.opponentName)
            putExtra("imagePath", record.imagePath)
        }
        startActivity(intent)
    }

}
