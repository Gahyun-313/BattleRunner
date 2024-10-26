package com.example.battlerunner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView

class UserAdapter(private var userList: List<User>, private val activity: FragmentActivity) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    // ViewHolder 클래스 정의
    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.profile_image)
        val userId: TextView = itemView.findViewById(R.id.user_id)
        val userName: TextView = itemView.findViewById(R.id.user_name)
        val matchButton: Button = itemView.findViewById(R.id.match_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_matching, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.profileImage.setImageResource(user.profileImageResId)
        holder.userId.text = user.id
        holder.userName.text = user.name

        // 매칭 버튼 클릭 시 BattleFragment로 이동
        holder.matchButton.setOnClickListener {
            val fragmentManager = activity.supportFragmentManager
            val transaction = fragmentManager.beginTransaction()

            // BattleFragment로 이름 전달하기 위해 Bundle 사용
            val battleFragment = BattleFragment()
            val bundle = Bundle()
            bundle.putString("userName", user.name) // 선택한 사용자 이름 전달
            battleFragment.arguments = bundle

            transaction.replace(R.id.fragmentContainer, battleFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    // 업데이트 메서드 추가
    fun updateUserList(newList: List<User>) {
        userList = newList
        notifyDataSetChanged() // 데이터가 변경되었음을 어댑터에 알림
    }
}
