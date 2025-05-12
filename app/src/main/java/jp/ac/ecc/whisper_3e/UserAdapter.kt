package jp.ac.ecc.whisper_3e

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class UserAdapter(
    private val userList: MutableList<UserRowData>,
    private val onItemClicked: (UserRowData) -> Unit  // Change name to `onItemClicked`
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userImage: ImageView = view.findViewById(R.id.userImage)
        val userNameText: TextView = view.findViewById(R.id.userNameText)
        val followCntText: TextView = view.findViewById(R.id.followCntText)
        val followerCntText: TextView = view.findViewById(R.id.followerCntText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_recycle_row, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]

        holder.userNameText.text = user.userName
        holder.followCntText.text = user.followCount.toString()
        holder.followerCntText.text = user.followerCount.toString()

        // Load the user image with Glide
        Glide.with(holder.itemView.context)
            .load(user.imagePath)
            .placeholder(R.drawable.ic_launcher_foreground)
            .into(holder.userImage)

        // Set up the click listener for each user item
        holder.itemView.setOnClickListener {
            // Pass the clicked user data to the callback to update the UI
            onItemClicked(user)
        }
    }

    override fun getItemCount(): Int = userList.size
}
