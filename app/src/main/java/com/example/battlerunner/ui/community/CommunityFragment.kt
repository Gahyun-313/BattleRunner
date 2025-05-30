package com.example.battlerunner.ui.community

import android.app.AlertDialog
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.battlerunner.R
import com.example.battlerunner.data.local.DBHelper
import com.example.battlerunner.data.model.User
import com.example.battlerunner.databinding.FragmentCommunityBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        userList.add(User("gu20313@naver.com", "김가현", profileImageResId = R.drawable.user_profile3))
        userList.add(User("gus20313@gmail.com", "김가현", profileImageResId = R.drawable.user_profile3))
        userList.add(User("hyunindia0@gmail.com", "김가현", profileImageResId = R.drawable.user_profile3))
        userList.add(User("shhk2100@gmail.com", "sh kim", profileImageResId = R.drawable.user_profile3))
        userList.add(User("kkdi8393@gmail.com", "kkdi", profileImageResId = R.drawable.user_profile3))

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
            }
            ,
            onBragClicked = { user ->
                //showBragRecordsPopup() // 기록 선택 팝업 표시
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
    /*
        // 자랑하기 로직
        private fun showBragRecordsPopup() {
            // 모든 러닝 날짜를 가져옴 (yyyy-MM-dd 형식)
            val allDates = dbHelper.getAllRunningDates()
            if (allDates.isEmpty()) {
                Toast.makeText(requireContext(), "기록이 없습니다.", Toast.LENGTH_SHORT).show()
                return
            }

            val items = allDates.map { date ->
                "기록 날짜: $date"
            }.toTypedArray()

            // 날짜 선택 팝업 표시
            AlertDialog.Builder(requireContext())
                .setTitle("기록을 선택하세요")
                .setItems(items) { _, which ->
                    val selectedDate = allDates[which]

                    // 날짜 형식 맞추기
                    val formattedDate = formatDate(selectedDate)

                    // 기록 세부 정보 표시
                    showRecordDetailsForDate(formattedDate)
                }
                .setNegativeButton("취소", null)
                .show()
        }

        // 날짜를 yyyy-MM-dd 형식으로 변환
        private fun formatDate(date: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-M-d", Locale.getDefault()) // 입력 형식
                val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // 출력 형식
                val parsedDate = inputFormat.parse(date)
                outputFormat.format(parsedDate ?: date)
            } catch (e: Exception) {
                Log.e("CommunityFragment", "Error formatting date: $date", e)
                date // 변환 실패 시 원본 반환
            }
        }




        private fun showRecordDetailsForDate(date: String) {
            val recordData = dbHelper.getRecordsByDate(date) // 올바른 형식의 날짜 전달

            if (recordData.isEmpty()) {
                Toast.makeText(requireContext(), "해당 날짜에 기록이 없습니다.", Toast.LENGTH_SHORT).show()
                return
            }

            val items = recordData.map { record ->
                val elapsedTime = record.second / 1000 / 60 // 소요 시간 (분 단위)
                val distance = String.format("%.2f", record.third) // 거리
                "소요 시간: $elapsedTime 분, 거리: $distance m"
            }.toTypedArray()

            AlertDialog.Builder(requireContext())
                .setTitle("$date 기록 목록")
                .setItems(items) { _, which ->
                    val selectedRecord = recordData[which]
                    showRecordDetailsPopup(selectedRecord, useBragLayout = true) // 기록 팝업 표시
                }
                .setNegativeButton("닫기", null)
                .show()
        }






        private fun showRecordDetailsPopup(record: Triple<String, Long, Float>, useBragLayout: Boolean) {
            val layout = if (useBragLayout) R.layout.popup_running_data_with_brag else R.layout.popup_running_data
            val dialogView = LayoutInflater.from(requireContext()).inflate(layout, null)

            val popupImage = dialogView.findViewById<ImageView>(R.id.popupRunningImage)
            val popupElapsedTime = dialogView.findViewById<TextView>(R.id.popupElapsedTime)
            val popupDistance = dialogView.findViewById<TextView>(R.id.popupDistance)

            popupElapsedTime.text = "소요 시간: ${record.second / 1000 / 60} 분"
            popupDistance.text = "달린 거리: ${record.third} m"

            val imagePath = record.first
            val imageFile = File(imagePath)
            if (imageFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(imagePath)
                if (bitmap != null) {
                    popupImage.setImageBitmap(bitmap)
                } else {
                    popupImage.setImageResource(R.drawable.placeholder)
                    Log.e("CommunityFragment", "Failed to decode bitmap for imagePath: $imagePath")
                }
            } else {
                popupImage.setImageResource(R.drawable.placeholder)
                Log.e("CommunityFragment", "File does not exist at path: $imagePath")
            }

            AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("닫기", null)
                .show()
        }



    */


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}