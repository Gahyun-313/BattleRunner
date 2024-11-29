package com.example.battlerunner.ui.community

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.battlerunner.R
import com.example.battlerunner.data.local.DBHelper
import com.example.battlerunner.data.model.User
import com.example.battlerunner.databinding.FragmentCommunityBinding

class CommunityFragment : Fragment() {

    private var _binding: FragmentCommunityBinding? = null
    private val binding get() = _binding!!

    private val userList = mutableListOf<User>() // 검색 가능한 사용자 리스트
    private val friendList = mutableListOf<User>() // 친구 목록
    private lateinit var userAdapter: CommunityUserAdapter
    private lateinit var friendAdapter: FriendAdapter
    private lateinit var dbHelper: DBHelper // DBHelper 인스턴스

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommunityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // DBHelper 초기화
        dbHelper = DBHelper.getInstance(requireContext())

        // 친구 목록 불러오기
        friendList.addAll(dbHelper.getFriends())

        // 더미 데이터 추가
        userList.add(User("gganpunggi01", "김철수", profileImageResId = R.drawable.user_profile3))
        userList.add(User("runner02", "이영희", profileImageResId = R.drawable.user_profile3))
        userList.add(User("battler03", "박준영", profileImageResId = R.drawable.user_profile3))

        // 검색 결과 RecyclerView 설정
        userAdapter = CommunityUserAdapter(emptyList()) { user ->
            onAddFriend(user) // 친구 추가 버튼 클릭 시 로직
        }
        binding.communityRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.communityRecyclerView.adapter = userAdapter

        // 친구 목록 RecyclerView 설정
        friendAdapter = FriendAdapter(
            friendList,
            onDeleteClicked = { user ->
                onDeleteFriend(user) // 친구 삭제 버튼 클릭 시 로직
            },
            onBragClicked = { user ->
                onBrag(user) // 자랑하기 버튼 클릭 시 로직
            }
        )
        binding.friendRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.friendRecyclerView.adapter = friendAdapter


        // SearchView 설정
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    if (it.isNotEmpty()) {
                        searchUsers(it)
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

    private fun searchUsers(query: String) {
        // 검색어와 일치하는 사용자 필터링
        val filteredList = userList.filter { it.userId.contains(query, ignoreCase = true) }

        if (filteredList.isNotEmpty()) {
            userAdapter.updateUserList(filteredList)
            binding.communityRecyclerView.visibility = View.VISIBLE // 검색 결과가 있을 때만 표시
        } else {
            userAdapter.updateUserList(emptyList())
            binding.communityRecyclerView.visibility = View.GONE // 결과가 없으면 숨김
            Toast.makeText(requireContext(), "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onAddFriend(user: User) {
        // 이미 친구인지 확인
        if (friendList.any { it.userId == user.userId }) {
            Toast.makeText(requireContext(), "이미 친구입니다.", Toast.LENGTH_SHORT).show()
        } else {
            // 친구 추가
            friendList.add(user)
            friendAdapter.notifyDataSetChanged() // 친구 목록 업데이트
            dbHelper.addFriend(user.userId, user.username, user.profileImageResId) // SQLite에 저장
            Toast.makeText(requireContext(), "${user.username}님이 친구로 추가되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // 친구 삭제 로직
    private fun onDeleteFriend(user: User) {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("친구 삭제")
            .setMessage("${user.username}님을 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                // 목록에서 삭제
                friendAdapter.removeFriend(user)

                // 데이터베이스에서도 삭제
                dbHelper.deleteFriend(user.userId)

                Toast.makeText(requireContext(), "${user.username}님이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("취소", null)
            .create()
        dialog.show()
    }

    // 자랑하기 로직
    private fun onBrag(user: User) {
        Toast.makeText(requireContext(), "${user.username}님에게 자랑하기 버튼 실행!", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
