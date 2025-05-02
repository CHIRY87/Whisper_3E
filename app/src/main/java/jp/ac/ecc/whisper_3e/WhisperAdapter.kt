package jp.ac.ecc.whisper_3e

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WhisperAdapter(
    private val whisperList: MutableList<String>
) : RecyclerView.Adapter<WhisperAdapter.WhisperViewHolder>() {

    inner class WhisperViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userImage: ImageView = itemView.findViewById(R.id.userImage)
        val userNameText: TextView = itemView.findViewById(R.id.userNameText)
        val whisperText: TextView = itemView.findViewById(R.id.whisperText)
        val goodImage: ImageView = itemView.findViewById(R.id.goodImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WhisperViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.whisper_recycle_row, parent, false)
        return WhisperViewHolder(view)
    }

    override fun onBindViewHolder(holder: WhisperViewHolder, position: Int) {
        val item = whisperList[position]

        holder.userNameText.text = item.userName
        holder.whisperText.text = item.whisperText

        // Set the goodImage based on like status
        holder.goodImage.setImageResource(
            if (item.isLiked) android.R.drawable.btn_star_big_on
            else android.R.drawable.btn_star_big_off
        )

        // Optional: Load image if using a library (Glide, Picasso, etc.)
        // Glide.with(context).load(item.userIconPath).into(holder.userImage)

        // User image click: go to profile (example Intent)
        holder.userImage.setOnClickListener {
            val intent = Intent(context, UserProfileActivity::class.java).apply {
                putExtra("userId", item.userId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }

        // Like button click: toggle like
        holder.goodImage.setOnClickListener {
            item.isLiked = !item.isLiked
            notifyItemChanged(position)

            // You can add API call here to update like status
        }
    }

    override fun getItemCount(): Int = whisperList.size
}
