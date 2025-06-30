package jp.ac.ecc.whisper_3e

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GoodAdapter(
    private val context: Context,
    private val items: MutableList<GoodRowData>
) : RecyclerView.Adapter<GoodAdapter.GoodViewHolder>() {

    inner class GoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userImage: ImageView = itemView.findViewById(R.id.userImage)
        val userNameText: TextView = itemView.findViewById(R.id.userNameText)
        val whisperText: TextView = itemView.findViewById(R.id.whisperText)
        val goodFlgText: TextView = itemView.findViewById(R.id.goodText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoodViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.good_recycle_row, parent, false)
        return GoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: GoodViewHolder, position: Int) {
        val currentItem = items[position]

        holder.userNameText.text = currentItem.userName
        holder.whisperText.text = currentItem.content
        holder.goodFlgText.text = if (currentItem.goodFlg) "Liked" else "Not Liked"

        // Navigate to UserInfoActivity
        holder.userImage.setOnClickListener {
            val intent = Intent(context, UserInfoActivity::class.java).apply {
                putExtra("userId", currentItem.userId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }

        // Optional: click whole row to go to profile too
        holder.itemView.setOnClickListener {
            val intent = Intent(context, UserInfoActivity::class.java).apply {
                putExtra("userId", currentItem.userId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateGood(newList: List<GoodRowData>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
}
