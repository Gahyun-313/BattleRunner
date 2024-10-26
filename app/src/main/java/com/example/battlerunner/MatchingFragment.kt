package com.example.battlerunner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.battlerunner.databinding.FragmentMatchingBinding

class MatchingFragment : Fragment() {

    private var _binding: FragmentMatchingBinding? = null
    private val binding get() = _binding!!

    private lateinit var userAdapter: UserAdapter
    private val userList = mutableListOf<User>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMatchingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 더미 데이터 추가
        userList.add(User("gganpunggi01", "김철수", R.drawable.user_profile3))
        userList.add(User("runner02", "이영희", R.drawable.user_profile3))
        userList.add(User("battler03", "박준영", R.drawable.user_profile3))

        // activity가 null이 아닌 경우에만 어댑터 설정
        activity?.let {
            // 리사이클러뷰 설정 (초기에는 빈 리스트 설정 및 FragmentActivity 전달)
            userAdapter = UserAdapter(emptyList(), it)
            binding.userRecyclerView.layoutManager = LinearLayoutManager(context)
            binding.userRecyclerView.adapter = userAdapter
            binding.userRecyclerView.visibility = View.GONE // 처음에는 숨김 처리
        }

        // 검색 버튼 클릭 리스너 설정
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    if (it.isNotEmpty()) {
                        searchUser(it)
                    } else {
                        Toast.makeText(requireContext(), "검색어를 입력하세요", Toast.LENGTH_SHORT).show()
                    }
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    // 사용자를 검색하는 메서드
    private fun searchUser(query: String) {
        val filteredList = userList.filter { it.id.contains(query, ignoreCase = true) }
        if (filteredList.isNotEmpty()) {
            userAdapter.updateUserList(filteredList)
            binding.userRecyclerView.visibility = View.VISIBLE // 검색 결과가 있을 때만 리사이클러뷰 표시
        } else {
            userAdapter.updateUserList(emptyList()) // 검색 결과가 없을 때 빈 리스트로 설정
            binding.userRecyclerView.visibility = View.GONE // 검색 결과가 없으면 숨김
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
