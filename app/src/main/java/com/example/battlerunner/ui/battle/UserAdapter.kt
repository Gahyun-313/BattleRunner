package com.example.battlerunner.ui.battle

import android.view.LayoutInflater
import android.view.View
import android.content.Intent
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.battlerunner.R
import com.example.battlerunner.data.model.User

class UserAdapter(private var userList: List<User>, private val activity: FragmentActivity) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

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
        holder.userId.text = user.userId
        holder.userName.text = user.username

        // 신청 버튼 클릭 리스너
        holder.matchButton.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, BattleApplyActivity::class.java).apply {
                putExtra("userName", user.username)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = userList.size

    fun updateUserList(newList: List<User>) {
        userList = newList
        notifyDataSetChanged()
    }
}