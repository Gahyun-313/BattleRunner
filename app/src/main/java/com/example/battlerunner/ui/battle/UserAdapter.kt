package com.example.battlerunner.ui.battle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.battlerunner.R
import com.example.battlerunner.data.model.User

class UserAdapter(
    private val userList: List<User>, // 사용자 목록
    private val onMatchButtonClick: (String) -> Unit // 버튼 클릭 콜백
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.profile_image) // 프로필 이미지
        val userId: TextView = itemView.findViewById(R.id.user_id) // 사용자 ID
        val userName: TextView = itemView.findViewById(R.id.user_name) // 사용자 이름
        val matchButton: Button = itemView.findViewById(R.id.match_button) // 매칭 버튼
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_matching, parent, false) // 뷰 생성
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.profileImage.setImageResource(R.drawable.user_profile3) // 프로필 이미지 설정
        holder.userId.text = user.userId // 사용자 ID 설정
//        holder.userName.text = user.userName // 사용자 이름 설정

        // 매칭 버튼 클릭 리스너 설정
        holder.matchButton.setOnClickListener {
//            onMatchButtonClick(user.userId, user.userName) // 클릭 시 콜백 호출
            onMatchButtonClick(user.userId) //클릭 시 콜백 호출
        }
    }

    override fun getItemCount(): Int = userList.size // 아이템 개수 반환
}
