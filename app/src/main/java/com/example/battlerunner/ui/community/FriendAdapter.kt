package com.example.battlerunner.ui.community

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.battlerunner.data.model.User
import com.example.battlerunner.databinding.ItemFriendBinding

class FriendAdapter(
    private val friendList: MutableList<User>,
    private val onDeleteClicked: (User) -> Unit, // 삭제 버튼 콜백
    private val onBragClicked: (User) -> Unit   // 자랑하기 버튼 콜백
) : RecyclerView.Adapter<FriendAdapter.FriendViewHolder>() {

    inner class FriendViewHolder(private val binding: ItemFriendBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.friendNameTextView.text = user.username
            binding.friendIdTextView.text = user.userId
            binding.friendProfileImage.setImageResource(user.profileImageResId)

            // 자랑하기 버튼 클릭 이벤트
            /*
            binding.bragButton.setOnClickListener {
                onBragClicked(user)
            }*/

            // 삭제 버튼 클릭 이벤트
            binding.deleteButton.setOnClickListener {
                onDeleteClicked(user)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val binding = ItemFriendBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FriendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.bind(friendList[position])
    }

    override fun getItemCount(): Int = friendList.size

    // 친구 삭제 메서드
    fun removeFriend(user: User) {
        val position = friendList.indexOf(user)
        if (position != -1) {
            friendList.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}