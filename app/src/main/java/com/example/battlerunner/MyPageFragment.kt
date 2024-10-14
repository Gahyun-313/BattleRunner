package com.example.battlerunner

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.kakao.sdk.user.UserApiClient

// TODO [갤러리에서 가져온 사진 넣기] https://velog.io/@ouowinnie/AndroidKotlin-%EA%B0%A4%EB%9F%AC%EB%A6%AC-%EC%9D%B4%EB%AF%B8%EC%A7%80-%EA%B0%80%EC%A0%B8%EC%98%A4%EA%B8%B0-Edit-Profile

//private const val ARG_PARAM1 = "param1"

class MyPageFragment : Fragment(R.layout.fragment_mypage) {

    private lateinit var dbHelper: DBHelper
    private lateinit var userIdTextView: TextView
    private lateinit var userNickTextView: TextView

    // [ onCreate ] 프래그먼트 생성될 때 호출됨. arguments로 받은 데이터에서 값 가져올 수 있음
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    // [ onCreateView ] UI 초기화 메서드
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_mypage, container, false)

        // DBHelper 초기화
        dbHelper = DBHelper(requireContext())
        // TextView 초기화
        userIdTextView = view.findViewById(R.id.userId)
        userNickTextView = view.findViewById(R.id.userName)

        // SharedPreferences에서 userId 가져오기
        val sharedPref = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("userId", null) // 사용자의 ID

        // DB에서 사용자 정보 가져오기
        val userInfo = dbHelper.getUserInfo(userId)

        // 사용자 정보가 존재하면 UI에 표시
        if (userInfo != null) {
            userIdTextView.text = userInfo.first  // ID 설정
            userNickTextView.text = userInfo.second  // 닉네임 설정
        } else {
            // 사용자 정보가 없을 때 처리
            userIdTextView.text = "ID not found"
            userNickTextView.text = "Nick not found"
        }

        // Todo: kakao logout -> 일반 logout 변경
        val kakaoLogoutButton = view.findViewById<Button>(R.id.kakao_logout_button)
        // 로그아웃 버튼 클릭시
        kakaoLogoutButton.setOnClickListener {
            UserApiClient.instance.accessTokenInfo { tokenInfo, error ->
                if (error != null) {
                    // 이미 로그아웃된 상태이거나 토큰이 없는 경우
                    Toast.makeText(requireContext(), "이미 로그아웃된 상태입니다.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    startActivity(intent)
                    activity?.finish()
                } else {
                    // 로그아웃 수행
                    UserApiClient.instance.logout { logoutError ->
                        if (logoutError != null) {
                            Toast.makeText(
                                requireContext(),
                                "로그아웃 실패: ${logoutError.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(requireContext(), "로그아웃 성공", Toast.LENGTH_SHORT).show()
                            val intent = Intent(requireContext(), LoginActivity::class.java)
                            startActivity(intent)
                            activity?.finish()
                        }
                    }
                }
            }
        }

        return view
    }
}