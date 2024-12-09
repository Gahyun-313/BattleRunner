package com.example.battlerunner.ui.battle

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.battlerunner.data.local.DBHelper
import com.example.battlerunner.data.model.Battle
import com.example.battlerunner.data.model.User
import com.example.battlerunner.data.repository.BattleRepository
import com.example.battlerunner.databinding.FragmentMatchingBinding
import com.example.battlerunner.network.RetrofitInstance
import com.example.battlerunner.ui.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

class MatchingFragment : Fragment() {

    // 뷰 바인딩 객체 선언
    private var _binding: FragmentMatchingBinding? = null
    private val binding get() = _binding!!

    // DBHelper 초기화 변수
    private var dbHelper: DBHelper? = null

    // BattleRepository 객체 선언 (API 호출 관리)
    private lateinit var battleRepository: BattleRepository

    // 사용자 검색 결과를 표시하는 RecyclerView 어댑터
    private lateinit var userAdapter: UserAdapter

    // 검색된 사용자 목록을 저장할 리스트
    private val userList = mutableListOf<User>()

    private lateinit var opponentName: String //배틀 상대 이름
    private lateinit var opponentId: String //배틀 상대 이름

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // 뷰 바인딩 초기화
        _binding = FragmentMatchingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // DBHelper 초기화
        dbHelper = context?.let { DBHelper.getInstance(it) }

        // BattleRepository 초기화 (API 객체 전달)
        battleRepository = BattleRepository(RetrofitInstance.battleApi)

        // RecyclerView 어댑터 초기화
        userAdapter = UserAdapter(userList) { userId ->
            // 매칭 버튼 클릭 시 배틀 신청 호출
            dbHelper?.getUserId()?.let {
                // 디버그 로그 추가
                Log.d("MatchingFragment", "Button clicked: UserId=$userId")
                requestBattle(it, userId)
            }
        }

        // RecyclerView 레이아웃 설정
        binding.userRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.userRecyclerView.adapter = userAdapter

        // 검색 버튼 클릭 리스너 설정
        binding.searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // 검색어가 입력되었을 때 동작
                query?.let {
                    if (it.isNotEmpty()) {
                        searchUser(it) // 사용자 검색 호출
                    } else {
                        // 검색어가 비어있을 경우 경고 메시지 표시
                        Toast.makeText(requireContext(), "검색어를 입력하세요", Toast.LENGTH_SHORT).show()
                    }
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean = false // 텍스트 변경 시 동작하지 않음
        })
    }

    // 사용자 검색 요청 메서드
    private fun searchUser(query: String) {
        // CoroutineScope를 사용하여 비동기 작업 실행
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Retrofit API를 통해 사용자 정보 요청
                val response: Response<User> = RetrofitInstance.userApi.getUserInfo(query)

                if (response.isSuccessful && response.body() != null) {
                    // 요청이 성공적이고 사용자 정보가 존재하면 리스트에 추가
                    val user = response.body()!!
                    val filteredList = listOf(user)
                    opponentName = user.username
                    opponentId = user.userId

                    // UI 업데이트는 메인 스레드에서 실행
                    CoroutineScope(Dispatchers.Main).launch {
                        userAdapter.updateUserList(filteredList)
                        binding.userRecyclerView.visibility = View.VISIBLE // RecyclerView 표시
                    }
                } else {
                    // 사용자 정보가 없거나 요청 실패 시 처리
                    CoroutineScope(Dispatchers.Main).launch {
                        userAdapter.updateUserList(emptyList()) // RecyclerView를 빈 리스트로 설정
                        binding.userRecyclerView.visibility = View.GONE // RecyclerView 숨김
                        Toast.makeText(requireContext(), "사용자를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                // 네트워크 오류 발생 시 처리
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(requireContext(), "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 배틀 신청 요청 메서드
    private fun requestBattle(user1Id: String, user2Id: String) {
        // lifecycleScope를 사용하여 비동기 작업 실행
        lifecycleScope.launch {
            Log.d("MatchingFragment", "Requesting battle: User1=$user1Id, User2=$user2Id")

            try {
                // BattleRepository를 통해 서버로 배틀 생성 요청
                val response = withContext(Dispatchers.IO) {
                    battleRepository.createBattle(
                        Battle(
                            battleId = null, // 서버에서 자동 생성
                            user1Id = user1Id, // 현재 사용자 ID
                            user2Id = user2Id, // 상대 사용자 ID
                            isBattleStarted = true, // 배틀 시작 상태
                            gridStartLat = null, // 초기 Grid 위치
                            gridStartLng = null
                        )
                    )
                }

                if (response.isSuccessful) {
                    // 요청이 성공적일 경우 응답 데이터 처리
                    val battle = response.body()
                    if (battle != null) {
                        val battleId = battle.battleId
                        if (battleId != null) {
                            Toast.makeText(requireContext(), "배틀 신청 성공: $battleId", Toast.LENGTH_SHORT).show()
                            navigateToBattleFragment(opponentName, opponentId, battleId) // BattleFragment로 이동
                        }
                    } else {
                        // 응답 데이터가 없을 경우
                        Toast.makeText(requireContext(), "배틀 정보를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // 서버에서 에러 응답을 받은 경우
                    Toast.makeText(requireContext(), "배틀 신청 실패: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // 예외 발생 시 처리
                Toast.makeText(requireContext(), "배틀 신청 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // BattleFragment로 이동하는 메서드
    private fun navigateToBattleFragment(opponentName: String, opponentId: String, battleId: Long) {
        // MainActivity의 showFragment 메서드를 통해 프래그먼트 전환
        (activity as? MainActivity)?.let {
            it.isInBattle = true // 배틀 상태를 활성화
            val newBattleFragment = BattleFragment().apply {
                arguments = Bundle().apply {
                    putString("opponentName", opponentName) // BattleFragment로 상대 이름 전달
                    putString("opponentId", opponentId)
                    putLong("battleId", battleId) // BattleFragment로 배틀 ID 전달
                }
            }
            it.showFragment(newBattleFragment, "BattleFragment") // BattleFragment로 전환
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 뷰 바인딩 해제
    }
}
