package jp.ac.ecc.whisper_3e

import android.content.Context

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class WhisperAdapter(
    private val whisperList: MutableList<WhisperRowData>,
    private val context: Context,
    private val loginUserId: String,
    private val onUserImageClick: (WhisperRowData) -> Unit,
    private val onGoodClick: (WhisperRowData, Int) -> Unit
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
        holder.whisperText.text = item.content

        holder.goodImage.setImageResource(
            if (item.goodFlg) android.R.drawable.btn_star_big_on
            else android.R.drawable.btn_star_big_off
        )

        // Instead of internal handling, call your lambda here:
        holder.userImage.setOnClickListener {
            onUserImageClick(item)
        }
        holder.itemView.setOnClickListener {
            onUserImageClick(item)
        }

        holder.goodImage.setOnClickListener {
            onGoodClick(item, position)
        }
    }

    fun updateWhisperAt(position: Int, newWhisper: WhisperRowData) {
        whisperList[position] = newWhisper
        notifyItemChanged(position)
    }
    override fun getItemCount(): Int = whisperList.size

    fun updateWhispers(newList: List<WhisperRowData>) {
        whisperList.clear()
        whisperList.addAll(newList)
        notifyDataSetChanged()
    }
}
