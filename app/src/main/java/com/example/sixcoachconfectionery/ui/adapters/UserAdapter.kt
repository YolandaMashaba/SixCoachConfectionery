package com.example.sixcoachconfectionery.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.sixcoachconfectionery.R
import com.example.sixcoachconfectionery.data.model.UserProfile
import com.example.sixcoachconfectionery.databinding.ItemUserRowBinding

class UserAdapter(
    private var users: List<UserProfile>,
    private val onEditUser: (UserProfile) -> Unit,
    private val onDeleteUser: (UserProfile) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(val binding: ItemUserRowBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        with(holder.binding) {
            tvUserUsername.text = user.email.ifEmpty { user.username }
            val roleText = user.role.ifEmpty { if (user.isAdmin) "Admin" else "Counter Staff" }
            tvUserRoleBadge.text = roleText
            tvUserRoleBadge.setTextColor(ContextCompat.getColor(root.context, R.color.bakery_text_primary))

            btnEditUser.setOnClickListener { onEditUser(user) }
            btnDeleteUser.setOnClickListener { onDeleteUser(user) }
        }
    }

    override fun getItemCount(): Int = users.size

    fun updateData(newUsers: List<UserProfile>) {
        users = newUsers
        notifyDataSetChanged()
    }
}
