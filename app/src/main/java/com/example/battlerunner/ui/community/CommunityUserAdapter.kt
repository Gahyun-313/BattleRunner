package com.example.battlerunner.ui.community

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.battlerunner.data.model.User
import com.example.battlerunner.databinding.ItemUserBinding

class CommunityUserAdapter(
    private var userList: List<User>,
    private val onAddFriendClicked: (User) -> Unit
) : RecyclerView.Adapter<CommunityUserAdapter.UserViewHolder>() {

    inner class UserViewHolder(private val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.userId.text = user.userId
            binding.userName.text = user.username
            binding.profileImage.setImageResource(user.profileImageResId)

            binding.addFriendButton.setOnClickListener {
                onAddFriendClicked(user)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(userList[position])
    }

    override fun getItemCount(): Int = userList.size

    fun updateUserList(newList: List<User>) {
        userList = newList
        notifyDataSetChanged()
    }
}