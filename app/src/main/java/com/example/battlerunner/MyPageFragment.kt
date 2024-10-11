package com.example.battlerunner

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.kakao.sdk.user.UserApiClient

// TODO [갤러리에서 가져온 사진 넣기] https://velog.io/@ouowinnie/AndroidKotlin-%EA%B0%A4%EB%9F%AC%EB%A6%AC-%EC%9D%B4%EB%AF%B8%EC%A7%80-%EA%B0%80%EC%A0%B8%EC%98%A4%EA%B8%B0-Edit-Profile

//private const val ARG_PARAM1 = "param1"

class MyPageFragment : Fragment(R.layout.fragment_mypage) {

    // [ onCreate ] 프래그먼트 생성될 때 호출됨. arguments로 받은 데이터에서 값 가져올 수 있음
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // arguments?.let {
        //   param1 = it.getString(ARG_PARAM1)
        // }
    }

    // [ onCreateView ] UI 초기화 메서드
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_mypage, container, false)

        val kakaoLogoutButton = view.findViewById<Button>(R.id.kakao_logout_button)
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