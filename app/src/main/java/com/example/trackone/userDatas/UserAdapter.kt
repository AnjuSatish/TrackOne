package com.example.trackone.userDatas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.trackone.R

class UserAdapter(private val userClickListener: UserClickListener) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {
    private var userList: List<User> = emptyList()
    interface UserClickListener {
        fun onUserClick(user: User)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_item, parent, false)
        return UserViewHolder(view)
    }
    fun setUserList(userList: List<User>) {
        this.userList = userList
        notifyDataSetChanged()
    }
    fun filterList(filteredList: List<User>) {
        userList = filteredList
        notifyDataSetChanged()
    }
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.bind(user,userClickListener)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

      //  private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        private val emailTextView: TextView = itemView.findViewById(R.id.usernameTextView)

        fun bind(user: User, userClickListener: UserClickListener) {
           // nameTextView.text = user.name
            emailTextView.text = user.email
            itemView.setOnClickListener {
                userClickListener.onUserClick(user)
            }

        }

    }

}
