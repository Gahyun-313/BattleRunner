package com.example.battlerunner.ui.battle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.battlerunner.R
import com.example.battlerunner.data.model.User
import com.example.battlerunner.databinding.FragmentMatchingBinding
import com.example.battlerunner.network.RetrofitInstance
import com.example.battlerunner.ui.main.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MatchingFragment : Fragment() {

    private var _binding: FragmentMatchingBinding? = null
    private val binding get() = _binding!!

    private val battleApi = RetrofitInstance.battleApi // BattleApi 인스턴스
    private val userApi = RetrofitInstance.userApi // User API 인스턴스
    private lateinit var userAdapter: UserAdapter // RecyclerView 어댑터

    private val userList = mutableListOf<User>() // 검색 결과 저장

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMatchingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView 초기화
        userAdapter = UserAdapter(userList) { userId, userName ->
            requestBattle("loggedInUserId", userId, userName) // 배틀 신청 호출
        }
        binding.userRecyclerView.layoutManager = LinearLayoutManager(context) // 레이아웃 설정
        binding.userRecyclerView.adapter = userAdapter // 어댑터 설정

        // 검색 버튼 클릭 리스너 설정
        binding.searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    if (it.isNotEmpty()) {
                        searchUserById(it) // 사용자 검색 호출
                    } else {
                        Toast.makeText(requireContext(), "검색어를 입력하세요", Toast.LENGTH_SHORT).show()
                    }
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean = false
        })
    }

    // 사용자 검색 요청
    private fun searchUserById(userId: String) {
        lifecycleScope.launch {
            try {
                // 서버에서 사용자 검색
                val response = withContext(Dispatchers.IO) {
                    userApi.findUserById(userId) // User API 호출
                }

                if (response.isSuccessful) {
                    val user = response.body() // 응답에서 User 객체 추출
                    if (user != null) {
                        userList.clear() // 기존 검색 결과 초기화
                        userList.add(user) // 검색 결과 추가
                        userAdapter.notifyDataSetChanged() // 어댑터에 변경 사항 알림
                        binding.userRecyclerView.visibility = View.VISIBLE // RecyclerView 표시
                    } else {
                        Toast.makeText(requireContext(), "사용자를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                        binding.userRecyclerView.visibility = View.GONE // RecyclerView 숨김
                    }
                } else {
                    // 서버 응답 실패 처리
                    Toast.makeText(requireContext(), "검색 실패: ${response.message()}", Toast.LENGTH_SHORT).show()
                    binding.userRecyclerView.visibility = View.GONE // RecyclerView 숨김
                }
            } catch (e: Exception) {
                // 네트워크 또는 기타 에러 처리
                Toast.makeText(requireContext(), "검색 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // 배틀 신청 요청
    private fun requestBattle(user1Id: String, user2Id: String, userName: String) {
        lifecycleScope.launch {
            try {
                // 서버로 배틀 신청 요청
                val battle = withContext(Dispatchers.IO) {
                    battleApi.requestBattle(user1Id, user2Id) // Battle API 호출
                }

                // 배틀 신청 성공 시 처리
                Toast.makeText(requireContext(), "배틀 신청 성공: ${battle.battleId}", Toast.LENGTH_SHORT).show()

                // BattleFragment로 전환
                navigateToBattleFragment(userName)

            } catch (e: Exception) {
                // 배틀 신청 실패 처리
                Toast.makeText(requireContext(), "배틀 신청 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // BattleFragment로 이동
    private fun navigateToBattleFragment(userName: String) {
        (activity as? MainActivity)?.let {
            it.isInBattle = true // 배틀 상태 설정
            val newBattleFragment = BattleFragment().apply {
                arguments = Bundle().apply {
                    putString("userName", userName) // BattleFragment로 배틀 상대 이름 전달
                }
            }
            it.showFragment(newBattleFragment, "BattleFragment") // 프래그먼트 전환
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 뷰 바인딩 해제
    }
}
