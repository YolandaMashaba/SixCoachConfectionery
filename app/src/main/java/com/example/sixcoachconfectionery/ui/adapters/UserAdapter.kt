package com.example.sixcoachconfectionery.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sixcoachconfectionery.data.models.User
import com.example.sixcoachconfectionery.databinding.ItemUserRowBinding

class UserAdapter(
    private var users: List<User>,
    private val onToggleAdmin: (User) -> Unit
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
            tvUserUsername.text = user.username
            tvUserRoleBadge.text = if (user.isAdmin) "Role: Admin" else "Role: Staff"
            btnToggleAdmin.text = if (user.isAdmin) "Revoke Admin" else "Make Admin"

            btnToggleAdmin.setOnClickListener { onToggleAdmin(user) }
        }
    }

    override fun getItemCount(): Int = users.size

    fun updateData(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }
}
