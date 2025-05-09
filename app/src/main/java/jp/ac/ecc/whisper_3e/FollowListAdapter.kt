package jp.ac.ecc.whisper_3e

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.json.JSONObject

class FollowListAdapter(
    private val context: Context,
    private var items: List<JSONObject>  // Now the list holds JSONObject items
) : RecyclerView.Adapter<FollowListAdapter.FollowViewHolder>() {

    // ViewHolder for each item
    inner class FollowViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userImage: ImageView = itemView.findViewById(R.id.userImage)
        val userNameText: TextView = itemView.findViewById(R.id.userNameText)
        val followStatusText: TextView = itemView.findViewById(R.id.followListText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.activity_follow_list, parent, false)
        return FollowViewHolder(view)
    }

    override fun onBindViewHolder(holder: FollowViewHolder, position: Int) {
        val currentItem = items[position]

        // Extract data from JSONObject and bind it to the views
        val userName = currentItem.getString("username")  // Assuming 'username' is the key
        val userImagePath = currentItem.getString("imageUrl")  // Assuming 'imageUrl' is the key for user image
        val isFollowing = currentItem.getBoolean("isFollowing")  // Assuming 'isFollowing' is a boolean key

        // Bind the data to the views
        holder.userNameText.text = userName
        holder.followStatusText.text = if (isFollowing) "Following" else "Follow"

        // Set up user image (you may use Glide for image loading)
        Glide.with(context).load(userImagePath).into(holder.userImage)

        // Set click listener on the user image to open the user's profile
        holder.userImage.setOnClickListener {
            val intent = Intent(context, UserInfoActivity::class.java)
            intent.putExtra("USER_ID", currentItem.getString("userId"))  // Assuming the user ID is in the JSONObject
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    // Method to update the data in the adapter
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newItems: List<JSONObject>) {
        items = newItems
        notifyDataSetChanged()  // Notify adapter that data has been updated
    }
}
