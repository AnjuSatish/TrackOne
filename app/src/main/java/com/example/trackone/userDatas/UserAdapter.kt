package com.example.trackone.userDatas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.trackone.R
import java.util.Locale

class UserAdapter(private val userClickListener: UserClickListener) : RecyclerView.Adapter<UserAdapter.UserViewHolder>(),
    Filterable {
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
            emailTextView.text = user.name
            itemView.setOnClickListener {
                userClickListener.onUserClick(user)
            }

        }

    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.toLowerCase(Locale.getDefault())
                val filteredList = if (query.isNullOrEmpty()) {
                    userList
                } else {
                    userList.filter { user ->
                        user.name.toLowerCase(Locale.getDefault()).contains(query)
                    }
                }
                val results = FilterResults()
                results.values = filteredList
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                userList = results?.values as List<User>
                notifyDataSetChanged()

            }
        }
    }
    }