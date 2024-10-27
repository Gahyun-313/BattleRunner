package com.example.battlerunner  // 패키지 정의

import android.annotation.SuppressLint  // SuppressLint 어노테이션 임포트
import android.content.Context  // Context 클래스 임포트
import android.content.Intent  // Intent 클래스 임포트
import android.os.Bundle  // Bundle 클래스 임포트
import androidx.fragment.app.Fragment  // Fragment 클래스 임포트
import android.view.LayoutInflater  // LayoutInflater 클래스 임포트
import android.view.View  // View 클래스 임포트
import android.view.ViewGroup  // ViewGroup 클래스 임포트
import android.widget.Button  // Button 클래스 임포트
import android.widget.TextView  // TextView 클래스 임포트
import android.widget.Toast  // Toast 클래스 임포트
import com.example.battlerunner.data.local.DBHelper
import com.example.battlerunner.ui.login.LoginActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn  // GoogleSignIn 클래스 임포트
import com.google.android.gms.auth.api.signin.GoogleSignInClient  // GoogleSignInClient 클래스 임포트
import com.google.android.gms.auth.api.signin.GoogleSignInOptions  // GoogleSignInOptions 클래스 임포트
import com.kakao.sdk.user.UserApiClient  // Kakao UserApiClient 클래스 임포트

class MyPageFragment : Fragment(R.layout.fragment_mypage) {  // MyPageFragment 클래스 정의, Fragment 상속 및 레이아웃 설정

    private lateinit var dbHelper: DBHelper  // DBHelper 인스턴스 변수 선언
    private lateinit var googleSignInClient: GoogleSignInClient  // Google 로그인 클라이언트 인스턴스 변수 선언
    private lateinit var userIdTextView: TextView  // 사용자 ID를 표시할 TextView 인스턴스 변수 선언
    private lateinit var userNickTextView: TextView  // 사용자 닉네임을 표시할 TextView 인스턴스 변수 선언

    override fun onCreate(savedInstanceState: Bundle?) {  // Fragment가 생성될 때 호출되는 메서드
        super.onCreate(savedInstanceState)

        // GoogleSignInOptions 설정
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))  // Firebase와 연동할 클라이언트 ID 요청
            .requestEmail()  // 이메일 요청
            .build()

        // GoogleSignInClient 초기화
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)  // Activity 컨텍스트와 GoogleSignInOptions 객체 전달
    }

    @SuppressLint("SetTextI18n", "MissingInflatedId")  // SuppressLint 어노테이션 사용
    override fun onCreateView(  // UI를 초기화할 때 호출되는 메서드
        inflater: LayoutInflater,  // 레이아웃 인플레이터 인스턴스
        container: ViewGroup?,  // 부모 ViewGroup 인스턴스
        savedInstanceState: Bundle?  // 저장된 인스턴스 상태
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mypage, container, false)  // MyPageFragment의 레이아웃을 인플레이트하여 view에 저장

        dbHelper = DBHelper.getInstance(requireContext())  // DBHelper 인스턴스 초기화

        userIdTextView = view.findViewById(R.id.userId)  // 사용자 ID를 표시할 TextView 초기화
        userNickTextView = view.findViewById(R.id.userName)  // 사용자 닉네임을 표시할 TextView 초기화

        val sharedPref = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)  // SharedPreferences에서 세션 정보 가져오기
        val userId = sharedPref.getString("userId", null)  // 사용자 ID 가져오기
        val loginType = sharedPref.getString("loginType", "custom")  // 로그인 타입 가져오기 (기본값은 "custom")

        // DBHelper를 통해 사용자 정보를 가져옵니다.
        val userInfo = dbHelper.getUserInfo()
        if (userInfo != null) {
            userIdTextView.text = userInfo.first //아이디
            userNickTextView.text = userInfo.second //이름
        } else {
            userIdTextView.text = "ID not found"
            userNickTextView.text = "Nick not found"
        }

        val logoutButton = view.findViewById<Button>(R.id.logoutButton)  // 로그아웃 버튼 초기화

        logoutButton.setOnClickListener {  // 로그아웃 버튼 클릭 시
            when (loginType) {  // loginType에 따라 분기 처리
                "kakao" -> logoutFromKakao()  // 카카오 로그아웃 처리
                "google" -> logoutFromGoogle()  // 구글 로그아웃 처리
                else -> logoutAndRedirect()  // 자체 로그인 로그아웃 처리
            }
        }

        return view  // 초기화된 view 반환
    }

    private fun logoutFromKakao() {  // 카카오 로그아웃 처리 메서드
        UserApiClient.instance.logout { error ->  // 카카오 로그아웃 시도
            if (error != null) {  // 로그아웃 실패 시
                Toast.makeText(requireContext(), "카카오 로그아웃 실패: ${error.message}", Toast.LENGTH_SHORT).show()  // 실패 메시지 출력
            } else {  // 로그아웃 성공 시
                Toast.makeText(requireContext(), "카카오 로그아웃 성공", Toast.LENGTH_SHORT).show()  // 성공 메시지 출력
                logoutAndRedirect()  // 로컬 로그아웃 및 리다이렉트 처리
            }
        }
    }

    private fun logoutFromGoogle() {  // 구글 로그아웃 처리 메서드
        googleSignInClient.signOut().addOnCompleteListener { task ->  // 구글 로그아웃 시도
            if (task.isSuccessful) {  // 로그아웃 성공 시
                Toast.makeText(requireContext(), "구글 로그아웃 성공", Toast.LENGTH_SHORT).show()  // 성공 메시지 출력
                logoutAndRedirect()  // 로컬 로그아웃 및 리다이렉트 처리
            } else {  // 로그아웃 실패 시
                Toast.makeText(requireContext(), "구글 로그아웃 실패", Toast.LENGTH_SHORT).show()  // 실패 메시지 출력
            }
        }
    }

    private fun logoutAndRedirect() {  // 로컬 로그아웃 및 로그인 화면으로 리다이렉트하는 메서드
        dbHelper.deleteLoginInfo()  // DBHelper를 통해 로그인 정보 삭제

        val sharedPref = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)  // SharedPreferences 인스턴스 가져오기
        sharedPref.edit().clear().apply()  // SharedPreferences의 세션 정보 초기화

        val intent = Intent(requireContext(), LoginActivity::class.java)  // LoginActivity로 이동하는 Intent 생성
        startActivity(intent)  // LoginActivity 시작
        activity?.finish()  // 현재 액티비티 종료
    }
}
