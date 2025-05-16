package jp.ac.ecc.whisper_3e

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class GoodAdapter(
    private val context: Context,
    private val items: MutableList<GoodRowData>
) : RecyclerView.Adapter<GoodAdapter.GoodViewHolder>() {

    // ViewHolder for each item
    inner class GoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userImage: ImageView = itemView.findViewById(R.id.userImage)
        val userNameText: TextView = itemView.findViewById(R.id.userNameText)
        val whisperText: TextView = itemView.findViewById(R.id.whisperText)
        val goodCntText: TextView = itemView.findViewById(R.id.goodCntText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoodViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.good_recycle_row, parent, false)
        return GoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: GoodViewHolder, position: Int) {
        val currentItem = items[position]

        // Bind the data to the views
        holder.userNameText.text = currentItem.userName
        holder.whisperText.text = currentItem.whisper
        holder.goodCntText.text = context.getString(R.string.likes_text, currentItem.goodCount)

        // Load user image with Glide
        Glide.with(context)
            .load(currentItem.userImagePath)
            .into(holder.userImage)

        // Set click listener on user image to open UserInfoActivity
        holder.userImage.setOnClickListener {
            val intent = Intent(context, UserInfoActivity::class.java).apply {
                putExtra("userId", currentItem.userId)  // Key should be "userId"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    // Add this method to update the adapter's data and refresh the RecyclerView
    fun updateWhispers(newList: List<GoodRowData>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
}
